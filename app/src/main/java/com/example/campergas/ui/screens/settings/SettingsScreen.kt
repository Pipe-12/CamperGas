package com.example.campergas.ui.screens.settings

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.example.campergas.domain.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()
    val isSystemInDarkTheme = isSystemInDarkTheme()

    // Estados para intervalos BLE
    val weightInterval by viewModel.weightInterval.collectAsState()
    val inclinationInterval by viewModel.inclinationInterval.collectAsState()
    val operationStatus by viewModel.operationStatus.collectAsState()

    // Estados locales para campos de texto
    var weightIntervalText by remember { mutableStateOf("") }
    var inclinationIntervalText by remember { mutableStateOf("") }

    // Actualizar campos de texto cuando cambian los valores
    LaunchedEffect(weightInterval) {
        weightIntervalText = weightInterval.toString()
    }

    LaunchedEffect(inclinationInterval) {
        inclinationIntervalText = inclinationInterval.toString()
    }

    // Determinar si el switch debe estar activado
    val isDarkModeEnabled = when (uiState.themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón de volver atrás y título
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Configuración",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

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

        // Configuración de tema
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Apariencia",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modo oscuro",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDarkModeEnabled,
                        onCheckedChange = { isEnabled ->
                            // Si se activa, poner modo oscuro; si se desactiva, poner modo claro
                            viewModel.setThemeMode(if (isEnabled) ThemeMode.DARK else ThemeMode.LIGHT)
                        }
                    )
                }

                // Botón para volver al modo sistema
                if (uiState.themeMode != ThemeMode.SYSTEM) {
                    TextButton(
                        onClick = { viewModel.setThemeMode(ThemeMode.SYSTEM) },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "Usar configuración del sistema",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                // Texto explicativo del estado actual
                Text(
                    text = when (uiState.themeMode) {
                        ThemeMode.SYSTEM -> "Siguiendo configuración del sistema"
                        ThemeMode.LIGHT -> "Modo claro activado"
                        ThemeMode.DARK -> "Modo oscuro activado"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        // Configuración de notificaciones
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Notificaciones",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Activar notificaciones",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Intervalo entre lecturas de peso del sensor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = weightIntervalText,
                    onValueChange = { newValue ->
                        weightIntervalText = newValue
                    },
                    label = { Text("Intervalo (minutos)") },
                    suffix = { Text("min") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val interval = weightIntervalText.toIntOrNull()
                        if (interval != null && interval in 1..60) {
                            viewModel.setWeightInterval(interval)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = weightIntervalText.toIntOrNull()?.let { it in 1..60 } == true
                ) {
                    Text("Aplicar Intervalo de Peso")
                }

                Text(
                    text = "Rango válido: 1-60 minutos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Intervalo entre lecturas de inclinación del sensor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

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
                    enabled = inclinationIntervalText.toIntOrNull()?.let { it in 1..300 } == true
                ) {
                    Text("Aplicar Intervalo de Inclinación")
                }

                Text(
                    text = "Rango válido: 1-300 segundos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
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
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "• Intervalos más cortos proporcionan datos más frecuentes pero pueden consumir más batería",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Intervalos más largos ahorran batería pero proporcionan datos menos frecuentes",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Peso: 1 minuto por defecto, Inclinación: 5 segundos por defecto",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "• Los cambios se guardan automáticamente",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
