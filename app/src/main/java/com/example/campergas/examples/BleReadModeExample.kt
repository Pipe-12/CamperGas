package com.example.campergas.examples

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.campergas.domain.usecase.ReadSensorDataUseCase

/**
 * Ejemplo de uso del nuevo sistema BLE READ-only
 * Muestra cómo solicitar datos bajo demanda y mostrar el estado
 */
@Composable
fun BleReadModeExample(
    readSensorDataUseCase: ReadSensorDataUseCase
) {
    // Estados del sensor
    val isConnected by readSensorDataUseCase.getConnectionState().collectAsStateWithLifecycle()
    val fuelData by readSensorDataUseCase.getFuelData().collectAsStateWithLifecycle()
    val inclinationData by readSensorDataUseCase.getInclinationData().collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Estado de conexión
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = "Sensor: ${if (isConnected) "Conectado" else "Desconectado"}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        // Datos de combustible
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Datos de Combustible",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                fuelData?.let { fuel ->
                    Text("Peso Total: ${fuel.getFormattedTotalWeight()}")
                    Text("Combustible: ${fuel.getFormattedFuelKilograms()}")
                    Text("Porcentaje: ${fuel.getFormattedPercentage()}")
                } ?: Text("No hay datos disponibles")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { readSensorDataUseCase.readWeightData() },
                    enabled = isConnected
                ) {
                    Text("Leer Peso")
                }
            }
        }

        // Datos de inclinación
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Datos de Inclinación",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                inclinationData?.let { inclination ->
                    Text("Pitch: ${String.format("%.1f°", inclination.pitch)}")
                    Text("Roll: ${String.format("%.1f°", inclination.roll)}")
                } ?: Text("No hay datos disponibles")
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { readSensorDataUseCase.readInclinationData() },
                    enabled = isConnected
                ) {
                    Text("Leer Inclinación")
                }
            }
        }

        // Leer todos los datos
        Button(
            onClick = { readSensorDataUseCase.readAllSensorData() },
            enabled = isConnected,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Leer Todos los Datos")
        }

        // Información adicional
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Información del Sistema",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Lectura automática cada 2 segundos cuando conectado",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Usa botones para lectura bajo demanda",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Datos históricos se cargan automáticamente al conectar",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "• Compatible con firmware READ-only",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Ejemplo de uso desde un ViewModel
 */
class BleReadModeExampleViewModel(
    private val readSensorDataUseCase: ReadSensorDataUseCase
) {
    
    // StateFlows para la UI
    val isConnected = readSensorDataUseCase.getConnectionState()
    val fuelData = readSensorDataUseCase.getFuelData()
    val inclinationData = readSensorDataUseCase.getInclinationData()
    
    // Funciones para la UI
    fun readWeightData() {
        if (readSensorDataUseCase.isConnected()) {
            readSensorDataUseCase.readWeightData()
        }
    }
    
    fun readInclinationData() {
        if (readSensorDataUseCase.isConnected()) {
            readSensorDataUseCase.readInclinationData()
        }
    }
    
    fun readAllSensorData() {
        if (readSensorDataUseCase.isConnected()) {
            readSensorDataUseCase.readAllSensorData()
        }
    }
}

/**
 * Extensiones para formatear datos
 */
private fun com.example.campergas.domain.model.FuelMeasurement.getFormattedTotalWeight(): String {
    return String.format("%.1f kg", totalWeight)
}

private fun com.example.campergas.domain.model.FuelMeasurement.getFormattedFuelKilograms(): String {
    return String.format("%.1f kg", fuelKilograms)
}

private fun com.example.campergas.domain.model.FuelMeasurement.getFormattedPercentage(): String {
    return String.format("%.1f%%", fuelPercentage)
}
