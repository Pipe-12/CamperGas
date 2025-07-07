package com.example.campergas.ui.components.gas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.FuelMeasurement

@Composable
fun GasCylinderInfoCard(
    activeCylinder: GasCylinder?,
    currentFuelMeasurement: FuelMeasurement?,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Estado del Sistema",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Información de bombona activa
            activeCylinder?.let { cylinder ->
                Text(
                    text = "Bombona activa: ${cylinder.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                
                currentFuelMeasurement?.let { fuelMeasurement ->
                    Text(
                        text = "Gas restante: ${fuelMeasurement.getFormattedPercentage()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            fuelMeasurement.fuelPercentage > 50 -> MaterialTheme.colorScheme.primary
                            fuelMeasurement.fuelPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = "Peso total: ${"%.1f kg".format(fuelMeasurement.totalWeight)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Contenido de gas: ${fuelMeasurement.getFormattedFuelKilograms()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } ?: run {
                Text(
                    text = "⚠️ No hay bombona activa - Añade una bombona para empezar",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Mostrar mensaje de error si existe
            errorMessage?.let { error ->
                Text(
                    text = "⚠️ $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
