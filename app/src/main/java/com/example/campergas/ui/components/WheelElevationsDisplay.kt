package com.example.campergas.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.campergas.domain.model.VehicleType
import com.example.campergas.ui.screens.inclination.WheelElevations
import java.util.Locale
import kotlin.math.abs

@Composable
fun WheelElevationsDisplay(
    vehicleType: VehicleType,
    wheelElevations: WheelElevations,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔧 Elevación de Ruedas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (vehicleType) {
                VehicleType.CARAVAN -> CaravanWheelElevations(wheelElevations)
                VehicleType.AUTOCARAVANA -> MotorHomeWheelElevations(wheelElevations)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "💡 Valores positivos = elevar, negativos = bajar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CaravanWheelElevations(wheelElevations: WheelElevations) {
    Column {
        // Ruedín delantero
        WheelElevationItem(
            wheelName = "🛞 Ruedín Delantero",
            elevation = wheelElevations.frontSupport,
            icon = "⬆️"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Ruedas traseras
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WheelElevationItem(
                wheelName = "🛞 Trasera Izq.",
                elevation = wheelElevations.rearLeft,
                icon = "⬅️",
                modifier = Modifier.weight(1f)
            )
            
            WheelElevationItem(
                wheelName = "🛞 Trasera Der.",
                elevation = wheelElevations.rearRight,
                icon = "➡️",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MotorHomeWheelElevations(wheelElevations: WheelElevations) {
    Column {
        // Ruedas delanteras
        Text(
            text = "Ruedas Delanteras",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WheelElevationItem(
                wheelName = "🛞 Delant. Izq.",
                elevation = wheelElevations.frontLeft,
                icon = "⬅️",
                modifier = Modifier.weight(1f)
            )
            
            WheelElevationItem(
                wheelName = "🛞 Delant. Der.",
                elevation = wheelElevations.frontRight,
                icon = "➡️",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Ruedas traseras
        Text(
            text = "Ruedas Traseras",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WheelElevationItem(
                wheelName = "🛞 Trasera Izq.",
                elevation = wheelElevations.rearLeft,
                icon = "⬅️",
                modifier = Modifier.weight(1f)
            )
            
            WheelElevationItem(
                wheelName = "🛞 Trasera Der.",
                elevation = wheelElevations.rearRight,
                icon = "➡️",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun WheelElevationItem(
    wheelName: String,
    elevation: Float,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = when {
                abs(elevation) < 0.5f -> MaterialTheme.colorScheme.surfaceContainer
                elevation > 0 -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.errorContainer
            }
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = wheelName,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${String.format(Locale.US, "%.1f", elevation)} cm",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = when {
                    abs(elevation) < 0.5f -> MaterialTheme.colorScheme.onSurfaceVariant
                    elevation > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
            
            Text(
                text = when {
                    abs(elevation) < 0.5f -> "OK"
                    elevation > 0 -> "Elevar"
                    else -> "Bajar"
                },
                style = MaterialTheme.typography.labelSmall,
                color = when {
                    abs(elevation) < 0.5f -> MaterialTheme.colorScheme.onSurfaceVariant
                    elevation > 0 -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onErrorContainer
                }
            )
        }
    }
}
