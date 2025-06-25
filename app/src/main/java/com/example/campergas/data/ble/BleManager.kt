package com.example.campergas.data.ble

import android.content.Context
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothAdapter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleManager @Inject constructor(
    private val context: Context
) {
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    
    val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager.adapter
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    // Implementa métodos para gestionar las conexiones BLE
}
