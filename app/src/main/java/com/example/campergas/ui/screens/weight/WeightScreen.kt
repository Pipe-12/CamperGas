package com.example.campergas.ui.screens.weight

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.luminance
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

    // Use theme-appropriate colors for better consistency
    val isDarkTheme = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val cylinderBackground = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    } else {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
    }

    Box(
        modifier = modifier.size(width = 140.dp, height = 220.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawGasCylinder(
                fillPercentage = fuelPercentage / 100f,
                fillColor = fillColor,
                backgroundColor = cylinderBackground
            )
        }

        // Percentage text with theme-aware coloring
        Text(
            text = "${fuelPercentage.toInt()}%",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun DrawScope.drawGasCylinder(
    fillPercentage: Float,
    fillColor: Color,
    backgroundColor: Color
) {
    val cylinderWidth = size.width * 0.7f
    val cylinderHeight = size.height * 0.7f
    val topCapHeight = size.height * 0.06f
    val bottomCapHeight = size.height * 0.06f
    val valveWidth = cylinderWidth * 0.25f
    val valveHeight = size.height * 0.08f

    val startX = (size.width - cylinderWidth) / 2f
    val startY = size.height * 0.12f

    // Dibujar la base inferior
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(startX, startY + topCapHeight + cylinderHeight),
        size = Size(cylinderWidth, bottomCapHeight),
        cornerRadius = CornerRadius(cylinderWidth * 0.05f)
    )

    // Dibujar el contorno principal de la bombona (más grueso)
    val strokeWidth = 8f
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(startX - strokeWidth / 2, startY + topCapHeight - strokeWidth / 2),
        size = Size(cylinderWidth + strokeWidth, cylinderHeight + strokeWidth),
        cornerRadius = CornerRadius(cylinderWidth * 0.08f)
    )

    // Dibujar el interior con el color de fondo según el tema
    drawRoundRect(
        color = backgroundColor,
        topLeft = Offset(startX, startY + topCapHeight),
        size = Size(cylinderWidth, cylinderHeight),
        cornerRadius = CornerRadius(cylinderWidth * 0.06f)
    )

    // Dibujar el nivel de gas (lleno) desde abajo hacia arriba
    if (fillPercentage > 0) {
        val fillHeight = cylinderHeight * fillPercentage
        val fillY = startY + topCapHeight + (cylinderHeight - fillHeight)

        drawRoundRect(
            color = fillColor,
            topLeft = Offset(startX, fillY),
            size = Size(cylinderWidth, fillHeight),
            cornerRadius = CornerRadius(cylinderWidth * 0.06f)
        )
    }

    // Dibujar la tapa superior
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(startX, startY),
        size = Size(cylinderWidth, topCapHeight),
        cornerRadius = CornerRadius(topCapHeight * 0.3f)
    )

    // Dibujar la válvula superior (más realista)
    val valveX = (size.width - valveWidth) / 2f
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(valveX, startY - valveHeight * 0.7f),
        size = Size(valveWidth, valveHeight),
        cornerRadius = CornerRadius(valveWidth * 0.15f)
    )

    // Dibujar el pico de la válvula
    val nozzleWidth = valveWidth * 0.4f
    val nozzleHeight = valveHeight * 0.3f
    val nozzleX = (size.width - nozzleWidth) / 2f
    drawRoundRect(
        color = Color.Gray,
        topLeft = Offset(nozzleX, startY - valveHeight * 0.8f - nozzleHeight),
        size = Size(nozzleWidth, nozzleHeight),
        cornerRadius = CornerRadius(nozzleWidth * 0.2f)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    navController: NavController,
    viewModel: WeightViewModel = hiltViewModel()
) {
    val fuelState by viewModel.fuelState.collectAsState()
    val activeCylinder by viewModel.activeCylinder.collectAsState()
    val isRequestingData by viewModel.isRequestingData.collectAsState()

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
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Monitor de Combustible",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Bombona de gas visual centrada
                            GasCylinderVisualizer(
                                fuelPercentage = fuelMeasurement.fuelPercentage
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Información de combustible centrada
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

                            Spacer(modifier = Modifier.height(20.dp))

                            // Tiempo de medición centrado
                            Text(
                                text = "Última medición: ${formatTimestamp(fuelMeasurement.timestamp)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Botón para solicitar datos manualmente
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.requestWeightDataManually() },
                                    modifier = Modifier.weight(1f),
                                    enabled = viewModel.isConnected() && viewModel.canMakeRequest()
                                ) {
                                    if (isRequestingData) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isRequestingData) "Solicitando..." else "Actualizar Peso")
                                }
                            }

                            if (!viewModel.isConnected()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⚠️ Sensor no conectado",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else if (!viewModel.canMakeRequest()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "⏱️ Espera 2 segundos entre peticiones",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Datos de la bombona actual centrados
                            activeCylinder?.let { cylinder ->
                                Text(
                                    text = "Bombona Actual",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Nombre: ${cylinder.name}",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Capacidad: ${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.1f",
                                            cylinder.capacity
                                        )
                                    } kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                Text(
                                    text = "Peso vacía: ${
                                        String.format(
                                            Locale.getDefault(),
                                            "%.1f",
                                            cylinder.tare
                                        )
                                    } kg",
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

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para solicitar datos cuando no hay datos disponibles
                        OutlinedButton(
                            onClick = { viewModel.requestWeightDataManually() },
                            enabled = viewModel.isConnected() && viewModel.canMakeRequest()
                        ) {
                            if (isRequestingData) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRequestingData) "Solicitando..." else "Solicitar Datos")
                        }

                        if (!viewModel.isConnected()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⚠️ Conecta el sensor primero",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else if (!viewModel.canMakeRequest()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "⏱️ Espera 2 segundos entre peticiones",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
