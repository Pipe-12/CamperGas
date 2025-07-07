package com.example.campergas.utils

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class BluetoothPermissionManager(
    private val activity: ComponentActivity,
    private val onPermissionsGranted: () -> Unit,
    private val onPermissionsDenied: (List<String>) -> Unit
) {

    private lateinit var enableBluetoothLauncher: ActivityResultLauncher<Intent>
    private lateinit var enableLocationLauncher: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Array<String>>

    init {
        setupLaunchers()
    }

    private fun setupLaunchers() {
        // Launcher para activar Bluetooth
        enableBluetoothLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Bluetooth activado, ahora verificar permisos
                checkAndRequestPermissions()
            } else {
                onPermissionsDenied(listOf("Bluetooth no activado"))
            }
        }

        // Launcher para activar ubicación
        enableLocationLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Verificar si la ubicación está ahora activada
            if (isLocationEnabled()) {
                checkAndRequestPermissions()
            } else {
                onPermissionsDenied(listOf("Ubicación no activada"))
            }
        }

        // Launcher para solicitar permisos
        requestPermissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val deniedPermissions = permissions.filter { !it.value }.keys.toList()
            if (deniedPermissions.isEmpty()) {
                onPermissionsGranted()
            } else {
                onPermissionsDenied(deniedPermissions)
            }
        }
    }

    fun checkAndRequestAllPermissions() {
        when {
            !isBluetoothSupported() -> {
                onPermissionsDenied(listOf("Dispositivo no soporta Bluetooth"))
            }

            !isBluetoothEnabled() -> {
                requestEnableBluetooth()
            }

            !isLocationEnabled() -> {
                requestEnableLocation()
            }

            else -> {
                checkAndRequestPermissions()
            }
        }
    }

    private fun isBluetoothSupported(): Boolean {
        return activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager =
            activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter?.isEnabled == true
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun requestEnableBluetooth() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothLauncher.launch(enableBtIntent)
    }

    private fun requestEnableLocation() {
        val enableLocationIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        enableLocationLauncher.launch(enableLocationIntent)
    }

    private fun checkAndRequestPermissions() {
        val requiredPermissions = getRequiredPermissions()
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            onPermissionsGranted()
        } else {
            requestPermissionsLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf<String>()

        // Permisos para Android 12+ (API 31+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.addAll(
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else {
            // Permisos para versiones anteriores
            permissions.addAll(
                listOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

        // Permiso de ubicación siempre necesario para BLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return permissions
    }

    fun hasAllPermissions(): Boolean {
        return isBluetoothSupported() &&
                isBluetoothEnabled() &&
                isLocationEnabled() &&
                getRequiredPermissions().all {
                    ContextCompat.checkSelfPermission(
                        activity,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                }
    }

    companion object {
        fun hasBluetoothPermissions(context: Context): Boolean {
            val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                listOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            } else {
                listOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }

            return requiredPermissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
