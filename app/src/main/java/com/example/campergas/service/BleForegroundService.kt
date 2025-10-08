package com.example.campergas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.isActive
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
    
    // Control for periodic BLE requests
    private var isPeriodicRequestsActive = false
    private var periodicRequestsJob: kotlinx.coroutines.Job? = null
    private var lastWeightRequestTime = 0L
    private var lastInclinationRequestTime = 0L
    
    // Intervalos configurables (se cargan desde preferencias)
    private var weightRequestInterval = 5000L // 5 segundos por defecto
    private var inclinationRequestInterval = 5000L // 5 segundos por defecto

    companion object {
        private const val TAG = "BleForegroundService"
        const val ACTION_START_FOR_WIDGETS = "START_FOR_WIDGETS"
        
        fun startForWidgets(context: Context): Boolean {
            return ForegroundServiceUtils.startServiceSafelyIfNotRunning(
                context,
                BleForegroundService::class.java
            ) { intent ->
                intent.action = ACTION_START_FOR_WIDGETS
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
                // Try to connect to last known device
                connectToLastKnownDevice()
            }
            else -> {
                Log.d(TAG, "Servicio iniciado - conectando al último dispositivo conocido")
                connectToLastKnownDevice()
            }
        }

        // Return START_STICKY so service restarts automatically if system kills it
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
                    // Still load configuration and be ready for when it connects
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

                // Load interval configuration from preferences
                loadConfigurationAndStartPeriodicRequests()

                // Monitor fuel measurements and update notification
                launch {
                    bleRepository.fuelMeasurementData.collect { fuelMeasurement ->
                        if (fuelMeasurement != null) {
                            // Verificar umbral de gas para alertas
                            checkGasLevelThreshold(fuelMeasurement.fuelPercentage)
                            
                            // Actualizar widget de bombona de gas
                            GasCylinderWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        }
                    }
                }

                // Monitor inclination data to update widget
                launch {
                    bleRepository.inclinationData.collect { inclinationData ->
                        if (inclinationData != null) {
                            // Update vehicle stability widget
                            VehicleStabilityWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        }
                    }
                }
                
                // Monitor connection state to handle reconnections
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
                        
                        // Update widgets when connection state changes
                        GasCylinderWidgetProvider.updateAllWidgets(this@BleForegroundService)
                        VehicleStabilityWidgetProvider.updateAllWidgets(this@BleForegroundService)
                    }
                }

                // Save connected device address
                bleRepository.saveLastConnectedDevice(deviceAddress)

            } catch (e: Exception) {
                Log.e(TAG, "Error al conectar al dispositivo: ${e.message}")
                // Do not stop service to allow automatic retries
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
                
                // Start periodic requests if there is connection
                if (bleRepository.connectionState.first()) {
                    startPeriodicBleRequests()
                }
                
                // Listen for configuration changes to update intervals
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
            
            while (isPeriodicRequestsActive && isActive) {
                try {
                    val currentTime = System.currentTimeMillis()
                    
                    // Verify if there is BLE connection before making requests
                    val isConnected = bleRepository.connectionState.first()
                    if (!isConnected) {
                        Log.d(TAG, "No hay conexión BLE, pausando solicitudes...")
                        delay(2000) // Pause if no connection
                        continue
                    }
                    
                    // Request weight data if more than configured interval has passed
                    if (currentTime - lastWeightRequestTime > weightRequestInterval) {
                        Log.d(TAG, "Solicitando datos de peso...")
                        bleRepository.readWeightDataOnDemand()
                        lastWeightRequestTime = currentTime
                    }
                    
                    // Wait a bit before requesting inclination to avoid BLE conflicts
                    delay(500)
                    
                    // Request inclination data if more than configured interval has passed
                    if (currentTime - lastInclinationRequestTime > inclinationRequestInterval) {
                        Log.d(TAG, "Solicitando datos de inclinación...")
                        bleRepository.readInclinationDataOnDemand()
                        lastInclinationRequestTime = currentTime
                    }
                    
                    // Pause between verification cycles (1000ms)
                    delay(1000)
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error en solicitud periódica BLE: ${e.message}")
                    delay(2000) // Longer pause in case of error
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
     * Reinicia las solicitudes periódicas si están activas (útil when changesn los intervalos)
     */
    private fun restartPeriodicRequestsIfActive() {
        if (isPeriodicRequestsActive) {
            Log.d(TAG, "Reiniciando solicitudes periódicas con nuevos intervalos...")
            stopPeriodicBleRequests()
            
            // Short pause before restarting
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
        val descriptionText = "Canal for the servicio BLE de CamperGas"
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
                
                // Only send alert if gas is below threshold
                if (currentPercentage <= threshold) {
                    // Avoid spam: only send if not sent for this threshold or if threshold changed
                    if (!hasAlertBeenSent || lastAlertThreshold != threshold) {
                        sendGasAlert(currentPercentage, threshold)
                        hasAlertBeenSent = true
                        lastAlertThreshold = threshold
                    }
                } else {
                    // Reset alert state when gas is above threshold
                    if (hasAlertBeenSent) {
                        hasAlertBeenSent = false
                    }
                }
            } catch (_: Exception) {
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
        
        // Stop periodic requests
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
