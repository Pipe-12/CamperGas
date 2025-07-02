package com.example.campergas.data.ble

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import com.example.campergas.domain.model.CamperGasUuids
import com.example.campergas.domain.model.Weight
import com.example.campergas.domain.model.Inclination
import com.example.campergas.domain.usecase.SaveWeightMeasurementUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
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
    private val saveWeightMeasurementUseCase: SaveWeightMeasurementUseCase,
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
    
    // Datos de peso en tiempo real
    private val _weightData = MutableStateFlow<Weight?>(null)
    val weightData: StateFlow<Weight?> = _weightData
    
    // Datos de inclinación en tiempo real
    private val _inclinationData = MutableStateFlow<Inclination?>(null)
    val inclinationData: StateFlow<Inclination?> = _inclinationData
    
    // Datos históricos
    private val _historyData = MutableStateFlow<List<Weight>>(emptyList())
    val historyData: StateFlow<List<Weight>> = _historyData
    
    // Estado de carga de datos históricos
    private val _isLoadingHistory = MutableStateFlow(false)
    val isLoadingHistory: StateFlow<Boolean> = _isLoadingHistory
    
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
            // Las notificaciones offline se habilitan solo cuando se solicitan datos históricos
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
        
        val success = gatt.setCharacteristicNotification(characteristic, true)
        
        if (success) {
            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )
            
            descriptor?.let {
                // Usar el nuevo método writeDescriptor con ByteArray (API 33+)
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
            
            _weightData.value = weight
            Log.d(TAG, "Peso actualizado: ${weight.value} kg")
            
            // Guardar en la base de datos de forma asíncrona
            serviceScope.launch {
                try {
                    saveWeightMeasurementUseCase.saveRealTimeMeasurement(
                        value = weightValue,
                        timestamp = weight.timestamp
                    )
                    Log.d(TAG, "Medición de peso guardada en base de datos")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al guardar medición de peso: ${e.message}")
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
            Log.d(TAG, "Datos offline recibidos: $jsonString")
            
            // Parsear JSON array: [{"w":25.1,"t":1234567890},{"w":25.3,"t":1234567900}]
            val jsonArray = JSONArray(jsonString)
            val historyWeights = mutableListOf<Weight>()
            val historicalMeasurements = mutableListOf<Pair<Float, Long>>()
            
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val weightValue = jsonObject.getDouble("w").toFloat()
                val timestamp = jsonObject.getLong("t") * 1000L // Convertir de segundos a milisegundos
                
                val weight = Weight(
                    value = weightValue,
                    timestamp = timestamp,
                    unit = "kg",
                    isHistorical = true
                )
                
                historyWeights.add(weight)
                historicalMeasurements.add(Pair(weightValue, timestamp))
            }
            
            // Ordenar por timestamp (más antiguos primero)
            historyWeights.sortBy { it.timestamp }
            
            _historyData.value = historyWeights
            _isLoadingHistory.value = false
            
            Log.d(TAG, "Datos históricos procesados: ${historyWeights.size} registros")
            
            // Guardar datos históricos en la base de datos de forma asíncrona
            serviceScope.launch {
                try {
                    saveWeightMeasurementUseCase.saveHistoricalMeasurements(historicalMeasurements)
                    Log.d(TAG, "Datos históricos guardados en base de datos: ${historicalMeasurements.size} registros")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al guardar datos históricos: ${e.message}")
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar datos offline: ${e.message}")
            _isLoadingHistory.value = false
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
                gatt.disconnect()
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
                
                _isLoadingHistory.value = true
                Log.d(TAG, "Solicitando datos históricos...")
                
                // Primero habilitar notificaciones para datos offline si no están habilitadas
                enableNotifications(gatt, characteristic)
                
                // Luego leer la característica para obtener los datos offline
                val success = gatt.readCharacteristic(characteristic)
                if (!success) {
                    Log.e(TAG, "Error al solicitar lectura de datos históricos")
                    _isLoadingHistory.value = false
                }
            } ?: run {
                Log.e(TAG, "No hay conexión GATT disponible")
            }
        } ?: run {
            Log.e(TAG, "Característica offline no disponible")
        }
    }
    
    private fun cleanup() {
        bluetoothGatt = null
        weightCharacteristic = null
        inclinationCharacteristic = null
        offlineCharacteristic = null
        
        // Limpiar datos cuando se desconecta
        _weightData.value = null
        _inclinationData.value = null
    }
    
    fun isConnected(): Boolean = _connectionState.value
    
    fun clearHistoryData() {
        _historyData.value = emptyList()
    }
}
