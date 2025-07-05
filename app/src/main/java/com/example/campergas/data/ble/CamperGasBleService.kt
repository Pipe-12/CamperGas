package com.example.campergas.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.campergas.domain.model.CamperGasUuids
import com.example.campergas.domain.model.Weight
import com.example.campergas.domain.model.Inclination
import com.example.campergas.domain.model.FuelMeasurement
import com.example.campergas.domain.usecase.GetActiveCylinderUseCase
import com.example.campergas.domain.usecase.SaveFuelMeasurementUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Servicio BLE unificado para manejar todas las características del sensor CamperGas
 * El sensor tiene un solo servicio con tres características:
 * - Weight: datos de peso en tiempo real
 * - Inclination: datos de inclinación en tiempo real
 * - Offline: datos históricos en lotes
 */
@Singleton
class CamperGasBleService @Inject constructor(
    private val bleManager: BleManager,
    private val saveFuelMeasurementUseCase: SaveFuelMeasurementUseCase,
    private val getActiveCylinderUseCase: GetActiveCylinderUseCase,
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "CamperGasBleService"
    }
    
    // Scope para operaciones en segundo plano
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Estados de conexión
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState
    
    // Datos de peso en tiempo real (mantener para compatibilidad con sensores)
    private val _weightData = MutableStateFlow<Weight?>(null)
    val weightData: StateFlow<Weight?> = _weightData
    
    // Datos de combustible calculados
    private val _fuelData = MutableStateFlow<FuelMeasurement?>(null)
    val fuelData: StateFlow<FuelMeasurement?> = _fuelData
    
    // Datos de inclinación en tiempo real
    private val _inclinationData = MutableStateFlow<Inclination?>(null)
    val inclinationData: StateFlow<Inclination?> = _inclinationData
    
    // Datos históricos
    private val _historyData = MutableStateFlow<List<FuelMeasurement>>(emptyList())
    val historyData: StateFlow<List<FuelMeasurement>> = _historyData
    
    // Estado de carga de datos históricos
    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory
    
    // Control para lectura continua de datos offline
    private var isReadingOfflineData = false
    private var offlineDataCount = 0
    private val allHistoryData = mutableListOf<FuelMeasurement>()
    
    private var bluetoothGatt: BluetoothGatt? = null
    private var weightCharacteristic: BluetoothGattCharacteristic? = null
    private var inclinationCharacteristic: BluetoothGattCharacteristic? = null
    private var offlineCharacteristic: BluetoothGattCharacteristic? = null
    
    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.d(TAG, "Conectado al sensor CamperGas")
                    _connectionState.value = true
                    // Descubrir servicios solo si tenemos permisos
                    if (bleManager.hasBluetoothConnectPermission()) {
                        @SuppressLint("MissingPermission")
                        gatt?.discoverServices()
                    } else {
                        Log.e(TAG, "No hay permisos para descubrir servicios")
                        disconnect()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.d(TAG, "Desconectado del sensor CamperGas")
                    _connectionState.value = false
                    _isLoadingHistory.value = false
                    cleanup()
                }
                BluetoothProfile.STATE_CONNECTING -> {
                    Log.d(TAG, "Conectando al sensor CamperGas...")
                }
                BluetoothProfile.STATE_DISCONNECTING -> {
                    Log.d(TAG, "Desconectando del sensor CamperGas...")
                }
            }
        }
        
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Servicios descubiertos")
                
                // Buscar el servicio del sensor CamperGas
                val sensorService = gatt?.getService(UUID.fromString(CamperGasUuids.SENSOR_SERVICE_UUID))
                
                if (sensorService != null) {
                    Log.d(TAG, "Servicio CamperGas encontrado")
                    
                    // Obtener todas las características
                    setupCharacteristics(sensorService, gatt)
                } else {
                    Log.e(TAG, "Servicio CamperGas no encontrado")
                    listAvailableServices(gatt)
                }
            } else {
                Log.e(TAG, "Error al descubrir servicios: $status")
            }
        }
        
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            when (characteristic.uuid.toString().lowercase()) {
                CamperGasUuids.WEIGHT_CHARACTERISTIC_UUID.lowercase() -> {
                    processWeightData(value)
                }
                CamperGasUuids.INCLINATION_CHARACTERISTIC_UUID.lowercase() -> {
                    processInclinationData(value)
                }
                CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID.lowercase() -> {
                    processOfflineData(value)
                }
            }
        }
        
        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (characteristic.uuid.toString().lowercase()) {
                    CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID.lowercase() -> {
                        processOfflineData(value)
                    }
                }
            } else {
                Log.e(TAG, "Error al leer característica: $status")
                _isLoadingHistory.value = false
            }
        }
        
        override fun onDescriptorWrite(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Descriptor escrito correctamente para ${descriptor.characteristic?.uuid}")
                
                // Si se escribió correctamente el descriptor de la característica offline, iniciar lectura
                if (descriptor.characteristic?.uuid?.toString()?.lowercase() == 
                    CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID.lowercase()) {
                    Log.d(TAG, "Descriptor offline configurado, iniciando lectura de datos...")
                    serviceScope.launch {
                        delay(200) // Pequeña pausa para estabilizar
                        continueOfflineDataReading()
                    }
                }
            } else {
                Log.e(TAG, "Error al escribir descriptor: $status")
            }
        }
    }
    
    private fun setupCharacteristics(service: BluetoothGattService, gatt: BluetoothGatt) {
        // Verificar permisos antes de configurar características
        if (!bleManager.hasBluetoothConnectPermission()) {
            Log.e(TAG, "No hay permisos para configurar características")
            return
        }
        
        // Configurar característica de peso
        weightCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.WEIGHT_CHARACTERISTIC_UUID)
        )
        if (weightCharacteristic != null) {
            Log.d(TAG, "Característica de peso encontrada")
            enableNotifications(gatt, weightCharacteristic!!)
        } else {
            Log.w(TAG, "Característica de peso no encontrada")
        }
        
        // Configurar característica de inclinación
        inclinationCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.INCLINATION_CHARACTERISTIC_UUID)
        )
        if (inclinationCharacteristic != null) {
            Log.d(TAG, "Característica de inclinación encontrada")
            enableNotifications(gatt, inclinationCharacteristic!!)
        } else {
            Log.w(TAG, "Característica de inclinación no encontrada")
        }
        
        // Configurar característica offline
        offlineCharacteristic = service.getCharacteristic(
            UUID.fromString(CamperGasUuids.OFFLINE_CHARACTERISTIC_UUID)
        )
        if (offlineCharacteristic != null) {
            Log.d(TAG, "Característica offline encontrada")
            // Iniciar lectura automática de datos offline al conectar
            Log.d(TAG, "Iniciando lectura automática de datos offline...")
            startAutomaticOfflineDataReading()
        } else {
            Log.w(TAG, "Característica offline no encontrada")
        }
    }
    
    private fun enableNotifications(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        // Verificar permisos antes de habilitar notificaciones
        if (!bleManager.hasBluetoothConnectPermission()) {
            Log.e(TAG, "No hay permisos para habilitar notificaciones")
            return
        }
        
        @SuppressLint("MissingPermission")
        val success = gatt.setCharacteristicNotification(characteristic, true)
        
        if (success) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )
            
            descriptor?.let {
                // Usar el nuevo método writeDescriptor con ByteArray (API 33+)
                @SuppressLint("MissingPermission")
                val result = gatt.writeDescriptor(
                    it, 
                    BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                )
                if (result == BluetoothGatt.GATT_SUCCESS) {
                    Log.d(TAG, "Habilitando notificaciones para ${characteristic.uuid}")
                } else {
                    Log.e(TAG, "Error al escribir descriptor para ${characteristic.uuid}")
                }
            }
        } else {
            Log.e(TAG, "Error al configurar notificaciones para ${characteristic.uuid}")
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
    
    private fun processWeightData(data: ByteArray) {
        try {
            val jsonString = String(data, Charsets.UTF_8)
            Log.d(TAG, "Datos de peso recibidos: $jsonString")
            
            // Parsear JSON: {"w":12.5}
            val jsonObject = JSONObject(jsonString)
            val weightValue = jsonObject.getDouble("w").toFloat()
            
            val weight = Weight(
                value = weightValue,
                timestamp = System.currentTimeMillis(),
                unit = "kg"
            )
            
            // Mantener compatibilidad con el StateFlow de peso
            _weightData.value = weight
            Log.d(TAG, "Peso actualizado: ${weight.value} kg")
            
            // Guardar medición de combustible en la base de datos
            serviceScope.launch {
                try {
                    val result = saveFuelMeasurementUseCase.saveRealTimeMeasurement(
                        totalWeight = weightValue,
                        timestamp = weight.timestamp
                    )
                    
                    result.fold(
                        onSuccess = { saveResult ->
                            Log.d(TAG, "Medición de combustible guardada (ID: ${saveResult.measurementId})")
                            
                            // Actualizar el StateFlow con los datos de combustible
                            val activeCylinder = getActiveCylinderUseCase.getActiveCylinderSync()
                            if (activeCylinder != null) {
                                val fuelKilograms = maxOf(0f, weightValue - activeCylinder.tare)
                                val fuelPercentage = if (activeCylinder.capacity > 0) {
                                    (fuelKilograms / activeCylinder.capacity * 100).coerceIn(0f, 100f)
                                } else {
                                    0f
                                }
                                
                                val fuelMeasurement = FuelMeasurement(
                                    id = saveResult.measurementId,
                                    cylinderId = activeCylinder.id,
                                    cylinderName = activeCylinder.name,
                                    timestamp = weight.timestamp,
                                    fuelKilograms = fuelKilograms,
                                    fuelPercentage = fuelPercentage,
                                    totalWeight = weightValue,
                                    isCalibrated = true,
                                    isHistorical = false
                                )
                                
                                _fuelData.value = fuelMeasurement
                                Log.d(TAG, "Combustible calculado: ${fuelKilograms}kg (${fuelPercentage}%)")
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
            Log.e(TAG, "Error al procesar datos de peso: ${e.message}")
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
            Log.d(TAG, "Inclinación actualizada: pitch=${inclination.pitch}°, roll=${inclination.roll}°")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos de inclinación: ${e.message}")
        }
    }
    
    private fun processOfflineData(data: ByteArray) {
        try {
            val jsonString = String(data, Charsets.UTF_8)
            Log.d(TAG, "Datos offline recibidos (lote ${offlineDataCount + 1}): $jsonString")
            
            /*
             * IMPORTANTE: Los datos offline vienen con timestamps relativos
             * El campo "t" contiene los SEGUNDOS transcurridos desde que se tomó la medición
             * Debemos calcular el timestamp absoluto restando estos segundos del momento actual
             * Ejemplo: Si "t":300, significa que la medición se tomó hace 5 minutos (300 segundos)
             */
            
            // Verificar si los datos están vacíos, son "0", o indican fin de datos
            if (jsonString.isBlank() || 
                jsonString == "[]" || 
                jsonString == "{}" || 
                jsonString.equals("END", ignoreCase = true) ||
                jsonString.trim() == "0") {
                Log.d(TAG, "Fin de datos offline detectado (datos vacíos o 0)")
                finishOfflineDataReading()
                return
            }
            
            // Parsear JSON array: [{"w":25.1,"t":1234567890},{"w":25.3,"t":1234567900}]
            val jsonArray = JSONArray(jsonString)
            
            // Si el array está vacío, hemos terminado
            if (jsonArray.length() == 0) {
                Log.d(TAG, "Array vacío recibido - fin de datos offline")
                finishOfflineDataReading()
                return
            }
            
            val batchHistoryWeights = mutableListOf<Weight>()
            val batchHistoricalMeasurements = mutableListOf<Pair<Float, Long>>()
            val batchFuelMeasurements = mutableListOf<FuelMeasurement>()
            var duplicateFound = false
            
            // Verificar timestamp duplicados antes de procesar el lote completo
            serviceScope.launch {
                try {
                    // Obtener el timestamp actual como referencia
                    val currentTimestamp = System.currentTimeMillis()
                    
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val secondsAgo = jsonObject.getLong("t") // Segundos transcurridos desde que se tomó la medición
                        
                        // Calcular el timestamp real de cuando se tomó la medición
                        val actualTimestamp = calculateHistoricalTimestamp(secondsAgo)
                        
                        // Verificar si este timestamp ya existe en la base de datos
                        if (saveFuelMeasurementUseCase.doesTimestampExist(actualTimestamp)) {
                            Log.d(TAG, "🛑 Timestamp duplicado detectado: $actualTimestamp - deteniendo lectura offline")
                            duplicateFound = true
                            finishOfflineDataReading()
                            return@launch
                        }
                        
                        val weightValue = jsonObject.getDouble("w").toFloat()
                        
                        Log.d(TAG, "📊 Procesando medición histórica: ${weightValue}kg tomada hace ${secondsAgo}s")
                        Log.d(TAG, "🕒 Timestamp calculado: $actualTimestamp (${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(actualTimestamp))})")
                        
                        // Mantener para compatibilidad
                        val weight = Weight(
                            value = weightValue,
                            timestamp = actualTimestamp,
                            unit = "kg",
                            isHistorical = true
                        )
                        
                        batchHistoryWeights.add(weight)
                        batchHistoricalMeasurements.add(Pair(weightValue, actualTimestamp))
                    }
                    
                    // Solo procesar si no encontramos duplicados
                    if (!duplicateFound && batchHistoricalMeasurements.isNotEmpty()) {
                        offlineDataCount++
                        Log.d(TAG, "📦 Lote ${offlineDataCount} procesado: ${batchHistoryWeights.size} registros históricos")
                        Log.d(TAG, "📈 Total acumulado: ${allHistoryData.size} mediciones de combustible")
                        
                        // Guardar datos históricos del lote actual en la base de datos
                        try {
                            val activeCylinder = getActiveCylinderUseCase.getActiveCylinderSync()
                            if (activeCylinder != null) {
                                val result = saveFuelMeasurementUseCase.saveHistoricalMeasurements(
                                    cylinderId = activeCylinder.id,
                                    weightMeasurements = batchHistoricalMeasurements,
                                    isCalibrated = true
                                )
                                
                                result.fold(
                                    onSuccess = { savedCount ->
                                        Log.d(TAG, "✅ Lote ${offlineDataCount} guardado: $savedCount mediciones de combustible")
                                        
                                        // Crear FuelMeasurements para la UI
                                        val fuelMeasurements = batchHistoricalMeasurements.map { (weightValue, timestamp) ->
                                            val fuelKilograms = maxOf(0f, weightValue - activeCylinder.tare)
                                            val fuelPercentage = if (activeCylinder.capacity > 0) {
                                                (fuelKilograms / activeCylinder.capacity * 100).coerceIn(0f, 100f)
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
                                        val sortedHistoryData = allHistoryData.sortedBy { it.timestamp }
                                        _historyData.value = sortedHistoryData
                                    },
                                    onFailure = { error ->
                                        Log.e(TAG, "❌ Error al guardar lote ${offlineDataCount}: ${error.message}")
                                    }
                                )
                            } else {
                                Log.w(TAG, "No hay bombona activa - datos históricos NO guardados")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Error al procesar guardado del lote ${offlineDataCount}: ${e.message}")
                        }
                        
                        // Continuar leyendo más datos si estamos en modo de lectura continua y no hay duplicados
                        if (isReadingOfflineData) {
                            // Hacer una pausa pequeña antes de solicitar más datos
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
        Log.d(TAG, "Desconectando del sensor CamperGas")
        bluetoothGatt?.let { gatt ->
            // Verificar permisos antes de desconectar
            if (bleManager.hasBluetoothConnectPermission()) {
                @SuppressLint("MissingPermission")
                gatt.disconnect()
                @SuppressLint("MissingPermission")
                gatt.close()
            } else {
                Log.w(TAG, "No hay permisos para desconectar, forzando limpieza")
                // Forzar limpieza local aunque no tengamos permisos
                cleanup()
            }
        }
        cleanup()
    }
    
    fun requestHistoryData() {
        offlineCharacteristic?.let { characteristic ->
            bluetoothGatt?.let { gatt ->
                // Verificar permisos antes de solicitar datos históricos
                if (!bleManager.hasBluetoothConnectPermission()) {
                    Log.e(TAG, "No hay permisos para solicitar datos históricos")
                    return
                }
                
                // Inicializar la lectura continua de datos offline
                startOfflineDataReading()
                
                Log.d(TAG, "Iniciando lectura continua de datos históricos...")
                
                // Primero habilitar notificaciones para datos offline si no están habilitadas
                enableNotifications(gatt, characteristic)
                
                // Iniciar la primera lectura
                continueOfflineDataReading()
                
            } ?: run {
                Log.e(TAG, "No hay conexión GATT disponible")
                _isLoadingHistory.value = false
            }
        } ?: run {
            Log.e(TAG, "Característica offline no disponible")
            _isLoadingHistory.value = false
        }
    }
    
    /**
     * Inicia la lectura automática de datos offline al conectar
     */
    private fun startAutomaticOfflineDataReading() {
        offlineCharacteristic?.let { characteristic ->
            bluetoothGatt?.let { gatt ->
                // Verificar permisos antes de solicitar datos históricos
                if (!bleManager.hasBluetoothConnectPermission()) {
                    Log.e(TAG, "No hay permisos para lectura automática de datos offline")
                    return
                }
                
                Log.d(TAG, "🔄 Iniciando lectura automática de datos offline al conectar...")
                
                // Inicializar la lectura continua de datos offline
                startOfflineDataReading()
                
                // Habilitar notificaciones para datos offline
                enableNotifications(gatt, characteristic)
                
                // Iniciar la primera lectura inmediatamente después de habilitar notificaciones
                serviceScope.launch {
                    delay(300) // Pausa para estabilizar la conexión
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
                    @SuppressLint("MissingPermission")
                    val success = gatt.readCharacteristic(characteristic)
                    if (!success) {
                        Log.e(TAG, "Error al continuar lectura de datos históricos")
                        finishOfflineDataReading()
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
            Log.d(TAG, "📊 Rango de datos: ${allHistoryData.minOfOrNull { it.timestamp }} - ${allHistoryData.maxOfOrNull { it.timestamp }}")
            Log.d(TAG, "🎯 Datos offline sincronizados automáticamente al conectar")
        } else {
            Log.d(TAG, "ℹ️ No se encontraron datos offline en el sensor")
        }
    }
    
    fun stopOfflineDataReading() {
        if (isReadingOfflineData) {
            Log.d(TAG, "Deteniendo lectura continua de datos offline...")
            finishOfflineDataReading()
        }
    }
    
    private fun cleanup() {
        // Detener lectura offline si está en progreso
        stopOfflineDataReading()
        
        bluetoothGatt = null
        weightCharacteristic = null
        inclinationCharacteristic = null
        offlineCharacteristic = null
        
        // Limpiar datos cuando se desconecta
        _weightData.value = null
        _fuelData.value = null
        _inclinationData.value = null
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
     * Calcula el timestamp real de una medición basándose en cuántos segundos han pasado
     * desde que se tomó la medición hasta ahora
     */
    private fun calculateHistoricalTimestamp(secondsAgo: Long): Long {
        return System.currentTimeMillis() - (secondsAgo * 1000L)
    }
}
