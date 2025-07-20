package com.example.campergas.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GasCylinderVisualizer(
    fuelPercentage: Float,
    modifier: Modifier = Modifier
) {
    val fillColor = when {
        fuelPercentage > 50 -> MaterialTheme.colorScheme.primary
        fuelPercentage > 20 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }
    
    val emptyColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    
    Box(
        modifier = modifier.size(width = 120.dp, height = 200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawGasCylinder(
                fillPercentage = fuelPercentage / 100f,
                fillColor = fillColor,
                emptyColor = emptyColor
            )
        }
        
        // Texto del porcentaje encima de la bombona
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp)
        ) {
            Text(
                text = "${fuelPercentage.toInt()}%",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = fillColor
            )
        }
    }
}

private fun DrawScope.drawGasCylinder(
    fillPercentage: Float,
    fillColor: Color,
    emptyColor: Color
) {
    val cylinderWidth = size.width * 0.6f
    val cylinderHeight = size.height * 0.75f
    val topCapHeight = size.height * 0.08f
    val valveWidth = cylinderWidth * 0.3f
    val valveHeight = size.height * 0.12f
    
    val startX = (size.width - cylinderWidth) / 2f
    val startY = size.height * 0.15f
    
    // Dibujar el cuerpo principal de la bombona (vacío)
    drawRoundRect(
        color = emptyColor,
        topLeft = Offset(startX, startY + topCapHeight),
        size = Size(cylinderWidth, cylinderHeight),
        cornerRadius = CornerRadius(cylinderWidth * 0.1f)
    )
    
    // Dibujar el nivel de gas (lleno)
    if (fillPercentage > 0) {
        val fillHeight = cylinderHeight * fillPercentage
        val fillY = startY + topCapHeight + (cylinderHeight - fillHeight)
        
        drawRoundRect(
            color = fillColor,
            topLeft = Offset(startX, fillY),
            size = Size(cylinderWidth, fillHeight),
            cornerRadius = CornerRadius(cylinderWidth * 0.1f)
        )
    }
    
    // Dibujar la tapa superior
    drawRoundRect(
        color = emptyColor,
        topLeft = Offset(startX, startY),
        size = Size(cylinderWidth, topCapHeight),
        cornerRadius = CornerRadius(topCapHeight * 0.5f)
    )
    
    // Dibujar la válvula
    val valveX = (size.width - valveWidth) / 2f
    drawRoundRect(
        color = emptyColor,
        topLeft = Offset(valveX, startY - valveHeight),
        size = Size(valveWidth, valveHeight + topCapHeight * 0.5f),
        cornerRadius = CornerRadius(valveWidth * 0.2f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    navController: NavController,
    viewModel: WeightViewModel = hiltViewModel()
) {
    val fuelState by viewModel.fuelState.collectAsState()
    val vehicleState by viewModel.vehicleState.collectAsState()
    val activeCylinder by viewModel.activeCylinder.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoreo de Combustible") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                fuelState?.let { fuelMeasurement ->
                    // Tarjeta principal con información esencial
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Monitor de Combustible",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Bombona de gas visual centrada
                            GasCylinderVisualizer(
                                fuelPercentage = fuelMeasurement.fuelPercentage
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Información de combustible
                            Text(
                                text = fuelMeasurement.getFormattedFuelKilograms(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = fuelMeasurement.getFormattedPercentage(),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    fuelMeasurement.fuelPercentage > 50 -> MaterialTheme.colorScheme.primary
                                    fuelMeasurement.fuelPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                                    else -> MaterialTheme.colorScheme.error
                                }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Tiempo de medición
                            Text(
                                text = "Última medición: ${formatTimestamp(fuelMeasurement.timestamp)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Datos de la bombona actual
                            activeCylinder?.let { cylinder ->
                                Text(
                                    text = "Bombona Actual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Nombre: ${cylinder.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Capacidad: ${String.format("%.1f", cylinder.capacity)} kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Peso vacía: ${String.format("%.1f", cylinder.tare)} kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } ?: run {
                                Text(
                                    text = "⚠️ No hay bombona configurada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                } ?: run {
                    // Estado cuando no hay datos
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Esperando datos del sensor...",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Asegúrate de que el sensor esté conectado",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
