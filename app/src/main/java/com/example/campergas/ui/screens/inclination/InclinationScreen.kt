package com.example.campergas.ui.screens.inclination

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InclinationScreen(
    navController: NavController,
    viewModel: InclinationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRequestingData by viewModel.isRequestingData.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inclinación del Vehículo") },
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
            if (uiState.isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando datos del sensor...")

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para solicitar datos cuando está cargando
                    OutlinedButton(
                        onClick = { viewModel.requestInclinationDataManually() },
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
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Estado de nivelación
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.isLevel)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (uiState.isLevel) "✅ VEHÍCULO NIVELADO" else "⚠️ VEHÍCULO DESNIVELADO",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.isLevel)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Datos de inclinación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Inclinación Pitch (Cabeceo)
                        Card(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Cabeceo (Pitch)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${String.format("%.1f", uiState.inclinationPitch)}°",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Adelante/Atrás",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        // Inclinación Roll (Alabeo)
                        Card(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Alabeo (Roll)",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${String.format("%.1f", uiState.inclinationRoll)}°",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lado a lado",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información adicional
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Información del Sensor",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (uiState.timestamp > 0) {
                                Text(
                                    text = "Última actualización: ${formatTimestamp(uiState.timestamp)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            // Mostrar si cada eje está nivelado
                            Text(
                                text = "Estado Pitch: ${if (kotlin.math.abs(uiState.inclinationPitch) <= 2.0f) "✅ Nivelado" else "⚠️ Desnivelado"}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )

                            Text(
                                text = "Estado Roll: ${if (kotlin.math.abs(uiState.inclinationRoll) <= 2.0f) "✅ Nivelado" else "⚠️ Desnivelado"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón para solicitar datos de inclinación manualmente
                    Button(
                        onClick = { viewModel.requestInclinationDataManually() },
                        modifier = Modifier.fillMaxWidth(),
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
                        Text(if (isRequestingData) "Solicitando..." else "Actualizar Inclinación")
                    }

                    if (!viewModel.isConnected()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⚠️ Sensor no conectado",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else if (!viewModel.canMakeRequest()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏱️ Espera 2 segundos entre peticiones",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información de ayuda
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "💡 Información",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "• Tolerancia de nivelación: ±2°\n" +
                                        "• Pitch: Inclinación frontal/trasera (cabeceo)\n" +
                                        "• Roll: Inclinación lateral (alabeo)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
