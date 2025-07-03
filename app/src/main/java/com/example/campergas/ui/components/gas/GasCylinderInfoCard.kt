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
import com.example.campergas.domain.model.Weight

@Composable
fun GasCylinderInfoCard(
    activeCylinder: GasCylinder?,
    currentWeight: Weight?,
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
                
                currentWeight?.let { weight ->
                    val gasPercentage = cylinder.calculateGasPercentage(weight.value)
                    Text(
                        text = "Gas restante: ${String.format("%.1f", gasPercentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            gasPercentage > 50 -> MaterialTheme.colorScheme.primary
                            gasPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                    )
                    
                    Text(
                        text = "Peso total: ${weight.getFormattedValue()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    val gasContent = cylinder.calculateGasContent(weight.value)
                    Text(
                        text = "Contenido de gas: ${String.format("%.1f", gasContent)} kg",
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
