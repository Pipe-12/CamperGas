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

    // Connection state
    val connectionState: StateFlow<Boolean> = camperGasBleService.connectionState

    // Sensor data
    val fuelMeasurementData = camperGasBleService.fuelMeasurementData
    val fuelData = camperGasBleService.fuelData
    val inclinationData = camperGasBleService.inclinationData

    // Preferences
    val lastConnectedDeviceAddress: Flow<String> = preferencesDataStore.lastConnectedDeviceAddress
    val weightReadInterval: Flow<Long> = preferencesDataStore.weightReadInterval
    val inclinationReadInterval: Flow<Long> = preferencesDataStore.inclinationReadInterval

    fun isBluetoothEnabled(): Boolean = bleManager.isBluetoothEnabled()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun startScan() = bleDeviceScanner.startScan()

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun stopScan() = bleDeviceScanner.stopScan()

    // Unified connection to sensor CamperGas
    fun connectToSensor(deviceAddress: String) = camperGasBleService.connect(deviceAddress)

    fun disconnectSensor() = camperGasBleService.disconnect()

    fun isConnected(): Boolean = camperGasBleService.isConnected()

    /**
     * Solicita lectura of data of weight bajo demanda
     */
    fun readWeightDataOnDemand() = camperGasBleService.readWeightDataOnDemand()

    /**
     * Solicita lectura of data of inclination bajo demanda
     */
    fun readInclinationDataOnDemand() = camperGasBleService.readInclinationDataOnDemand()

    /**
     * Configura los intervalos de lectura for peso e inclination
     */
    fun configureReadingIntervals(weightIntervalMs: Long, inclinationIntervalMs: Long) {
        camperGasBleService.configureReadingIntervals(weightIntervalMs, inclinationIntervalMs)
    }

    /**
     * Gets el intervalo actual de lectura of weight
     */
    fun getWeightReadInterval(): Long = camperGasBleService.getWeightReadInterval()

    /**
     * Gets el intervalo actual de lectura of inclination
     */
    fun getInclinationReadInterval(): Long = camperGasBleService.getInclinationReadInterval()

    suspend fun saveLastConnectedDevice(address: String) =
        preferencesDataStore.saveLastConnectedDevice(address)

    suspend fun saveWeightReadInterval(intervalMs: Long) =
        preferencesDataStore.setWeightReadInterval(intervalMs)

    suspend fun saveInclinationReadInterval(intervalMs: Long) =
        preferencesDataStore.setInclinationReadInterval(intervalMs)

    // Filtrado de devices
    fun setCompatibleDevicesFilter(enabled: Boolean) =
        bleDeviceScanner.setCompatibleDevicesFilter(enabled)

    fun isCompatibleFilterEnabled(): Boolean = bleDeviceScanner.isCompatibleFilterEnabled()
}
