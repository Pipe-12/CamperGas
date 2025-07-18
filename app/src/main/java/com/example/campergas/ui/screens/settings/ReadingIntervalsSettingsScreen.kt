package com.example.campergas.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingIntervalsSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReadingIntervalsSettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    val weightInterval by viewModel.weightInterval.collectAsStateWithLifecycle()
    val inclinationInterval by viewModel.inclinationInterval.collectAsStateWithLifecycle()
    val isConnected by viewModel.isConnected.collectAsStateWithLifecycle()
    val operationStatus by viewModel.operationStatus.collectAsStateWithLifecycle()

    var weightIntervalText by remember { mutableStateOf("") }
    var inclinationIntervalText by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }

    // Actualizar campos de texto cuando cambian los valores
    LaunchedEffect(weightInterval) {
        weightIntervalText = weightInterval.toString()
    }

    LaunchedEffect(inclinationInterval) {
        inclinationIntervalText = inclinationInterval.toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Intervalos de Lectura") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    TextButton(onClick = { showResetDialog = true }) {
                        Text("Restaurar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Mostrar estado de operación si hay uno
            operationStatus?.let { status ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Estado",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = status,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Estado de conexión
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Estado del Sensor",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isConnected) "✅ Conectado" else "⚠️ Desconectado",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (isConnected) {
                        Text(
                            text = "Los cambios se aplicarán inmediatamente",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Configuración de intervalo de peso
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lectura de Peso",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Intervalo entre lecturas de peso del sensor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = weightIntervalText,
                        onValueChange = { newValue ->
                            weightIntervalText = newValue
                        },
                        label = { Text("Intervalo (segundos)") },
                        suffix = { Text("seg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val interval = weightIntervalText.toIntOrNull()
                            if (interval != null && interval in 1..300) {
                                viewModel.setWeightInterval(interval)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = weightIntervalText.toIntOrNull()?.let { it in 1..300 } == true
                    ) {
                        Text("Aplicar Intervalo de Peso")
                    }

                    Text(
                        text = "Rango válido: 1-300 segundos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Configuración de intervalo de inclinación
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Lectura de Inclinación",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Intervalo entre lecturas de inclinación del sensor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = inclinationIntervalText,
                        onValueChange = { newValue ->
                            inclinationIntervalText = newValue
                        },
                        label = { Text("Intervalo (segundos)") },
                        suffix = { Text("seg") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val interval = inclinationIntervalText.toIntOrNull()
                            if (interval != null && interval in 1..300) {
                                viewModel.setInclinationInterval(interval)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = inclinationIntervalText.toIntOrNull()
                            ?.let { it in 1..300 } == true
                    ) {
                        Text("Aplicar Intervalo de Inclinación")
                    }

                    Text(
                        text = "Rango válido: 1-300 segundos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Aplicar ambos intervalos
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Aplicar Configuración",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val weightInt = weightIntervalText.toIntOrNull()
                            val inclinationInt = inclinationIntervalText.toIntOrNull()

                            if (weightInt != null && inclinationInt != null &&
                                weightInt in 1..300 && inclinationInt in 1..300
                            ) {
                                viewModel.setBothIntervals(weightInt, inclinationInt)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = weightIntervalText.toIntOrNull()?.let { it in 1..300 } == true &&
                                inclinationIntervalText.toIntOrNull()?.let { it in 1..300 } == true
                    ) {
                        Text("Aplicar Ambos Intervalos")
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para reiniciar lectura periódica
                    Button(
                        onClick = {
                            viewModel.restartPeriodicReading()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isConnected
                    ) {
                        Text("Reiniciar Lectura Periódica")
                    }
                }
            }

            // Información adicional
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Información",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "• Intervalos más cortos proporcionan datos más frecuentes pero pueden consumir más batería",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Intervalos más largos ahorran batería pero proporcionan datos menos frecuentes",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Los valores por defecto son 5 segundos para ambos",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Los cambios se guardan automáticamente",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

    // Diálogo de confirmación para reset
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Restaurar Valores por Defecto") },
            text = { Text("¿Estás seguro de que quieres restaurar los intervalos a los valores por defecto (5 segundos)?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetToDefault()
                        showResetDialog = false
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
