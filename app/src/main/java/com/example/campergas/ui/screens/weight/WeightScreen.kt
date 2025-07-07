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
import androidx.compose.material3.LinearProgressIndicator
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
                    // Tarjeta de información de combustible
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Medición de Combustible",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Text(
                                text = fuelMeasurement.getFormattedFuelKilograms(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Text(
                                text = fuelMeasurement.getFormattedPercentage(),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Peso total: ${"%.1f kg".format(fuelMeasurement.totalWeight)}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Text(
                                text = "Estado: ${if (fuelMeasurement.isCalibrated) "✅ Calibrado" else "⚠️ No calibrado"}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (fuelMeasurement.isCalibrated) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.error
                            )
                            
                            Text(
                                text = "Última medición: ${formatTimestamp(fuelMeasurement.timestamp)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    
                    // Tarjeta de información de bombona activa
                    activeCylinder?.let { cylinder ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Bombona Activa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Nombre: ${cylinder.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Text(
                                    text = "Peso vacía: ${String.format("%.1f", cylinder.tare)} kg",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Text(
                                    text = "Capacidad: ${String.format("%.1f", cylinder.capacity)} kg",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Gas actual: ${fuelMeasurement.getFormattedFuelKilograms()}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Text(
                                    text = "Nivel de gas: ${fuelMeasurement.getFormattedPercentage()}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        fuelMeasurement.fuelPercentage > 50 -> MaterialTheme.colorScheme.primary
                                        fuelMeasurement.fuelPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                                
                                // Barra de progreso para nivel de gas
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { fuelMeasurement.fuelPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = when {
                                        gasPercentage > 50 -> MaterialTheme.colorScheme.primary
                                        gasPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                                
                                // Indicadores de estado
                                Spacer(modifier = Modifier.height(8.dp))
                                when {
                                    fuelMeasurement.fuelPercentage <= 0 -> {
                                        Text(
                                            text = "⚠️ Bombona vacía",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    fuelMeasurement.fuelPercentage <= 20 -> {
                                        Text(
                                            text = "⚠️ Nivel bajo de gas",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    else -> {
                                        Text(
                                            text = "✅ Nivel de gas adecuado",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    } ?: run {
                        // Mensaje cuando no hay bombona activa
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Bombona Activa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "⚠️ No hay bombona activa configurada",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Medium
                                )
                                
                                Text(
                                    text = "Añade una bombona desde la pantalla principal para obtener información detallada del gas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                                    text = "Información del Tanque",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Capacidad máxima: ${vehicle.gasTankCapacity} kg",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                
                                val remainingPercentage = fuelMeasurement.fuelPercentage.toInt()
                                
                                Text(
                                    text = "Nivel actual: $remainingPercentage%",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        remainingPercentage > 50 -> MaterialTheme.colorScheme.primary
                                        remainingPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
                                )
                                
                                // Barra de progreso visual
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { remainingPercentage / 100f },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = when {
                                        remainingPercentage > 50 -> MaterialTheme.colorScheme.primary
                                        remainingPercentage > 20 -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.error
                                    }
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
