package com.example.campergas.ui.screens.weight

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen(
    navController: NavController,
    viewModel: WeightViewModel = hiltViewModel()
) {
    val weightState by viewModel.weightState.collectAsState()
    val vehicleState by viewModel.vehicleState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitoreo de Peso") },
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
                weightState?.let { weight ->
                    // Tarjeta de información de peso
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Peso actual",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = "${weight.value} kg",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Estado: ${if (weight.isCalibrated) "Calibrado" else "No calibrado"}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "Última medición: ${formatTimestamp(weight.timestamp)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Tarjeta de capacidad del tanque
                    vehicleState?.let { vehicle ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Información del tanque",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Capacidad: ${vehicle.gasTankCapacity} kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                val remainingPercentage = (weight.value / vehicle.gasTankCapacity * 100).toInt()
                                Text(
                                    text = "Nivel actual: $remainingPercentage%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } ?: run {
                    CircularProgressIndicator()
                    Text(
                        text = "Esperando datos...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
