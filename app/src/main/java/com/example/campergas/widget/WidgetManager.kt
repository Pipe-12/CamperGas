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
     * Actualiza todos los widgets de gas cylinder
     */
    fun updateGasCylinderWidgets() {
        GasCylinderWidgetProvider.updateAllWidgets(context)
    }
    
    /**
     * Actualiza todos los widgets de estabilidad del vehículo
     */
    fun updateVehicleStabilityWidgets() {
        VehicleStabilityWidgetProvider.updateAllWidgets(context)
    }
    
    /**
     * Actualiza todos los widgets de la aplicación
     */
    fun updateAllWidgets() {
        updateGasCylinderWidgets()
        updateVehicleStabilityWidgets()
    }
    
    /**
     * Solicita datos de peso manualmente y actualiza widgets
     * También asegura que el servicio BLE esté ejecutándose
     */
    fun requestWeightDataAndUpdateWidgets() {
        try {
            // Asegurar que el servicio BLE está ejecutándose
            ensureBleServiceRunning()
            
            // Enviar broadcast para solicitar datos
            val intent = Intent(context, GasCylinderWidgetProvider::class.java).apply {
                action = GasCylinderWidgetProvider.ACTION_REQUEST_DATA
            }
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Solicitud de datos de peso enviada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar datos de peso", e)
        }
    }
    
    /**
     * Solicita datos de inclinación manualmente y actualiza widgets
     * También asegura que el servicio BLE esté ejecutándose
     */
    fun requestInclinationDataAndUpdateWidgets() {
        try {
            // Asegurar que el servicio BLE está ejecutándose
            ensureBleServiceRunning()
            
            // Enviar broadcast para solicitar datos
            val intent = Intent(context, VehicleStabilityWidgetProvider::class.java).apply {
                action = VehicleStabilityWidgetProvider.ACTION_REQUEST_INCLINATION_DATA
            }
            context.sendBroadcast(intent)
            
            Log.d(TAG, "Solicitud de datos de inclinación enviada")
        } catch (e: Exception) {
            Log.e(TAG, "Error al solicitar datos de inclinación", e)
        }
    }
    
    /**
     * Asegura que el servicio BLE esté ejecutándose para mantener las solicitudes periódicas
     */
    private fun ensureBleServiceRunning() {
        try {
            val serviceStarted = com.example.campergas.service.BleForegroundService.startForWidgets(context)
            if (serviceStarted) {
                Log.d(TAG, "Servicio BLE asegurado para widgets")
            } else {
                Log.w(TAG, "No se pudo iniciar servicio BLE - continuando sin servicio de fondo")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al asegurar servicio BLE", e)
            // Don't rethrow - this prevents issues when called from widgets
        }
    }
    
    /**
     * Inicia el servicio BLE para widgets (método público para uso externo)
     */
    fun startBleServiceForWidgets() {
        ensureBleServiceRunning()
    }
}