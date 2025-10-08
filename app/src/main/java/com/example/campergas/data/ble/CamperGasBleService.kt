package com.example.campergas.data.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.CamperGasUuids
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.model.Inclination
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import com.example.campergas.domain.usecase.SaveFuelMeasurementUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio BLE unificado para manejar todas las características from sensor CamperGas
 * El sensor tiene un solo servicio con tres características READ-only:
 * - FuelMeasurement: datos de medición de combustible (lectura bajo demanda)
 * - Inclination: datos de inclinación (lectura bajo demanda)
 * - Offline: datos históricos en lotes (lectura continua al conectar)
 *
 * IMPORTANTE: El sensor ahora usa modo READ en lugar de notificaciones.
 * La aplicación debe solicitar activamente los datos leyendo las características.
 */
@Singleton
class CamperGasBleService @Inject constructor(
    private val bleManager: BleManager,
    private val saveFuelMeasurementUseCase: SaveFuelMeasurementUseCase,
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase,
    private val preferencesDataStore: PreferencesDataStore,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CamperGasBleService"
    }

    // Scope para operaciones en segundo plano
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Connection states
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState

    // Datos de peso en tiempo real (mantener para compatibilidad con sensores)
    private val _fuelMeasurementData = MutableStateFlow<FuelMeasurement?>(null)
    val fuelMeasurementData: StateFlow<FuelMeasurement?> = _fuelMeasurementData

    // Datos de combustible calculados
    private val _fuelData = MutableStateFlow<FuelMeasurement?>(null)
    val fuelData: StateFlow<FuelMeasurement?> = _fuelData

    // Real-time inclination data
    private val _inclinationData = MutableStateFlow<Inclination?>(null)
    val inclinationData: StateFlow<Inclination?> = _inclinationData

    // Historical data
    private val _historyData = MutableStateFlow<List<FuelMeasurement>>(emptyList())
    val historyData: StateFlow<List<FuelMeasurement>> = _historyData

    // Historical data loading state
    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory

    // Control para lectura continua de datos offline
    private var isReadingOfflineData = false
    private var offlineDataCount = 0
    private val allHistoryData = mutableListOf<FuelMeasurement>()
    private val processedOfflineData =
        mutableSetOf<String>() // Para evitar duplicados por peso+tiempo

    // Control para serializar operaciones BLE
    private var isReadingInProgress = false
    private val readingQueue = mutableListOf<() -> Unit>()
    private var readingTimeoutJob: kotlinx.coroutines.Job? = null

    private var bluetoothGatt: BluetoothGatt? = null
    private var fuelMeasurementCharacteristic: BluetoothGattCharacteristic? = null
    private var inclinationCharacteristic: BluetoothGattCharacteristic? = null
    private var offlineCharacteristic: BluetoothGattCharacteristic? = null

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(TAG, "🔄 onConnectionStateChange - status: $status, newState: $newState")
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "✅ Conectado al sensor CamperGas")
                    _connectionState.value = true
                    // Descubrir servicios solo si tenemos permisos
                    if (bleManager.hasBluetoothConnectPermission()) {
                        @SuppressLint("MissingPermission")
                        gatt?.discoverServices()
                    } else {
                        Log.e(TAG, "❌ No hay permisos para descubrir servicios")
                        disconnect()
                    }
                }

                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "❌ Callback: Desconectado from sensor CamperGas (status: $status)")

                    // Only update if we have not forced disconnection yet
                    if (_connectionState.value) {
                        Log.d(TAG, "❌ Actualizando estado de conexión desde callback")
                        _connectionState.value = false
                    } else {
                        Log.d(TAG, "❌ Estado ya había sido actualizado manualmente")
                    }

                    _isLoadingHistory.value = false
                    // Stop periodic reading on disconnect
                    stopPeriodicDataReading()
                    cleanup()

                    // If disconnection was unexpected (status != 0), log it
                    if (status != BluetoothGatt.GATT_SUCCESS) {
                        Log.w(TAG, "⚠️ Desconexión inesperada - status: $status")
                    }
                }

                BluetoothProfile.STATE_CONNECTING -> {
                    Log.d(TAG, "🔄 Conectando al sensor CamperGas...")
                }

                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.d(TAG, "🔄 Desconectando from sensor CamperGas...")
                    // Stop periodic reading when starting disconnection
                    stopPeriodicDataReading()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Servicios descubiertos")

                // Buscar el servicio from sensor CamperGas
                val sensorService =
                    gatt?.getService(UUID.fromString(CamperGasUuids.SENSOR_SERVICE_UUID))

                if (sensorService != null) {
                    Log.d(TAG, "Servicio CamperGas encontrado")

                    // Get all characteristics
                    setupCharacteristics(sensorService)
                } else {
                    Log.e(TAG, "Servicio CamperGas no encontrado")
                    listAvailableServices(gatt)
                }
            } else {
                Log.e(TAG, "Error al descubrir servicios: $status")
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            // Cancelar el timeout ya que recibimos respuesta
            readingTimeoutJob?.cancel()

            // Marcar que ya no hay una lectura en progreso
            isReadingInProgress = false

            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid.toString().lowercase()) {
                    CamperGasUuids.WEIGHT_CHARACTERISTIC_UUID.lowercase() -> {
                        processFuelMeasurementData(value)
                    }

                    CamperGasUuids.INCLINATION_CHARACTERISTIC_UUID.lowercase() -> {
                        processInclinationData(value)
                    }

                    CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID.lowercase() -> {
                        processOfflineData(value)
                    }
                }
            } else {
                Log.e(TAG, "Error al leer característica ${characteristic.uuid}: $status")
                if (characteristic.uuid.toString()
                        .lowercase() == CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID.lowercase()
                ) {
                    _isLoadingHistory.value = false
                }
            }

            // Procesar el siguiente elemento en la cola
            processNextReadingInQueue()
        }
    }

    private fun setupCharacteristics(service: BluetoothGattService) {
        // Verify permissions before setting up characteristics
        if (!bleManager.hasBluetoothConnectPermission()) {
            Log.e(TAG, "No hay permisos para configurar características")
            return
        }

        // Configure fuel measurement characteristic (READ-only)
        fuelMeasurementCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.WEIGHT_CHARACTERISTIC_UUID)
        )
        if (fuelMeasurementCharacteristic != null) {
            Log.d(TAG, "Característica de medición de combustible encontrada (READ-only)")
        } else {
            Log.w(TAG, "Característica de medición de combustible no encontrada")
        }

        // Configure inclination characteristic (READ-only)
        inclinationCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.INCLINATION_CHARACTERISTIC_UUID)
        )
        if (inclinationCharacteristic != null) {
            Log.d(TAG, "Característica de inclinación encontrada (READ-only)")
            Log.d(TAG, "UUID inclinación: ${inclinationCharacteristic?.uuid}")
            Log.d(TAG, "Propiedades inclinación: ${inclinationCharacteristic?.properties}")
        } else {
            Log.w(TAG, "Característica de inclinación no encontrada")
            Log.w(TAG, "UUID buscado: ${CamperGasUuids.INCLINATION_CHARACTERISTIC_UUID}")
        }

        // Configure offline characteristic (READ-only)
        offlineCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID)
        )
        if (offlineCharacteristic != null) {
            Log.d(TAG, "Característica offline encontrada (READ-only)")
            // Start automatic offline data reading on connect
            Log.d(TAG, "Iniciando lectura automática de datos offline...")
            startAutomaticOfflineDataReading()
        } else {
            Log.w(TAG, "Característica offline no encontrada")
        }

        // Start periodic real-time data reading
        loadConfigurationAndStartReading()
    }

    /**
     * Carga la configuración guardada e inicia la lectura periódica
     */
    private fun loadConfigurationAndStartReading() {
        serviceScope.launch {
            try {
                // Load configuration from preferences
                val weightInterval = preferencesDataStore.weightReadInterval.first()
                val inclinationInterval = preferencesDataStore.inclinationReadInterval.first()

                // Apply configuration
                configureReadingIntervals(weightInterval, inclinationInterval)

                // Start periodic reading
                startPeriodicDataReading()
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar configuración: ${e.message}")
                // Usar valores por defecto si falla
                configureReadingIntervals(5000L, 5000L)
                startPeriodicDataReading()
            }
        }
    }

    // Variables para control de lectura periódica
    private var isPeriodicReadingActive = false
    private var periodicReadingJob: kotlinx.coroutines.Job? = null
    private var lastWeightReadTime = 0L
    private var lastInclinationReadTime = 0L

    // Intervalos configurables (por defecto 5 segundos)
    private var weightReadInterval = 5000L // 5 segundos entre lecturas de peso
    private var inclinationReadInterval = 5000L // 5 segundos entre lecturas de inclinación

    /**
     * Configura los intervalos de lectura para peso e inclinación
     * @param weightIntervalMs Intervalo en milisegundos para lectura de peso
     * @param inclinationIntervalMs Intervalo en milisegundos para lectura de inclinación
     */
    fun configureReadingIntervals(weightIntervalMs: Long, inclinationIntervalMs: Long) {
        val oldWeightInterval = weightReadInterval
        val oldInclinationInterval = inclinationReadInterval

        weightReadInterval = weightIntervalMs
        inclinationReadInterval = inclinationIntervalMs

        Log.d(
            TAG,
            "Intervalos configurados - Peso: ${weightIntervalMs}ms, Inclinación: ${inclinationIntervalMs}ms"
        )

        // Si los intervalos cambiaron y estamos conectados, reiniciar la lectura periódica
        if ((oldWeightInterval != weightIntervalMs || oldInclinationInterval != inclinationIntervalMs)
            && isConnected()
        ) {
            Log.d(TAG, "Reiniciando lectura periódica con nuevos intervalos...")
            restartPeriodicReading()
        }
    }

    /**
     * Reinicia la lectura periódica con los nuevos intervalos
     */
    private fun restartPeriodicReading() {
        if (isPeriodicReadingActive) {
            Log.d(TAG, "Deteniendo lectura periódica actual...")
            stopPeriodicDataReading()

            // Short pause before restarting
            serviceScope.launch {
                delay(500)
                if (isConnected()) {
                    Log.d(TAG, "Reiniciando lectura periódica con nuevos intervalos...")
                    startPeriodicDataReading()
                }
            }
        }
    }

    /**
     * Obtiene el intervalo actual de lectura de peso
     */
    fun getWeightReadInterval(): Long = weightReadInterval

    /**
     * Obtiene el intervalo actual de lectura de inclinación
     */
    fun getInclinationReadInterval(): Long = inclinationReadInterval

    /**
     * Reinicia la lectura periódica (útil when cambian los intervalos)
     */
    fun restartPeriodicDataReading() {
        if (isConnected()) {
            Log.d(TAG, "Reiniciando lectura periódica por solicitud externa...")
            restartPeriodicReading()
        } else {
            Log.w(TAG, "No se puede reiniciar la lectura periódica: no hay conexión activa")
        }
    }

    /**
     * Inicia la lectura periódica de datos en tiempo real
     */
    private fun startPeriodicDataReading() {
        if (isPeriodicReadingActive) {
            Log.d(TAG, "La lectura periódica ya está activa")
            return
        }

        isPeriodicReadingActive = true
        periodicReadingJob = serviceScope.launch {
            Log.d(TAG, "🔄 Iniciando lectura periódica de datos en tiempo real...")
            Log.d(
                TAG,
                "📊 Intervalo peso: ${weightReadInterval}ms, Intervalo inclinación: ${inclinationReadInterval}ms"
            )

            while (isPeriodicReadingActive && isConnected()) {
                try {
                    val currentTime = System.currentTimeMillis()

                    // Read weight if more than configured interval has passed since last reading
                    if (currentTime - lastWeightReadTime > weightReadInterval) {
                        readWeightData()
                        lastWeightReadTime = currentTime
                    }

                    // Wait a bit more before reading inclination to avoid BLE conflicts
                    delay(500)

                    // Read inclination if more than configured interval has passed since last reading
                    if (currentTime - lastInclinationReadTime > inclinationReadInterval) {
                        readInclinationData()
                        lastInclinationReadTime = currentTime
                    }

                    // Pause between verification cycles (1000ms)
                    delay(1000)
                } catch (e: Exception) {
                    Log.e(TAG, "Error en lectura periódica: ${e.message}")
                    delay(2000) // Longer pause in case of error
                }
            }

            Log.d(TAG, "Lectura periódica de datos en tiempo real finalizada")
        }
    }

    /**
     * Detiene la lectura periódica de datos en tiempo real
     */
    private fun stopPeriodicDataReading() {
        isPeriodicReadingActive = false
        periodicReadingJob?.cancel()
        periodicReadingJob = null
        Log.d(TAG, "Lectura periódica de datos en tiempo real detenida")
    }

    /**
     * Lee datos de peso desof the característica
     */
    private fun readWeightData() {
        queueBleRead {
            fuelMeasurementCharacteristic?.let { characteristic ->
                bluetoothGatt?.let { gatt ->
                    if (bleManager.hasBluetoothConnectPermission()) {
                        @SuppressLint("MissingPermission")
                        val success = gatt.readCharacteristic(characteristic)
                        if (!success) {
                            Log.e(TAG, "Error al leer datos de peso")
                            isReadingInProgress = false
                            processNextReadingInQueue()
                        }
                    } else {
                        Log.e(TAG, "No hay permisos para leer datos de peso")
                        isReadingInProgress = false
                        processNextReadingInQueue()
                    }
                }
            } ?: run {
                Log.e(TAG, "Característica de peso no disponible")
                isReadingInProgress = false
                processNextReadingInQueue()
            }
        }
    }

    /**
     * Lee datos de inclinación desof the característica
     */
    private fun readInclinationData() {
        queueBleRead {
            inclinationCharacteristic?.let { characteristic ->
                bluetoothGatt?.let { gatt ->
                    if (bleManager.hasBluetoothConnectPermission()) {
                        Log.d(TAG, "Intentando leer datos de inclinación...")
                        @SuppressLint("MissingPermission")
                        val success = gatt.readCharacteristic(characteristic)
                        if (!success) {
                            Log.e(
                                TAG,
                                "Error al leer datos de inclinación - readCharacteristic() retornó false"
                            )
                            isReadingInProgress = false
                            processNextReadingInQueue()
                        } else {
                            Log.d(TAG, "Lectura de inclinación iniciada correctamente")
                        }
                    } else {
                        Log.e(TAG, "No hay permisos para leer datos de inclinación")
                        isReadingInProgress = false
                        processNextReadingInQueue()
                    }
                }
            } ?: run {
                Log.e(TAG, "Característica de inclinación no disponible")
                isReadingInProgress = false
                processNextReadingInQueue()
            }
        }
    }

    /**
     * Función pública para leer datos de peso bajo demanda
     */
    fun readWeightDataOnDemand() {
        if (!isConnected()) {
            Log.w(TAG, "No hay conexión activa para leer datos de peso")
            return
        }
        readWeightData()
    }

    /**
     * Función pública para leer datos de inclinación bajo demanda
     */
    fun readInclinationDataOnDemand() {
        if (!isConnected()) {
            Log.w(TAG, "No hay conexión activa para leer datos de inclinación")
            return
        }
        readInclinationData()
    }

    /**
     * Añade una lectura BLE a la cola para evitar lecturas concurrentes
     */
    private fun queueBleRead(readOperation: () -> Unit) {
        synchronized(readingQueue) {
            readingQueue.add(readOperation)
            if (!isReadingInProgress) {
                processNextReadingInQueue()
            }
        }
    }

    /**
     * Procesa el siguiente elemento en la cola de lecturas BLE
     */
    private fun processNextReadingInQueue() {
        synchronized(readingQueue) {
            if (readingQueue.isNotEmpty() && !isReadingInProgress) {
                isReadingInProgress = true
                val nextRead = readingQueue.removeAt(0)

                // Configure timeout for read operation
                readingTimeoutJob = serviceScope.launch {
                    delay(5000) // Timeout de 5 segundos
                    if (isReadingInProgress) {
                        Log.w(TAG, "Timeout en lectura BLE, continuando con la siguiente")
                        isReadingInProgress = false
                        processNextReadingInQueue()
                    }
                }

                nextRead()
            }
        }
    }

    private fun listAvailableServices(gatt: BluetoothGatt?) {
        // Verificar permisos antes de listar servicios
        if (!bleManager.hasBluetoothConnectPermission()) {
            Log.e(TAG, "No hay permisos para listar servicios")
            return
        }

        gatt?.services?.forEach { service ->
            Log.d(TAG, "Servicio disponible: ${service.uuid}")
            service.characteristics.forEach { characteristic ->
                Log.d(TAG, "  - Característica: ${characteristic.uuid}")
            }
        }
    }

    /**
     * Procesa datos de medición EN TIEMPO REAL from sensor
     * This data comes from the WEIGHT_CHARACTERISTIC_UUID characteristic
     * y se marcan automáticamente como isHistorical = false
     */
    private fun processFuelMeasurementData(data: ByteArray) {
        try {
            val jsonString = String(data, Charsets.UTF_8)
            Log.d(TAG, "Datos de medición de combustible recibidos: $jsonString")

            // Parsear JSON: {"w":12.5}
            val jsonObject = JSONObject(jsonString)
            val totalWeight = jsonObject.getDouble("w").toFloat()

            // Guardar medición de combustible EN TIEMPO REAL en la base de datos
            // Estos datos provienen de WEIGHT_CHARACTERISTIC_UUID y se marcan como isHistorical = false
            serviceScope.launch {
                try {
                    val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
                        totalWeight = totalWeight,
                        timestamp = System.currentTimeMillis()
                    )

                    result.fold(
                        onSuccess = { saveResult ->
                            if (saveResult.processed) {
                                Log.d(
                                    TAG,
                                    "Medición de combustible guardada (ID: ${saveResult.measurementId}) - ${saveResult.reason}"
                                )
                            } else {
                                Log.d(
                                    TAG,
                                    "Medición omitida - ${saveResult.reason}"
                                )
                            }

                            // Siempre actualizar el StateFlow con los datos de combustible
                            // independientemente de si se guardó o no en la base de datos
                            val activeCylinder = getActiveCylinderUseCase.getActiveCylinderSync()
                            if (activeCylinder != null) {
                                val fuelKilograms = maxOf(0f, totalWeight - activeCylinder.tare)
                                val fuelPercentage = if (activeCylinder.capacity > 0) {
                                    (fuelKilograms / activeCylinder.capacity * 100).coerceIn(
                                        0f,
                                        100f
                                    )
                                } else {
                                    0f
                                }

                                val fuelMeasurement = FuelMeasurement(
                                    id = if (saveResult.processed) saveResult.measurementId else -1L,
                                    cylinderId = activeCylinder.id,
                                    cylinderName = activeCylinder.name,
                                    timestamp = System.currentTimeMillis(),
                                    fuelKilograms = fuelKilograms,
                                    fuelPercentage = fuelPercentage,
                                    totalWeight = totalWeight,
                                    isCalibrated = true,
                                    isHistorical = false
                                )

                                _fuelMeasurementData.value = fuelMeasurement
                                _fuelData.value = fuelMeasurement
                                Log.d(
                                    TAG,
                                    "Combustible calculado: ${fuelKilograms}kg (${fuelPercentage}%)"
                                )
                            }
                        },
                        onFailure = { error ->
                            Log.e(TAG, "Error al guardar medición de combustible: ${error.message}")
                        }
                    )
                } catch (e: Exception) {
                    Log.e(TAG, "Error al procesar medición de combustible: ${e.message}")
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos de medición de combustible: ${e.message}")
        }
    }

    private fun processInclinationData(data: ByteArray) {
        try {
            val jsonString = String(data, Charsets.UTF_8)
            Log.d(TAG, "Datos de inclinación recibidos: $jsonString")

            // Parsear JSON: {"p":15.2,"r":-3.1}
            val jsonObject = JSONObject(jsonString)
            val pitch = jsonObject.getDouble("p").toFloat()
            val roll = jsonObject.getDouble("r").toFloat()

            val inclination = Inclination(
                pitch = pitch,
                roll = roll,
                timestamp = System.currentTimeMillis()
            )

            _inclinationData.value = inclination
            Log.d(
                TAG,
                "Inclinación actualizada: pitch=${inclination.pitch}°, roll=${inclination.roll}°"
            )

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos de inclinación: ${e.message}")
            Log.e(TAG, "Datos raw recibidos: ${data.contentToString()}")
        }
    }

    /**
     * Procesa datos OFFLINE/HISTÓRICOS from sensor
     * Estos datos provienen of the característica OFFLINE_CHARACTERISTIC_UUID
     * y se marcan automáticamente como isHistorical = true
     */
    private fun processOfflineData(data: ByteArray) {
        try {
            val jsonString = String(data, Charsets.UTF_8)
            Log.d(TAG, "Datos offline recibidos (lote ${offlineDataCount + 1}): $jsonString")

            /*
             * IMPORTANTE: Los datos offline vienen con timestamps relativos
             * El campo "t" contiene los MILISEGUNDOS transcurridos desde que se tomó la medición
             * Debemos calcular el timestamp absoluto restando estos milisegundos del momento actual
             * Ejemplo: Si "t":300000, significa que la medición se tomó hace 5 minutos (300000 ms)
             */

            // Verify if data is empty, is "0", or indicates end of data
            if (jsonString.isBlank() ||
                jsonString == "[]" ||
                jsonString == "{}" ||
                jsonString.equals("END", ignoreCase = true) ||
                jsonString.trim() == "0"
            ) {
                Log.d(TAG, "Fin de datos offline detectado (datos vacíos o 0)")
                finishOfflineDataReading()
                return
            }

            // Parsear JSON array: [{"w":25.1,"t":1234567890},{"w":25.3,"t":1234567900}]
            val jsonArray = JSONArray(jsonString)

            // If array is empty, we are done
            if (jsonArray.length() == 0) {
                Log.d(TAG, "Array vacío recibido - fin de datos offline")
                finishOfflineDataReading()
                return
            }

            val batchHistoricalMeasurements = mutableListOf<Pair<Float, Long>>()

            // Procesar en corrutina para manejar operaciones de base de datos
            serviceScope.launch {
                try {
                    // Verificar si todos los datos del lote ya han sido procesados
                    var allDataAlreadyProcessed = true

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val weightValue = jsonObject.getDouble("w").toFloat()
                        val millisecondsAgo = jsonObject.getLong("t")

                        // Create unique key for this data (weight + relative time in milliseconds)
                        val dataKey = "${weightValue}_${millisecondsAgo}"

                        if (!processedOfflineData.contains(dataKey)) {
                            allDataAlreadyProcessed = false
                            break
                        }
                    }

                    // Si todos los datos ya fueron procesados, ignorar este lote
                    if (allDataAlreadyProcessed) {
                        Log.d(TAG, "🛑 Lote completo ya procesado - ignorando datos duplicados")
                        return@launch
                    }

                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val weightValue = jsonObject.getDouble("w").toFloat()
                        val millisecondsAgo =
                            jsonObject.getLong("t") // MILLISECONDS elapsed since measurement was taken

                        // Create unique key for this data
                        val dataKey = "${weightValue}_${millisecondsAgo}"

                        // Si ya procesamos este dato exacto, saltarlo
                        if (processedOfflineData.contains(dataKey)) {
                            Log.d(
                                TAG,
                                "⏭️ Dato duplicado ignorado: ${weightValue}kg hace ${millisecondsAgo}ms"
                            )
                            continue
                        }

                        // Marcar este dato como procesado
                        processedOfflineData.add(dataKey)

                        // Calcular el timestamp real de when tomó la medición
                        val actualTimestamp = calculateHistoricalTimestamp(millisecondsAgo)

                        Log.d(
                            TAG,
                            "📊 Procesando medición histórica: ${weightValue}kg tomada hace ${millisecondsAgo}ms"
                        )
                        Log.d(
                            TAG, "🕒 Timestamp calculado: $actualTimestamp (${
                                java.text.SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm:ss",
                                    Locale.getDefault()
                                ).format(
                                    Date(actualTimestamp)
                                )
                            })"
                        )

                        batchHistoricalMeasurements.add(Pair(weightValue, actualTimestamp))
                    }

                    // Solo procesar si tenemos datos nuevos para guardar
                    if (batchHistoricalMeasurements.isNotEmpty()) {
                        offlineDataCount++
                        Log.d(
                            TAG,
                            "📦 Lote $offlineDataCount procesado: ${batchHistoricalMeasurements.size} registros históricos"
                        )
                        Log.d(
                            TAG,
                            "📈 Total acumulado: ${allHistoryData.size} mediciones de combustible"
                        )

                        // Save historical data from current batch to database
                        try {
                            val activeCylinder = getActiveCylinderUseCase.getActiveCylinderSync()
                            if (activeCylinder != null) {
                                // Save HISTORICAL/OFFLINE data to database
                                // Estos datos provienen de OFFLINE_CHARACTERISTIC_UUID y se marcan como isHistorical = true
                                val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
                                    cylinderId = activeCylinder.id,
                                    weightMeasurements = batchHistoricalMeasurements
                                )

                                result.fold(
                                    onSuccess = { savedCount ->
                                        Log.d(
                                            TAG,
                                            "✅ Lote $offlineDataCount guardado: $savedCount mediciones de combustible"
                                        )

                                        // Crear FuelMeasurements para la UI
                                        val fuelMeasurements =
                                            batchHistoricalMeasurements.map { (weightValue, timestamp) ->
                                                val fuelKilograms =
                                                    maxOf(0f, weightValue - activeCylinder.tare)
                                                val fuelPercentage =
                                                    if (activeCylinder.capacity > 0) {
                                                        (fuelKilograms / activeCylinder.capacity * 100).coerceIn(
                                                            0f,
                                                            100f
                                                        )
                                                    } else {
                                                        0f
                                                    }

                                                FuelMeasurement(
                                                    cylinderId = activeCylinder.id,
                                                    cylinderName = activeCylinder.name,
                                                    timestamp = timestamp,
                                                    fuelKilograms = fuelKilograms,
                                                    fuelPercentage = fuelPercentage,
                                                    totalWeight = weightValue,
                                                    isCalibrated = true,
                                                    isHistorical = true
                                                )
                                            }

                                        allHistoryData.addAll(fuelMeasurements)

                                        // Actualizar UI con todos los datos acumulados hasta ahora
                                        val sortedHistoryData =
                                            allHistoryData.sortedBy { it.timestamp }
                                        _historyData.value = sortedHistoryData
                                    },
                                    onFailure = { error ->
                                        Log.e(
                                            TAG,
                                            "❌ Error al guardar lote ${offlineDataCount}: ${error.message}"
                                        )
                                    }
                                )
                            } else {
                                Log.w(TAG, "No hay bombona activa - datos históricos NO guardados")
                            }
                        } catch (e: Exception) {
                            Log.e(
                                TAG,
                                "❌ Error al procesar guardado del lote ${offlineDataCount}: ${e.message}"
                            )
                        }

                        // Continue reading more data if we are in continuous reading mode and there are no duplicates
                        if (isReadingOfflineData) {
                            // Make small pause before requesting more data
                            delay(100) // 100ms de pausa entre lecturas
                            continueOfflineDataReading()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al verificar timestamps duplicados: ${e.message}")
                    finishOfflineDataReading()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos offline: ${e.message}")
            finishOfflineDataReading()
        }
    }

    fun connect(deviceAddress: String) {
        try {
            // Verificar permisos antes de conectar
            if (!bleManager.hasBluetoothConnectPermission()) {
                Log.e(TAG, "No hay permisos para conectar dispositivos BLE")
                return
            }

            val device = bleManager.bluetoothAdapter?.getRemoteDevice(deviceAddress)
            device?.let {
                Log.d(TAG, "Conectando al sensor CamperGas: $deviceAddress")

                // Limpiar estado anterior si existe
                cleanup()

                @SuppressLint("MissingPermission")
                bluetoothGatt = it.connectGatt(context, false, gattCallback)
            } ?: run {
                Log.e(TAG, "No se pudo obtener el dispositivo remoto")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al conectar: ${e.message}")
        }
    }

    fun disconnect() {
        Log.d(TAG, "🔌 Iniciando desconexión from sensor CamperGas")
        Log.d(TAG, "🔌 Estado actual de conexión: ${_connectionState.value}")

        // Stop periodic reading
        stopPeriodicDataReading()

        // IMPORTANTE: Actualizar el estado inmediatamente
        // No esperar al callback porque a veces no se ejecuta
        _connectionState.value = false
        Log.d(TAG, "🔌 Estado de conexión actualizado a: false")

        bluetoothGatt?.let { gatt ->
            // Verificar permisos antes de desconectar
            if (bleManager.hasBluetoothConnectPermission()) {
                Log.d(TAG, "🔌 Desconectando GATT...")
                @SuppressLint("MissingPermission")
                gatt.disconnect()

                // Short pause before closing
                serviceScope.launch {
                    delay(100)
                    if (bleManager.hasBluetoothConnectPermission()) {
                        @SuppressLint("MissingPermission")
                        gatt.close()
                    }
                    cleanup()
                }

                Log.d(TAG, "🔌 GATT desconectado")
            } else {
                Log.w(TAG, "🔌 No hay permisos para desconectar, forzando limpieza")
                cleanup()
            }
        } ?: run {
            Log.w(TAG, "🔌 bluetoothGatt es null, limpiando recursos")
            cleanup()
        }

        Log.d(TAG, "🔌 Desconexión completada - Estado final: ${_connectionState.value}")
    }

    /**
     * Inicia la lectura automática de datos offline al conectar
     */
    private fun startAutomaticOfflineDataReading() {
        offlineCharacteristic?.let { characteristic ->
            bluetoothGatt?.let { gatt ->
                // Verify permissions before requesting historical data
                if (!bleManager.hasBluetoothConnectPermission()) {
                    Log.e(TAG, "No hay permisos para lectura automática de datos offline")
                    return
                }

                Log.d(TAG, "🔄 Iniciando lectura automática de datos offline al conectar...")

                // Inicializar la lectura continua de datos offline
                startOfflineDataReading()

                // Start first reading after a pause
                serviceScope.launch {
                    delay(500) // Pause to stabilize connection
                    if (isReadingOfflineData) {
                        Log.d(TAG, "Ejecutando primera lectura de datos offline...")
                        continueOfflineDataReading()
                    }
                }

            } ?: run {
                Log.e(TAG, "No hay conexión GATT disponible para lectura automática")
            }
        } ?: run {
            Log.w(TAG, "Característica offline no disponible para lectura automática")
        }
    }

    private fun startOfflineDataReading() {
        isReadingOfflineData = true
        offlineDataCount = 0
        allHistoryData.clear()
        processedOfflineData.clear() // Limpiar datos procesados anteriores
        _isLoadingHistory.value = true
        _historyData.value = emptyList()
        Log.d(TAG, "Iniciando lectura continua de datos offline")
    }

    private fun continueOfflineDataReading() {
        if (!isReadingOfflineData) {
            Log.d(TAG, "Lectura offline cancelada")
            return
        }

        offlineCharacteristic?.let { characteristic ->
            bluetoothGatt?.let { gatt ->
                if (bleManager.hasBluetoothConnectPermission()) {
                    queueBleRead {
                        @SuppressLint("MissingPermission")
                        val success = gatt.readCharacteristic(characteristic)
                        if (!success) {
                            Log.e(TAG, "Error al continuar lectura de datos históricos")
                            finishOfflineDataReading()
                            isReadingInProgress = false
                            processNextReadingInQueue()
                        }
                    }
                } else {
                    Log.e(TAG, "No hay permisos para continuar lectura")
                    finishOfflineDataReading()
                }
            } ?: run {
                Log.e(TAG, "No hay conexión GATT para continuar lectura")
                finishOfflineDataReading()
            }
        } ?: run {
            Log.e(TAG, "Característica offline no disponible para continuar")
            finishOfflineDataReading()
        }
    }

    private fun finishOfflineDataReading() {
        isReadingOfflineData = false
        _isLoadingHistory.value = false

        Log.d(TAG, "Lectura de datos offline completada")
        Log.d(TAG, "Total de lotes procesados: $offlineDataCount")
        Log.d(TAG, "Total de registros históricos obtenidos: ${allHistoryData.size}")

        // Ordenar todos los datos por timestamp final
        val sortedHistoryData = allHistoryData.sortedBy { it.timestamp }
        _historyData.value = sortedHistoryData

        if (allHistoryData.isNotEmpty()) {
            Log.d(TAG, "✅ Sincronización offline completada con éxito")
            Log.d(
                TAG,
                "📊 Rango de datos: ${allHistoryData.minOfOrNull { it.timestamp }} - ${allHistoryData.maxOfOrNull { it.timestamp }}"
            )
            Log.d(TAG, "🎯 Datos offline sincronizados automáticamente al conectar")
        } else {
            Log.d(TAG, "ℹ️ No se encontraron datos offline in the sensor")
        }
    }

    fun stopOfflineDataReading() {
        if (isReadingOfflineData) {
            Log.d(TAG, "Deteniendo lectura continua de datos offline...")
            finishOfflineDataReading()
        }
    }

    private fun cleanup() {
        Log.d(TAG, "🧹 Iniciando limpieza de recursos BLE")

        // Stop offline reading if in progress
        stopOfflineDataReading()

        // Stop periodic reading if in progress
        stopPeriodicDataReading()

        bluetoothGatt = null
        fuelMeasurementCharacteristic = null
        inclinationCharacteristic = null
        offlineCharacteristic = null

        // Limpiar estado of the cola de lecturas BLE
        synchronized(readingQueue) {
            readingQueue.clear()
            isReadingInProgress = false
        }
        readingTimeoutJob?.cancel()
        readingTimeoutJob = null

        // Limpiar datos when desconecta
        _fuelMeasurementData.value = null
        _fuelData.value = null
        _inclinationData.value = null
        processedOfflineData.clear() // Limpiar datos procesados al desconectar

        Log.d(TAG, "🧹 Limpieza completada - Estado conexión: ${_connectionState.value}")
    }

    fun isConnected(): Boolean = _connectionState.value

    /**
     * Fuerza la verificación y lectura de datos offline si hay conexión activa
     */
    fun ensureOfflineDataReading() {
        if (isConnected() && !isReadingOfflineData) {
            Log.d(TAG, "🔍 Verificando y forzando lectura de datos offline...")
            startAutomaticOfflineDataReading()
        } else if (isReadingOfflineData) {
            Log.d(TAG, "ℹ️ Lectura de datos offline ya en progreso")
        } else {
            Log.w(TAG, "⚠️ No hay conexión activa para leer datos offline")
        }
    }

    /**
     * Calcula el timestamp real de una medición basándose en cuántos milisegundos han pasado
     * desde que se tomó la medición hasta ahora
     */
    private fun calculateHistoricalTimestamp(millisecondsAgo: Long): Long {
        return System.currentTimeMillis() - millisecondsAgo
    }
}
