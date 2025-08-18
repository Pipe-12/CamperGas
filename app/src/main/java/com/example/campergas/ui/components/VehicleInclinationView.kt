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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campergas.domain.model.VehicleType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

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
                    .size(320.dp, 140.dp)
                    .padding(8.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val groundLevel = centerY + 25f // Nivel del suelo más abajo para que las ruedas estén en la línea

                // Dibujar línea de referencia horizontal (nivel del suelo) con graduación
                drawLine(
                    color = Color.Gray.copy(alpha = 0.7f),
                    start = Offset(20f, groundLevel),
                    end = Offset(size.width - 20f, groundLevel),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Líneas de referencia adicionales cada 5 grados
                for (angle in listOf(-10, -5, 5, 10)) {
                    val angleRad = (angle * PI / 180).toFloat()
                    val lineLength = 40f
                    val startX = centerX - lineLength
                    val endX = centerX + lineLength
                    val y = groundLevel - (lineLength * sin(angleRad))
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Aplicar solo rotación de pitch, centrado en el nivel del suelo
                translate(centerX, groundLevel) {
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
                    .size(320.dp, 140.dp)
                    .padding(8.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val groundLevel = centerY + 25f // Nivel del suelo más abajo para que las ruedas estén en la línea

                // Dibujar línea de referencia horizontal (nivel del suelo) con graduación
                drawLine(
                    color = Color.Gray.copy(alpha = 0.7f),
                    start = Offset(20f, groundLevel),
                    end = Offset(size.width - 20f, groundLevel),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Líneas de referencia adicionales cada 5 grados
                for (angle in listOf(-10, -5, 5, 10)) {
                    val angleRad = (angle * PI / 180).toFloat()
                    val lineLength = 40f
                    val startX = centerX - lineLength
                    val endX = centerX + lineLength
                    val y = groundLevel - (lineLength * sin(angleRad))
                    
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(startX, y),
                        end = Offset(endX, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Aplicar solo rotación de roll, centrado en el nivel del suelo
                translate(centerX, groundLevel) {
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
    val width = 200f
    val height = 80f
    val wheelRadius = 14f
    val strokeWidth = 2.dp.toPx()
    val bodyOffsetY = -height / 2 - wheelRadius + 5f // Elevar el cuerpo por encima de las ruedas

    // Sombra del vehículo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(-width / 2 + 3, bodyOffsetY + 3),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Cuerpo principal de la caravana con esquinas redondeadas
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    
    // Contorno del cuerpo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = strokeWidth)
    )

    // Ventanas laterales con marcos
    val windowHeight = 28f
    val windowWidth = 40f
    
    // Ventana delantera
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 20, bodyOffsetY + 12),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(-width / 2 + 20, bodyOffsetY + 12),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Ventana trasera
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(width / 2 - 60, bodyOffsetY + 12),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(width / 2 - 60, bodyOffsetY + 12),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Puerta lateral
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.8f),
        topLeft = Offset(-20f, bodyOffsetY + 8),
        size = Size(40f, height - 24f),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Manija de la puerta
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 2f,
        center = Offset(15f, bodyOffsetY + height / 2)
    )

    // Ruedas con llantas detalladas - Posicionadas para tocar la línea de referencia
    val wheelCenter = Offset(0f, 0f) // Altura 0 para estar en la línea de referencia
    
    // Sombra de la rueda
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = wheelRadius + 2f,
        center = wheelCenter.copy(x = wheelCenter.x + 2f)
    )
    
    // Llanta exterior
    drawCircle(
        color = Color.Gray,
        radius = wheelRadius,
        center = wheelCenter
    )
    
    // Neumático
    drawCircle(
        color = Color.Black,
        radius = wheelRadius - 2f,
        center = wheelCenter
    )
    
    // Llanta interior
    drawCircle(
        color = Color.LightGray,
        radius = wheelRadius - 5f,
        center = wheelCenter
    )
    
    // Rayos de la llanta
    for (i in 0..4) {
        val angle = (i * 72 * PI / 180).toFloat()
        val startRadius = 3f
        val endRadius = wheelRadius - 6f
        val startX = wheelCenter.x + startRadius * cos(angle)
        val startY = wheelCenter.y + startRadius * sin(angle)
        val endX = wheelCenter.x + endRadius * cos(angle)
        val endY = wheelCenter.y + endRadius * sin(angle)
        
        drawLine(
            color = Color.Gray,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5.dp.toPx()
        )
    }

    // Timón/enganche delantero mejorado
    val hitchWidth = 60f
    val hitchHeight = 20f
    drawRoundRect(
        color = secondaryColor,
        topLeft = Offset(-width / 2 - hitchWidth, -hitchHeight / 2),
        size = Size(hitchWidth, hitchHeight),
        cornerRadius = CornerRadius(4f, 4f)
    )
    
    // Conexión del timón
    drawCircle(
        color = Color.Gray,
        radius = 6f,
        center = Offset(-width / 2 - hitchWidth, 0f)
    )

    // Ruedín delantero con soporte
    val jockeyWheelCenter = Offset(-width / 2 - hitchWidth - 15f, 0f)
    
    // Soporte del ruedín
    drawLine(
        color = Color.Gray,
        start = Offset(-width / 2 - 20f, bodyOffsetY + height - 10f),
        end = jockeyWheelCenter,
        strokeWidth = 3.dp.toPx()
    )
    
    // Ruedín
    drawCircle(
        color = Color.Black,
        radius = 8f,
        center = jockeyWheelCenter
    )
    drawCircle(
        color = Color.Gray,
        radius = 6f,
        center = jockeyWheelCenter
    )

    // Detalles adicionales
    // Línea de separación en el techo
    drawLine(
        color = Color.Black.copy(alpha = 0.3f),
        start = Offset(-width / 2 + 10, bodyOffsetY),
        end = Offset(width / 2 - 10, bodyOffsetY),
        strokeWidth = 1.dp.toPx()
    )
    
    // Ventilación en el techo
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(-15f, bodyOffsetY - 3f),
        size = Size(30f, 6f),
        cornerRadius = CornerRadius(3f, 3f)
    )
}

private fun DrawScope.drawMotorHomeSideView(primaryColor: Color, secondaryColor: Color) {
    val width = 220f
    val height = 90f
    val wheelRadius = 14f
    val strokeWidth = 2.dp.toPx()
    val bodyOffsetY = -height / 2 - wheelRadius + 5f // Elevar el cuerpo por encima de las ruedas

    // Sombra del vehículo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(-width / 2 + 3, bodyOffsetY + 3),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Cuerpo principal
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width - 60f, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    
    // Contorno del cuerpo principal
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width - 60f, height),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = strokeWidth)
    )

    // Cabina del conductor con forma más realista
    val cabinPath = Path().apply {
        moveTo(width / 2 - 60f, bodyOffsetY)
        lineTo(width / 2, bodyOffsetY + 15f)
        lineTo(width / 2, bodyOffsetY + height)
        lineTo(width / 2 - 60f, bodyOffsetY + height)
        close()
    }
    
    drawPath(
        path = cabinPath,
        color = secondaryColor
    )
    
    drawPath(
        path = cabinPath,
        color = Color.Black.copy(alpha = 0.8f),
        style = Stroke(width = strokeWidth)
    )

    // Ventanas laterales del habitáculo
    val windowHeight = 32f
    val windowWidth = 45f
    
    // Ventana principal del habitáculo
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(-width / 2 + 20, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(-width / 2 + 20, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Ventana media
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(-10f, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(-10f, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Parabrisas de la cabina (forma trapecial)
    val windshieldPath = Path().apply {
        moveTo(width / 2 - 50f, bodyOffsetY + 15f)
        lineTo(width / 2 - 10f, bodyOffsetY + 20f)
        lineTo(width / 2 - 10f, bodyOffsetY + height - 30f)
        lineTo(width / 2 - 50f, bodyOffsetY + height - 25f)
        close()
    }
    
    drawPath(
        path = windshieldPath,
        color = Color.White.copy(alpha = 0.9f)
    )
    
    drawPath(
        path = windshieldPath,
        color = Color.Black.copy(alpha = 0.6f),
        style = Stroke(width = 1.dp.toPx())
    )
    
    // Ventana lateral de la cabina
    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(width / 2 - 45f, bodyOffsetY + height - 25f),
        size = Size(30f, 20f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Puerta lateral del habitáculo
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2 + 70f, bodyOffsetY + 10),
        size = Size(45f, height - 28f),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    
    // Manija de la puerta
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 3f,
        center = Offset(-width / 2 + 105f, bodyOffsetY + height / 2)
    )

    // Ruedas detalladas - Posicionadas para tocar la línea de referencia
    val rearWheelCenter = Offset(-width / 2 + 40f, 0f) // Altura 0 para estar en la línea de referencia
    val frontWheelCenter = Offset(width / 2 - 40f, 0f)
    
    // Rueda trasera
    drawDetailedWheel(rearWheelCenter, wheelRadius)
    // Rueda delantera
    drawDetailedWheel(frontWheelCenter, wheelRadius)

    // Detalles adicionales
    // Franja lateral decorativa
    drawLine(
        color = secondaryColor.copy(alpha = 0.7f),
        start = Offset(-width / 2 + 10, bodyOffsetY + height / 2),
        end = Offset(width / 2 - 70f, bodyOffsetY + height / 2),
        strokeWidth = 4.dp.toPx()
    )
    
    // Ventilación en el techo
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(-30f, bodyOffsetY - 4f),
        size = Size(60f, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    
    // Faros delanteros
    drawCircle(
        color = Color.Yellow.copy(alpha = 0.8f),
        radius = 6f,
        center = Offset(width / 2 - 5f, bodyOffsetY + height - 15f)
    )
    drawCircle(
        color = Color.White,
        radius = 4f,
        center = Offset(width / 2 - 5f, bodyOffsetY + height - 25f)
    )
}

private fun DrawScope.drawDetailedWheel(center: Offset, radius: Float) {
    // Sombra de la rueda
    drawCircle(
        color = Color.Black.copy(alpha = 0.3f),
        radius = radius + 2f,
        center = center.copy(x = center.x + 2f)
    )
    
    // Llanta exterior
    drawCircle(
        color = Color.Gray,
        radius = radius,
        center = center
    )
    
    // Neumático
    drawCircle(
        color = Color.Black,
        radius = radius - 2f,
        center = center
    )
    
    // Llanta interior
    drawCircle(
        color = Color.LightGray,
        radius = radius - 5f,
        center = center
    )
    
    // Rayos de la llanta
    for (i in 0..4) {
        val angle = (i * 72 * PI / 180).toFloat()
        val startRadius = 3f
        val endRadius = radius - 6f
        val startX = center.x + startRadius * cos(angle)
        val startY = center.y + startRadius * sin(angle)
        val endX = center.x + endRadius * cos(angle)
        val endY = center.y + endRadius * sin(angle)
        
        drawLine(
            color = Color.Gray,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}

private fun DrawScope.drawCaravanRearView(primaryColor: Color) {
    val width = 140f // Reducido para vista más estrecha
    val height = 90f // Reducido ligeramente
    val wheelRadius = 14f
    val strokeWidth = 2.dp.toPx()
    val bodyOffsetY = -height / 2 - wheelRadius + 5f // Elevar el cuerpo por encima de las ruedas

    // Sombra del vehículo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(-width / 2 + 3, bodyOffsetY + 3),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Cuerpo principal de la caravana (vista trasera más estrecha)
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    
    // Contorno del cuerpo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = strokeWidth)
    )

    // Ventana trasera con marco (más pequeña para la perspectiva)
    val windowWidth = 70f
    val windowHeight = 35f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(-windowWidth / 2, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(-windowWidth / 2, bodyOffsetY + 15),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Puerta trasera con divisiones
    drawLine(
        color = Color.Black.copy(alpha = 0.4f),
        start = Offset(0f, bodyOffsetY + 8),
        end = Offset(0f, bodyOffsetY + height - 8),
        strokeWidth = 2.dp.toPx()
    )
    
    // Manijas de las puertas traseras
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 3f,
        center = Offset(-12f, bodyOffsetY + height / 2 + 8f)
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 3f,
        center = Offset(12f, bodyOffsetY + height / 2 + 8f)
    )

    // Ruedas traseras detalladas - Posicionadas para tocar la línea de referencia
    val leftWheelCenter = Offset(-width / 2 + 25, 0f) // Altura 0 para estar en la línea de referencia
    val rightWheelCenter = Offset(width / 2 - 25, 0f)
    
    drawDetailedWheel(leftWheelCenter, wheelRadius)
    drawDetailedWheel(rightWheelCenter, wheelRadius)

    // Luces traseras (más juntas por la perspectiva estrecha)
    val lightRadius = 5f
    
    // Luces de freno/posición
    drawCircle(
        color = Color.Red,
        radius = lightRadius,
        center = Offset(-width / 2 + 12, bodyOffsetY + height / 2 + 15f)
    )
    drawCircle(
        color = Color.Red,
        radius = lightRadius,
        center = Offset(width / 2 - 12, bodyOffsetY + height / 2 + 15f)
    )
    
    // Luces de marcha atrás
    drawCircle(
        color = Color.White,
        radius = lightRadius - 1f,
        center = Offset(-width / 2 + 12, bodyOffsetY + height / 2 + 33f)
    )
    drawCircle(
        color = Color.White,
        radius = lightRadius - 1f,
        center = Offset(width / 2 - 12, bodyOffsetY + height / 2 + 33f)
    )
    
    // Indicadores de dirección
    drawCircle(
        color = Color.Yellow,
        radius = lightRadius - 1f,
        center = Offset(-width / 2 + 28, bodyOffsetY + height / 2 + 15f)
    )
    drawCircle(
        color = Color.Yellow,
        radius = lightRadius - 1f,
        center = Offset(width / 2 - 28, bodyOffsetY + height / 2 + 15f)
    )
    
    // Contornos de las luces
    for (lightCenter in listOf(
        Offset(-width / 2 + 12, bodyOffsetY + height / 2 + 15f),
        Offset(width / 2 - 12, bodyOffsetY + height / 2 + 15f),
        Offset(-width / 2 + 12, bodyOffsetY + height / 2 + 33f),
        Offset(width / 2 - 12, bodyOffsetY + height / 2 + 33f),
        Offset(-width / 2 + 28, bodyOffsetY + height / 2 + 15f),
        Offset(width / 2 - 28, bodyOffsetY + height / 2 + 15f)
    )) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.6f),
            radius = lightRadius,
            center = lightCenter,
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // Matrícula (más pequeña para la perspectiva)
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(-20f, bodyOffsetY + height - 7f),
        size = Size(40f, 12f),
        cornerRadius = CornerRadius(2f, 2f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-20f, bodyOffsetY + height - 7f),
        size = Size(40f, 12f),
        cornerRadius = CornerRadius(2f, 2f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Estabilizadores laterales (más cortos para la perspectiva)
    drawLine(
        color = Color.Gray,
        start = Offset(-width / 2, bodyOffsetY + height - 8f),
        end = Offset(-width / 2 - 10f, -wheelRadius + 3f),
        strokeWidth = 3.dp.toPx()
    )
    drawLine(
        color = Color.Gray,
        start = Offset(width / 2, bodyOffsetY + height - 8f),
        end = Offset(width / 2 + 10f, -wheelRadius + 3f),
        strokeWidth = 3.dp.toPx()
    )
}

private fun DrawScope.drawMotorHomeRearView(primaryColor: Color, secondaryColor: Color) {
    val width = 160f // Reducido para vista más estrecha
    val height = 100f // Reducido ligeramente
    val wheelRadius = 14f
    val strokeWidth = 2.dp.toPx()
    val bodyOffsetY = -height / 2 - wheelRadius + 5f // Elevar el cuerpo por encima de las ruedas

    // Sombra del vehículo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.2f),
        topLeft = Offset(-width / 2 + 3, bodyOffsetY + 3),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Cuerpo principal (más estrecho para perspectiva trasera)
    drawRoundRect(
        color = primaryColor,
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f)
    )
    
    // Contorno del cuerpo
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2, bodyOffsetY),
        size = Size(width, height),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = strokeWidth)
    )

    // Ventana trasera grande con marco (ajustada al nuevo ancho)
    val windowWidth = 85f
    val windowHeight = 38f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.9f),
        topLeft = Offset(-windowWidth / 2, bodyOffsetY + 20),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(8f, 8f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.6f),
        topLeft = Offset(-windowWidth / 2, bodyOffsetY + 20),
        size = Size(windowWidth, windowHeight),
        cornerRadius = CornerRadius(8f, 8f),
        style = Stroke(width = 1.5.dp.toPx())
    )

    // Puerta trasera con bisagra central
    drawLine(
        color = secondaryColor,
        start = Offset(0f, bodyOffsetY + 8),
        end = Offset(0f, bodyOffsetY + height - 8),
        strokeWidth = 4.dp.toPx()
    )
    
    // Paneles de la puerta trasera (ajustados al nuevo ancho)
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.8f),
        topLeft = Offset(-width / 2 + 12, bodyOffsetY + height / 2 + 15f),
        size = Size(width / 2 - 15f, height / 3),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawRoundRect(
        color = primaryColor.copy(alpha = 0.8f),
        topLeft = Offset(3f, bodyOffsetY + height / 2 + 15f),
        size = Size(width / 2 - 15f, height / 3),
        cornerRadius = CornerRadius(4f, 4f),
        style = Stroke(width = 1.5.dp.toPx())
    )
    
    // Manijas de las puertas traseras
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 4f,
        center = Offset(-15f, bodyOffsetY + height / 2 + 25f)
    )
    drawCircle(
        color = Color.Black.copy(alpha = 0.8f),
        radius = 4f,
        center = Offset(15f, bodyOffsetY + height / 2 + 25f)
    )

    // Escalón trasero (ajustado al nuevo ancho)
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(-width / 2 + 8, bodyOffsetY + height - 5f),
        size = Size(width - 16f, 8f),
        cornerRadius = CornerRadius(2f, 2f)
    )

    // Ruedas traseras detalladas - Posicionadas para tocar la línea de referencia
    val leftWheelCenter = Offset(-width / 2 + 28, 0f) // Altura 0 para estar en la línea de referencia
    val rightWheelCenter = Offset(width / 2 - 28, 0f)
    
    drawDetailedWheel(leftWheelCenter, wheelRadius)
    drawDetailedWheel(rightWheelCenter, wheelRadius)

    // Sistema de luces traseras (ajustado para el nuevo ancho)
    val lightRadius = 6f
    
    // Luces de freno principales
    drawCircle(
        color = Color.Red,
        radius = lightRadius,
        center = Offset(-width / 2 + 15, bodyOffsetY + height / 2 + 20f)
    )
    drawCircle(
        color = Color.Red,
        radius = lightRadius,
        center = Offset(width / 2 - 15, bodyOffsetY + height / 2 + 20f)
    )
    
    // Luces de marcha atrás
    drawCircle(
        color = Color.White,
        radius = lightRadius - 1f,
        center = Offset(-width / 2 + 15, bodyOffsetY + height / 2 + 37f)
    )
    drawCircle(
        color = Color.White,
        radius = lightRadius - 1f,
        center = Offset(width / 2 - 15, bodyOffsetY + height / 2 + 37f)
    )
    
    // Indicadores de dirección
    drawCircle(
        color = Color.Yellow,
        radius = lightRadius - 1f,
        center = Offset(-width / 2 + 35, bodyOffsetY + height / 2 + 20f)
    )
    drawCircle(
        color = Color.Yellow,
        radius = lightRadius - 1f,
        center = Offset(width / 2 - 35, bodyOffsetY + height / 2 + 20f)
    )
    
    // Luces de posición
    drawCircle(
        color = Color.Red.copy(alpha = 0.7f),
        radius = lightRadius - 2f,
        center = Offset(-width / 2 + 35, bodyOffsetY + height / 2 + 32f)
    )
    drawCircle(
        color = Color.Red.copy(alpha = 0.7f),
        radius = lightRadius - 2f,
        center = Offset(width / 2 - 35, bodyOffsetY + height / 2 + 32f)
    )
    
    // Contornos de todas las luces
    val allLights = listOf(
        Offset(-width / 2 + 15, bodyOffsetY + height / 2 + 20f),
        Offset(width / 2 - 15, bodyOffsetY + height / 2 + 20f),
        Offset(-width / 2 + 15, bodyOffsetY + height / 2 + 37f),
        Offset(width / 2 - 15, bodyOffsetY + height / 2 + 37f),
        Offset(-width / 2 + 35, bodyOffsetY + height / 2 + 20f),
        Offset(width / 2 - 35, bodyOffsetY + height / 2 + 20f),
        Offset(-width / 2 + 35, bodyOffsetY + height / 2 + 32f),
        Offset(width / 2 - 35, bodyOffsetY + height / 2 + 32f)
    )
    
    for (lightCenter in allLights) {
        drawCircle(
            color = Color.Black.copy(alpha = 0.6f),
            radius = lightRadius,
            center = lightCenter,
            style = Stroke(width = 1.dp.toPx())
        )
    }

    // Matrícula (ajustada al nuevo ancho)
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(-30f, bodyOffsetY + height - 10f),
        size = Size(60f, 16f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.8f),
        topLeft = Offset(-30f, bodyOffsetY + height - 10f),
        size = Size(60f, 16f),
        cornerRadius = CornerRadius(3f, 3f),
        style = Stroke(width = 1.dp.toPx())
    )

    // Escape lateral (ajustado a la nueva posición)
    drawCircle(
        color = Color.Gray,
        radius = 7f,
        center = Offset(width / 2 - 45f, -wheelRadius + 12f)
    )
    drawCircle(
        color = Color.Black,
        radius = 5f,
        center = Offset(width / 2 - 45f, -wheelRadius + 12f)
    )

    // Escalera lateral (ajustada al nuevo ancho)
    for (i in 0..2) {
        val stepY = bodyOffsetY + height / 2 + (i * 12f)
        drawLine(
            color = Color.Gray,
            start = Offset(width / 2 - 8f, stepY),
            end = Offset(width / 2 + 3f, stepY),
            strokeWidth = 2.dp.toPx()
        )
    }
    
    // Soporte de la escalera
    drawLine(
        color = Color.Gray,
        start = Offset(width / 2 - 8f, bodyOffsetY + height / 2),
        end = Offset(width / 2 - 8f, bodyOffsetY + height - 6f),
        strokeWidth = 3.dp.toPx()
    )
}
