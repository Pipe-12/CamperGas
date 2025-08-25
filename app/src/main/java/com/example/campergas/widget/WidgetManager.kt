package com.example.campergas.widget

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
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
     */
    fun requestWeightDataAndUpdateWidgets() {
        // Enviar broadcast para solicitar datos
        val intent = Intent(context, GasCylinderWidgetProvider::class.java).apply {
            action = GasCylinderWidgetProvider.ACTION_REQUEST_DATA
        }
        context.sendBroadcast(intent)
    }
    
    /**
     * Solicita datos de inclinación manualmente y actualiza widgets
     */
    fun requestInclinationDataAndUpdateWidgets() {
        // Enviar broadcast para solicitar datos
        val intent = Intent(context, VehicleStabilityWidgetProvider::class.java).apply {
            action = VehicleStabilityWidgetProvider.ACTION_REQUEST_INCLINATION_DATA
        }
        context.sendBroadcast(intent)
    }
}