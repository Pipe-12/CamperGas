package com.example.campergas.data.ble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gestor centralizado de Bluetooth Low Energy (BLE).
 *
 * Esta clase singleton proporciona acceso unificado al adaptador de Bluetooth del dispositivo
 * y gestiona la verificación de permisos necesarios para operaciones BLE. Abstrae las diferencias
 * entre versiones de Android (especialmente los cambios en permisos de Android 12+).
 *
 * Funcionalidades principales:
 * - Acceso al adaptador de Bluetooth del sistema
 * - Verificación del estado de Bluetooth (habilitado/deshabilitado)
 * - Comprobación de permisos BLE según la versión de Android
 * - Manejo de permisos BLUETOOTH_SCAN y BLUETOOTH_CONNECT (Android 12+)
 * - Manejo de permisos legacy (Android 11 y anteriores)
 *
 * @property context Contexto de la aplicación para acceder a servicios del sistema
 * @author Felipe García Gómez
 */
@Singleton
class BleManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Gestor de Bluetooth del sistema.
     *
     * Se inicializa de forma lazy la primera vez que se accede, obteniendo
     * el servicio BLUETOOTH_SERVICE del sistema Android.
     */
    private val bluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    /**
     * Adaptador de Bluetooth del dispositivo.
     *
     * Proporciona acceso a las funcionalidades de Bluetooth, incluyendo
     * el escáner BLE y la gestión de conexiones. Puede ser null si el
     * dispositivo no tiene hardware Bluetooth.
     */
    val bluetoothAdapter: BluetoothAdapter?
        get() = bluetoothManager.adapter

    /**
     * Verifica si el Bluetooth está habilitado en el dispositivo.
     *
     * @return true si el Bluetooth está encendido, false si está apagado o no disponible
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Verifica si tenemos los permisos necesarios para conectar dispositivos BLE.
     *
     * En Android 12+ (API 31+), requiere el permiso BLUETOOTH_CONNECT.
     * En versiones anteriores, requiere el permiso BLUETOOTH legacy.
     *
     * @return true si tenemos el permiso necesario, false en caso contrario
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


}
