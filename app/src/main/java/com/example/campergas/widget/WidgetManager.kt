package com.example.campergas.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val TAG = "WidgetManager"
    }
    
    /**
     * Updates all widgets de gas cylinder
     */
    fun updateGasCylinderWidgets() {
        GasCylinderWidgetProvider.updateAllWidgets(context)
    }
    
    /**
     * Updates all widgets de vehicle stability
     */
    fun updateVehicleStabilityWidgets() {
        VehicleStabilityWidgetProvider.updateAllWidgets(context)
    }
    
    /**
     * Updates all widgets of the application
     */
    fun updateAllWidgets() {
        updateGasCylinderWidgets()
        updateVehicleStabilityWidgets()
    }
    
    /**
     * Ensures that the service BLE is running y actualiza widgets
     */
    fun ensureBleServiceAndUpdateWidgets() {
        try {
            // Asegurar que el servicio BLE is running
            ensureBleServiceRunning()
            
            // Updatesr todos los widgets con los data actuales
            updateAllWidgets()
            
            Log.d(TAG, "Service BLE asegurado y widgets actualizados")
        } catch (e: Exception) {
            Log.e(TAG, "Error al asegurar servicio BLE y actualizar widgets", e)
        }
    }
    
    /**
     * Solicita data of inclination manualmente y actualiza widgets
     * Also ensures que el servicio BLE is running
     */
    fun requestInclinationDataAndUpdateWidgets() {
        try {
            // Asegurar que el servicio BLE is running
            ensureBleServiceRunning()
            
            // Enviar broadcast for solicitar data
            val intent = Intent(context, VehicleStabilityWidgetProvider::class.java).apply {
                action = VehicleStabilityWidgetProvider.ACTION_REQUEST_INCLINATION_DATA
            }
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Solicitud of data of inclination enviada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar data of inclination", e)
        }
    }
    
    /**
     * Ensures that the service BLE is running to maintain the periodic requests
     */
    private fun ensureBleServiceRunning() {
        try {
            val serviceStarted = com.example.campergas.service.BleForegroundService.startForWidgets(context)
            if (serviceStarted) {
                Log.d(TAG, "Service BLE asegurado for widgets")
            } else {
                Log.w(TAG, "No se pudo iniciar servicio BLE - continuando sin servicio de fondo")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al asegurar servicio BLE", e)
            // Don't rethrow - this prevents issues when called from widgets
        }
    }
    
    /**
     * Starts the service BLE for widgets (public method for external use)
     */
    fun startBleServiceForWidgets() {
        ensureBleServiceRunning()
    }
}