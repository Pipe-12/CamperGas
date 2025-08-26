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

        // Escuchar cambios en los datos de inclinación
        scope.launch {
            bleRepository.inclinationData.collect { inclinationData ->
                if (inclinationData != null) {
                    // Actualizar widget de estabilidad del vehículo
                    VehicleStabilityWidgetProvider.updateAllWidgets(context)
                }
            }
        }

        // Escuchar cambios en el estado de conexión
        scope.launch {
            bleRepository.connectionState.collect { isConnected ->
                // Actualizar ambos widgets cuando cambie el estado de conexión
                GasCylinderWidgetProvider.updateAllWidgets(context)
                VehicleStabilityWidgetProvider.updateAllWidgets(context)
            }
        }
    }
}