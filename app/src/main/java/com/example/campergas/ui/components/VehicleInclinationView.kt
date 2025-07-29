package com.example.campergas.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import com.example.campergas.domain.model.VehicleType

@Composable
fun VehicleInclinationView(
    vehicleType: VehicleType,
    pitchAngle: Float,
    rollAngle: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val surfaceColor = MaterialTheme.colorScheme.surface

    Canvas(
        modifier = modifier
            .size(300.dp, 200.dp)
            .padding(16.dp)
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Aplicar rotaciones según los ángulos de inclinación
        translate(centerX, centerY) {
            rotate(rollAngle) {
                rotate(pitchAngle, pivot = Offset.Zero) {
                    when (vehicleType) {
                        VehicleType.CARAVAN -> drawCaravan(primaryColor, secondaryColor)
                        VehicleType.AUTOCARAVANA -> drawMotorHome(primaryColor, secondaryColor)
                    }
                }
            }
        }

        // Dibujar línea de referencia horizontal
        drawLine(
            color = Color.Gray,
            start = Offset(20f, centerY),
            end = Offset(size.width - 20f, centerY),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawCaravan(primaryColor: Color, secondaryColor: Color) {
    val width = 160f
    val height = 80f
    val wheelRadius = 12f
    
    // Cuerpo principal de la caravana
    drawRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, -height / 2),
        size = Size(width, height)
    )
    
    // Ventanas
    drawRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 10, -height / 2 + 10),
        size = Size(30f, 20f)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 40, -height / 2 + 10),
        size = Size(30f, 20f)
    )
    
    // Ruedas traseras
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-20f, height / 2)
    )
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(20f, height / 2)
    )
    
    // Timón/enganche delantero
    drawRect(
        color = secondaryColor,
        topLeft = Offset(-width / 2 - 40, -5f),
        size = Size(40f, 10f)
    )
    
    // Ruedín delantero
    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = Offset(-width / 2 - 50, 0f)
    )
}

private fun DrawScope.drawMotorHome(primaryColor: Color, secondaryColor: Color) {
    val width = 180f
    val height = 90f
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
        topLeft = Offset(width / 2 - 40, -height / 2),
        size = Size(40f, height)
    )
    
    // Ventanas
    drawRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 10, -height / 2 + 10),
        size = Size(40f, 25f)
    )
    drawRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 35, -height / 2 + 10),
        size = Size(30f, 25f)
    )
    
    // Parabrisas
    drawRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 35, -height / 2 + 10),
        size = Size(30f, 30f)
    )
    
    // Ruedas delanteras
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(width / 2 - 20, height / 2)
    )
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(width / 2 - 50, height / 2)
    )
    
    // Ruedas traseras
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-width / 2 + 20, height / 2)
    )
    drawCircle(
        color = Color.Black,
        radius = wheelRadius,
        center = Offset(-width / 2 + 50, height / 2)
    )
}
