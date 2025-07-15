package com.example.campergas.data.repository

import android.Manifest
import androidx.annotation.RequiresPermission
import com.example.campergas.data.ble.BleDeviceScanner
import com.example.campergas.data.ble.BleManager
import com.example.campergas.data.ble.CamperGasBleService
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.BleDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleRepository @Inject constructor(
    private val bleManager: BleManager,
    private val bleDeviceScanner: BleDeviceScanner,
    private val camperGasBleService: CamperGasBleService,
    private val preferencesDataStore: PreferencesDataStore
) {
    // Scan
    val scanResults: StateFlow<List<BleDevice>> = bleDeviceScanner.scanResults
    val isScanning: StateFlow<Boolean> = bleDeviceScanner.isScanning

    // Connection state
    val connectionState: StateFlow<Boolean> = camperGasBleService.connectionState

    // Sensor data
    val fuelMeasurementData = camperGasBleService.fuelMeasurementData
    val fuelData = camperGasBleService.fuelData
    val inclinationData = camperGasBleService.inclinationData
    val historyData = camperGasBleService.historyData
    val isLoadingHistory = camperGasBleService.isLoadingHistory

    // Preferences
    val lastConnectedDeviceAddress: Flow<String> = preferencesDataStore.lastConnectedDeviceAddress
    val weightReadInterval: Flow<Long> = preferencesDataStore.weightReadInterval
    val inclinationReadInterval: Flow<Long> = preferencesDataStore.inclinationReadInterval

    fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()

    /**
     * Verifica si se tienen todos los permisos necesarios para operaciones BLE
     */
    fun hasAllBluetoothPermissions(): Boolean = bleManager.hasAllBluetoothPermissions()

    /**
     * Verifica si se tienen permisos para escanear dispositivos BLE
     */
    fun hasBluetoothScanPermission(): Boolean = bleManager.hasBluetoothScanPermission()

    /**
     * Verifica si se tienen permisos para conectar dispositivos BLE
     */
    fun hasBluetoothConnectPermission(): Boolean = bleManager.hasBluetoothConnectPermission()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() = bleDeviceScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() = bleDeviceScanner.stopScan()

    // Conexión unificada al sensor CamperGas
    fun connectToSensor(deviceAddress: String) = camperGasBleService.connect(deviceAddress)

    fun disconnectSensor() = camperGasBleService.disconnect()

    fun isConnected(): Boolean = camperGasBleService.isConnected()

    fun ensureOfflineDataReading() = camperGasBleService.ensureOfflineDataReading()

    /**
     * Solicita lectura de datos de peso bajo demanda
     */
    fun readWeightDataOnDemand() = camperGasBleService.readWeightDataOnDemand()

    /**
     * Solicita lectura de datos de inclinación bajo demanda
     */
    fun readInclinationDataOnDemand() = camperGasBleService.readInclinationDataOnDemand()

    /**
     * Configura los intervalos de lectura para peso e inclinación
     */
    fun configureReadingIntervals(weightIntervalMs: Long, inclinationIntervalMs: Long) {
        camperGasBleService.configureReadingIntervals(weightIntervalMs, inclinationIntervalMs)
    }

    /**
     * Obtiene el intervalo actual de lectura de peso
     */
    fun getWeightReadInterval(): Long = camperGasBleService.getWeightReadInterval()

    /**
     * Obtiene el intervalo actual de lectura de inclinación
     */
    fun getInclinationReadInterval(): Long = camperGasBleService.getInclinationReadInterval()

    suspend fun saveLastConnectedDevice(address: String) =
        preferencesDataStore.saveLastConnectedDevice(address)

    suspend fun saveWeightReadInterval(intervalMs: Long) =
        preferencesDataStore.setWeightReadInterval(intervalMs)

    suspend fun saveInclinationReadInterval(intervalMs: Long) =
        preferencesDataStore.setInclinationReadInterval(intervalMs)

    // Filtrado de dispositivos
    fun setCompatibleDevicesFilter(enabled: Boolean) =
        bleDeviceScanner.setCompatibleDevicesFilter(enabled)

    fun isCompatibleFilterEnabled(): Boolean = bleDeviceScanner.isCompatibleFilterEnabled()
}
