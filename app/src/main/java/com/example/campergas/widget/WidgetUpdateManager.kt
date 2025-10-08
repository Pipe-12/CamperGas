package com.example.campergas.widget

import android.content.Context
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.data.repository.FuelMeasurementRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bleRepository: BleRepository,
    private val fuelMeasurementRepository: FuelMeasurementRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        startListeningForUpdates()
    }

    private fun startListeningForUpdates() {
        // Escuchar cambios en las mediciones de combustible
        scope.launch {
            fuelMeasurementRepository.getLatestRealTimeMeasurement().collect { fuelMeasurement ->
                // Actualizar widget de bombona de gas siempre, con o sin datos
                GasCylinderWidgetProvider.updateAllWidgets(context)
            }
        }

        // Escuchar cambios en los datos BLE de combustible en tiempo real
        scope.launch {
            bleRepository.fuelMeasurementData.collect { fuelMeasurement ->
                // Actualizar widget cuando lleguen datos BLE nuevos
                GasCylinderWidgetProvider.updateAllWidgets(context)
            }
        }

        // Listen for changes in inclination data
        scope.launch {
            bleRepository.inclinationData.collect { inclinationData ->
                if (inclinationData != null) {
                    // Update vehicle stability widget
                    VehicleStabilityWidgetProvider.updateAllWidgets(context)
                }
            }
        }

        // Listen for changes in connection state
        scope.launch {
            bleRepository.connectionState.collect { isConnected ->
                // Update both widgets when connection state changes
                GasCylinderWidgetProvider.updateAllWidgets(context)
                VehicleStabilityWidgetProvider.updateAllWidgets(context)
            }
        }
    }
}