package com.example.campergas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campergas.domain.model.VehicleType

@Composable
fun VehicleInclinationView(
    vehicleType: VehicleType,
    pitchAngle: Float,
    rollAngle: Float,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vista lateral - Nivelado Vertical (Pitch)
        VehicleInclinationSideView(
            vehicleType = vehicleType,
            pitchAngle = pitchAngle,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Vista trasera - Nivelado Horizontal (Roll)
        VehicleInclinationRearView(
            vehicleType = vehicleType,
            rollAngle = rollAngle,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VehicleInclinationSideView(
    vehicleType: VehicleType,
    pitchAngle: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vista Lateral - Nivelado Vertical (Pitch)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", pitchAngle)}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (kotlin.math.abs(pitchAngle) <= 2f)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Canvas(
                modifier = Modifier
                    .size(280.dp, 120.dp)
                    .padding(8.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                // Dibujar línea de referencia horizontal
                drawLine(
                    color = Color.Gray,
                    start = Offset(20f, centerY),
                    end = Offset(size.width - 20f, centerY),
                    strokeWidth = 2.dp.toPx()
                )

                // Aplicar solo rotación de pitch
                translate(centerX, centerY) {
                    rotate(pitchAngle, pivot = Offset.Zero) {
                        when (vehicleType) {
                            VehicleType.CARAVAN -> drawCaravanSideView(primaryColor, secondaryColor)
                            VehicleType.AUTOCARAVANA -> drawMotorHomeSideView(
                                primaryColor,
                                secondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleInclinationRearView(
    vehicleType: VehicleType,
    rollAngle: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Vista Trasera - Nivelado Horizontal (Roll)",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", rollAngle)}°",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (kotlin.math.abs(rollAngle) <= 2f)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )

            Canvas(
                modifier = Modifier
                    .size(280.dp, 120.dp)
                    .padding(8.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2

                // Dibujar línea de referencia horizontal
                drawLine(
                    color = Color.Gray,
                    start = Offset(20f, centerY),
                    end = Offset(size.width - 20f, centerY),
                    strokeWidth = 2.dp.toPx()
                )

                // Aplicar solo rotación de roll
                translate(centerX, centerY) {
                    rotate(rollAngle, pivot = Offset.Zero) {
                        when (vehicleType) {
                            VehicleType.CARAVAN -> drawCaravanRearView(primaryColor)
                            VehicleType.AUTOCARAVANA -> drawMotorHomeRearView(
                                primaryColor,
                                secondaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawCaravanSideView(primaryColor: Color, secondaryColor: Color) {
    val width = 180f
    val height = 70f
    val wheelRadius = 12f

    // Cuerpo principal de la caravana (vista lateral)
    drawRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, -height / 2),
        size = Size(width, height)
    )

    // Ventanas laterales
    drawRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 15, -height / 2 + 10),
        size = Size(35f, 25f)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 50, -height / 2 + 10),
        size = Size(35f, 25f)
    )

    // Ruedas traseras (vista lateral - solo se ve una)
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(0f, height / 2)
    )

    // Timón/enganche delantero
    drawRect(
        color = secondaryColor,
        topLeft = Offset(-width / 2 - 50, -8f),
        size = Size(50f, 16f)
    )

    // Ruedín delantero
    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = Offset(-width / 2 - 60, 0f)
    )
}

private fun DrawScope.drawMotorHomeSideView(primaryColor: Color, secondaryColor: Color) {
    val width = 200f
    val height = 80f
    val wheelRadius = 12f

    // Cuerpo principal
    drawRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, -height / 2),
        size = Size(width, height)
    )

    // Cabina del conductor
    drawRect(
        color = secondaryColor,
        topLeft = Offset(width / 2 - 50, -height / 2),
        size = Size(50f, height)
    )

    // Ventanas laterales
    drawRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 15, -height / 2 + 12),
        size = Size(45f, 30f)
    )

    // Parabrisas
    drawRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 45, -height / 2 + 12),
        size = Size(40f, 35f)
    )

    // Ruedas (vista lateral)
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(width / 2 - 35, height / 2)
    )

    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-width / 2 + 35, height / 2)
    )
}

private fun DrawScope.drawCaravanRearView(primaryColor: Color) {
    val width = 160f
    val height = 90f
    val wheelRadius = 12f

    // Cuerpo principal de la caravana (vista trasera)
    drawRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, -height / 2),
        size = Size(width, height)
    )

    // Ventana trasera
    drawRect(
        color = Color.White,
        topLeft = Offset(-40f, -height / 2 + 15),
        size = Size(80f, 35f)
    )

    // Ruedas traseras (vista trasera - se ven las dos)
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-width / 2 + 25, height / 2)
    )
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(width / 2 - 25, height / 2)
    )

    // Detalles traseros (luces)
    drawCircle(
        color = Color.Red,
        radius = 4f,
        center = Offset(-width / 2 + 10, 0f)
    )
    drawCircle(
        color = Color.Red,
        radius = 4f,
        center = Offset(width / 2 - 10, 0f)
    )
}

private fun DrawScope.drawMotorHomeRearView(primaryColor: Color, secondaryColor: Color) {
    val width = 180f
    val height = 100f
    val wheelRadius = 12f

    // Cuerpo principal
    drawRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, -height / 2),
        size = Size(width, height)
    )

    // Ventana trasera
    drawRect(
        color = Color.White,
        topLeft = Offset(-50f, -height / 2 + 20),
        size = Size(100f, 40f)
    )

    // Ruedas traseras (vista trasera)
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-width / 2 + 30, height / 2)
    )
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(width / 2 - 30, height / 2)
    )

    // Detalles traseros (luces)
    drawCircle(
        color = Color.Red,
        radius = 5f,
        center = Offset(-width / 2 + 15, height / 4)
    )
    drawCircle(
        color = Color.Red,
        radius = 5f,
        center = Offset(width / 2 - 15, height / 4)
    )

    // Puerta trasera (línea divisoria)
    drawLine(
        color = secondaryColor,
        start = Offset(0f, -height / 2),
        end = Offset(0f, height / 2),
        strokeWidth = 3.dp.toPx()
    )
}
