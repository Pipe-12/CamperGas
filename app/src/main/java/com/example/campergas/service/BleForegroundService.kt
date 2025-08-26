package com.example.campergas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.campergas.R
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.utils.ForegroundServiceUtils
import com.example.campergas.widget.GasCylinderWidgetProvider
import com.example.campergas.widget.VehicleStabilityWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject
    lateinit var bleRepository: BleRepository

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationId = 123
    private val channelId = "ble_service_channel"
    private val alertChannelId = "gas_alert_channel"
    private val alertNotificationId = 124
    
    // Estado para evitar spam de notificaciones
    private var lastAlertThreshold: Float? = null
    private var hasAlertBeenSent = false
    
    // Control para las solicitudes periódicas de BLE
    private var isPeriodicRequestsActive = false
    private var periodicRequestsJob: kotlinx.coroutines.Job? = null
    private var lastWeightRequestTime = 0L
    private var lastInclinationRequestTime = 0L
    
    // Intervalos configurables (se cargan desde preferencias)
    private var weightRequestInterval = 5000L // 5 segundos por defecto
    private var inclinationRequestInterval = 5000L // 5 segundos por defecto

    companion object {
        private const val TAG = "BleForegroundService"
        const val KEY_DEVICE_ADDRESS = "device_address"
        const val ACTION_START_FOR_WIDGETS = "START_FOR_WIDGETS"
        
        fun startForWidgets(context: Context): Boolean {
            return ForegroundServiceUtils.startServiceSafelyIfNotRunning(
                context,
                BleForegroundService::class.java
            ) { intent ->
                intent.action = ACTION_START_FOR_WIDGETS
            }
        }
        
        fun startWithDevice(context: Context, deviceAddress: String): Boolean {
            return ForegroundServiceUtils.startServiceSafely(
                context,
                BleForegroundService::class.java
            ) { intent ->
                intent.putExtra(KEY_DEVICE_ADDRESS, deviceAddress)
            }
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, BleForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createAlertNotificationChannel()
        Log.d(TAG, "BleForegroundService creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Try to start as foreground service if possible using the same safe logic as starting the service
        try {
            if (ForegroundServiceUtils.canStartForegroundService(this)) {
                val notification = createNotification("CamperGas en funcionamiento")
                startForeground(notificationId, notification)
                Log.d(TAG, "Service started as foreground service")
            } else {
                Log.d(TAG, "Service started as regular service (foreground not allowed)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not start as foreground service, running as regular service", e)
            // Continue as regular service - widgets will still be updated when app is in foreground
        }

        when (intent?.action) {
            ACTION_START_FOR_WIDGETS -> {
                Log.d(TAG, "Servicio iniciado para widgets")
                // Intentar conectar al último dispositivo conocido
                connectToLastKnownDevice()
            }
            else -> {
                val deviceAddress = intent?.getStringExtra(KEY_DEVICE_ADDRESS)
                if (deviceAddress != null) {
                    Log.d(TAG, "Servicio iniciado con dispositivo: $deviceAddress")
                    connectToDevice(deviceAddress)
                } else {
                    Log.d(TAG, "Servicio iniciado sin dispositivo específico")
                    connectToLastKnownDevice()
                }
            }
        }

        // Retornamos START_STICKY para que el servicio se reinicie automáticamente si el sistema lo mata
        return START_STICKY
    }
    
    private fun connectToLastKnownDevice() {
        serviceScope.launch {
            try {
                val lastDeviceAddress = preferencesDataStore.lastConnectedDeviceAddress.first()
                if (lastDeviceAddress.isNotEmpty()) {
                    Log.d(TAG, "Conectando al último dispositivo conocido: $lastDeviceAddress")
                    connectToDevice(lastDeviceAddress)
                } else {
                    Log.d(TAG, "No hay dispositivo conocido previo, iniciando solicitudes periódicas sin conexión")
                    // Aún así cargar configuración y estar preparado para cuando se conecte
                    loadConfigurationAndStartPeriodicRequests()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al conectar al último dispositivo conocido: ${e.message}")
                loadConfigurationAndStartPeriodicRequests()
            }
        }
    }

    private fun connectToDevice(deviceAddress: String) {
        serviceScope.launch {
            try {
                // Conectar al dispositivo BLE
                bleRepository.connectToSensor(deviceAddress)

                // Cargar configuración de intervalos desde preferencias
                loadConfigurationAndStartPeriodicRequests()

                // Monitorear las mediciones de combustible y actualizar la notificación
                launch {
                    bleRepository.fuelMeasurementData.collect { fuelMeasurement ->
                        if (fuelMeasurement != null) {
                            updateNotification("Combustible: ${fuelMeasurement.getFormattedFuelKilograms()} (${fuelMeasurement.getFormattedPercentage()})")
                            
                            // Verificar umbral de gas para alertas
                            checkGasLevelThreshold(fuelMeasurement.fuelPercentage)
                            
                            // Actualizar widget de bombona de gas
                            GasCylinderWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        }
                    }
                }

                // Monitorear datos de inclinación para actualizar widget
                launch {
                    bleRepository.inclinationData.collect { inclinationData ->
                        if (inclinationData != null) {
                            // Actualizar widget de estabilidad del vehículo
                            VehicleStabilityWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        }
                    }
                }
                
                // Monitorear estado de conexión para manejar reconexiones
                launch {
                    bleRepository.connectionState.collect { isConnected ->
                        if (isConnected) {
                            Log.d(TAG, "Conectado - iniciando solicitudes periódicas")
                            if (!isPeriodicRequestsActive) {
                                startPeriodicBleRequests()
                            }
                        } else {
                            Log.d(TAG, "Desconectado - deteniendo solicitudes periódicas")
                            stopPeriodicBleRequests()
                        }
                        
                        // Actualizar widgets cuando cambie el estado de conexión
                        GasCylinderWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        VehicleStabilityWidgetProvider.updateAllWidgets(this@BleForegroundService)
                    }
                }

                // Guardar la dirección del dispositivo conectado
                bleRepository.saveLastConnectedDevice(deviceAddress)

            } catch (e: Exception) {
                Log.e(TAG, "Error al conectar al dispositivo: ${e.message}")
                // No detenemos el servicio para permitir reintentos automáticos
                updateNotification("Error de conexión - reintentando...")
            }
        }
    }
    
    /**
     * Carga la configuración de intervalos desde preferencias e inicia las solicitudes periódicas
     */
    private fun loadConfigurationAndStartPeriodicRequests() {
        serviceScope.launch {
            try {
                // Cargar intervalos desde preferencias
                weightRequestInterval = preferencesDataStore.weightReadInterval.first()
                inclinationRequestInterval = preferencesDataStore.inclinationReadInterval.first()
                
                Log.d(TAG, "Configuración cargada - Peso: ${weightRequestInterval}ms, Inclinación: ${inclinationRequestInterval}ms")
                
                // Iniciar solicitudes periódicas si hay conexión
                if (bleRepository.connectionState.first()) {
                    startPeriodicBleRequests()
                }
                
                // Escuchar cambios en la configuración para actualizar intervalos
                launch {
                    preferencesDataStore.weightReadInterval.collect { newInterval ->
                        if (newInterval != weightRequestInterval) {
                            Log.d(TAG, "Intervalo de peso actualizado: $newInterval ms")
                            weightRequestInterval = newInterval
                            restartPeriodicRequestsIfActive()
                        }
                    }
                }
                
                launch {
                    preferencesDataStore.inclinationReadInterval.collect { newInterval ->
                        if (newInterval != inclinationRequestInterval) {
                            Log.d(TAG, "Intervalo de inclinación actualizado: $newInterval ms")
                            inclinationRequestInterval = newInterval
                            restartPeriodicRequestsIfActive()
                        }
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar configuración: ${e.message}")
                // Usar valores por defecto si falla
                weightRequestInterval = 5000L
                inclinationRequestInterval = 5000L
            }
        }
    }
    
    /**
     * Inicia las solicitudes periódicas de datos BLE según los intervalos configurados
     */
    private fun startPeriodicBleRequests() {
        if (isPeriodicRequestsActive) {
            Log.d(TAG, "Las solicitudes periódicas ya están activas")
            return
        }
        
        isPeriodicRequestsActive = true
        periodicRequestsJob = serviceScope.launch {
            Log.d(TAG, "🔄 Iniciando solicitudes periódicas de datos BLE...")
            Log.d(TAG, "📊 Intervalo peso: ${weightRequestInterval}ms, Intervalo inclinación: ${inclinationRequestInterval}ms")
            
            while (isPeriodicRequestsActive && !periodicRequestsJob!!.isCancelled) {
                try {
                    val currentTime = System.currentTimeMillis()
                    
                    // Verificar si hay conexión BLE antes de hacer solicitudes
                    val isConnected = bleRepository.connectionState.first()
                    if (!isConnected) {
                        Log.d(TAG, "No hay conexión BLE, pausando solicitudes...")
                        delay(2000) // Pausa si no hay conexión
                        continue
                    }
                    
                    // Solicitar datos de peso si han pasado más del intervalo configurado
                    if (currentTime - lastWeightRequestTime > weightRequestInterval) {
                        Log.d(TAG, "Solicitando datos de peso...")
                        bleRepository.readWeightDataOnDemand()
                        lastWeightRequestTime = currentTime
                    }
                    
                    // Esperar un poco antes de solicitar inclinación para evitar conflictos BLE
                    delay(500)
                    
                    // Solicitar datos de inclinación si han pasado más del intervalo configurado
                    if (currentTime - lastInclinationRequestTime > inclinationRequestInterval) {
                        Log.d(TAG, "Solicitando datos de inclinación...")
                        bleRepository.readInclinationDataOnDemand()
                        lastInclinationRequestTime = currentTime
                    }
                    
                    // Pausa entre ciclos de verificación (1000ms)
                    delay(1000)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error en solicitud periódica BLE: ${e.message}")
                    delay(2000) // Pausa más larga en caso de error
                }
            }
            
            Log.d(TAG, "Solicitudes periódicas de datos BLE finalizadas")
        }
    }
    
    /**
     * Detiene las solicitudes periódicas de datos BLE
     */
    private fun stopPeriodicBleRequests() {
        isPeriodicRequestsActive = false
        periodicRequestsJob?.cancel()
        periodicRequestsJob = null
        Log.d(TAG, "Solicitudes periódicas de datos BLE detenidas")
    }
    
    /**
     * Reinicia las solicitudes periódicas si están activas (útil cuando cambian los intervalos)
     */
    private fun restartPeriodicRequestsIfActive() {
        if (isPeriodicRequestsActive) {
            Log.d(TAG, "Reiniciando solicitudes periódicas con nuevos intervalos...")
            stopPeriodicBleRequests()
            
            // Pequeña pausa antes de reiniciar
            serviceScope.launch {
                delay(500)
                if (bleRepository.connectionState.first()) {
                    startPeriodicBleRequests()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "CamperGas BLE Service"
        val descriptionText = "Canal para el servicio BLE de CamperGas"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createAlertNotificationChannel() {
        val name = "Alertas de Gas"
        val descriptionText = "Notificaciones de alerta cuando el gas está bajo"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(alertChannelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("CamperGas")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun checkGasLevelThreshold(currentPercentage: Float) {
        serviceScope.launch {
            try {
                val notificationsEnabled = preferencesDataStore.areNotificationsEnabled.first()
                if (!notificationsEnabled) return@launch
                
                val threshold = preferencesDataStore.gasLevelThreshold.first()
                
                // Solo enviar alerta si el gas está por debajo del umbral
                if (currentPercentage <= threshold) {
                    // Evitar spam: solo enviar si no se ha enviado para este umbral o si el umbral cambió
                    if (!hasAlertBeenSent || lastAlertThreshold != threshold) {
                        sendGasAlert(currentPercentage, threshold)
                        hasAlertBeenSent = true
                        lastAlertThreshold = threshold
                    }
                } else {
                    // Reset del estado de alerta cuando el gas está por encima del umbral
                    if (hasAlertBeenSent) {
                        hasAlertBeenSent = false
                    }
                }
            } catch (e: Exception) {
                // Manejar errores silenciosamente para no afectar el servicio principal
            }
        }
    }

    private fun sendGasAlert(currentPercentage: Float, threshold: Float) {
        val title = "⚠️ Nivel de Gas Bajo"
        val message = "El gas está al ${currentPercentage.toInt()}% (por debajo del ${threshold.toInt()}%)"
        
        val alertNotification = NotificationCompat.Builder(this, alertChannelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(alertNotificationId, alertNotification)
    }

    override fun onDestroy() {
        Log.d(TAG, "Destruyendo BleForegroundService...")
        
        // Detener solicitudes periódicas
        stopPeriodicBleRequests()
        
        serviceScope.launch {
            bleRepository.disconnectSensor()
        }
        serviceScope.cancel()
        super.onDestroy()
        
        Log.d(TAG, "BleForegroundService destruido")
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
