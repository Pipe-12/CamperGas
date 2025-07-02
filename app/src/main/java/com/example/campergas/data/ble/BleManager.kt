package com.example.campergas.data.ble

import android.content.Context
import android.content.pm.PackageManager
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager.adapter
    
    /**
     * Verifica si Bluetooth está habilitado en el dispositivo
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    /**
     * Verifica si el dispositivo soporta Bluetooth LE
     */
    fun isBluetoothLeSupported(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }
    
    /**
     * Obtiene el scanner BLE si está disponible
     */
    fun getBluetoothLeScanner() = bluetoothAdapter?.bluetoothLeScanner
    
    /**
     * Verifica si Bluetooth está disponible y habilitado
     */
    fun isBluetoothAvailable(): Boolean {
        return bluetoothAdapter != null && isBluetoothEnabled()
    }
    
    /**
     * Verifica si se tienen los permisos necesarios para conectar dispositivos BLE
     */
    fun hasBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Verifica si se tienen los permisos necesarios para escanear dispositivos BLE
     */
    fun hasBluetoothScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.BLUETOOTH_ADMIN
            ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context, 
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    
    /**
     * Verifica si se tienen todos los permisos necesarios para operaciones BLE
     */
    fun hasAllBluetoothPermissions(): Boolean {
        return hasBluetoothConnectPermission() && hasBluetoothScanPermission()
    }
}
