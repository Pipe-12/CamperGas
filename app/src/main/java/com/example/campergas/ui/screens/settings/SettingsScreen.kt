package com.example.campergas.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

import androidx.navigation.NavController
import com.example.campergas.R
import com.example.campergas.domain.model.ThemeMode

/**
 * Settings screen for application configuration.
 *
 * Provides UI for managing:
 * - Theme mode (light, dark, system)
 * - Notification preferences
 * - BLE sensor reading intervals
 * - Low fuel threshold
 *
 * @param navController Navigation controller for screen navigation
 * @param viewModel ViewModel managing settings state and persistence
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    // States for BLE intervals
    val weightInterval by viewModel.weightInterval.collectAsState()
    val inclinationInterval by viewModel.inclinationInterval.collectAsState()
    val operationStatus by viewModel.operationStatus.collectAsState()

    // Local states for text fields
    var weightIntervalText by remember { mutableStateOf("") }
    var inclinationIntervalText by remember { mutableStateOf("") }
    var gasThresholdText by remember { mutableStateOf("") }

    // Update text fields when values change
    LaunchedEffect(weightInterval) {
        weightIntervalText = weightInterval.toString()
    }

    LaunchedEffect(inclinationInterval) {
        inclinationIntervalText = inclinationInterval.toString()
    }

    // Update gas threshold text field
    LaunchedEffect(uiState.gasLevelThreshold) {
        gasThresholdText = uiState.gasLevelThreshold.toString()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back button and title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.nav_back)
                )
            }
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Mostrar el estado de la operación si hay uno
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
                        text = stringResource(R.string.settings_status),
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

        // Application theme configuration
        ThemeSelectionCard(
            currentThemeMode = uiState.themeMode,
            onThemeModeSelected = { themeMode ->
                viewModel.setThemeMode(themeMode)
            }
        )

        // Configuración de notificaciones
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.settings_enable_notifications),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }

                if (uiState.notificationsEnabled) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.settings_gas_threshold),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = gasThresholdText,
                        onValueChange = { newValue ->
                            gasThresholdText = newValue
                        },
                        label = { Text(stringResource(R.string.percentage_minimum_gas)) },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val threshold = gasThresholdText.toFloatOrNull()
                            if (threshold != null && threshold in 1.0f..50.0f) {
                                viewModel.setGasLevelThreshold(threshold)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.save_threshold))
                    }

                    Text(
                        text = stringResource(
                            R.string.settings_gas_threshold_description,
                            uiState.gasLevelThreshold.toInt()
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
        // Interval configuration of weight
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_weight_reading),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.settings_weight_interval_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = weightIntervalText,
                    onValueChange = { newValue ->
                        weightIntervalText = newValue
                    },
                    label = { Text(stringResource(R.string.settings_weight_interval_label)) },
                    suffix = { Text(stringResource(R.string.unit_minutes)) },
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
                    Text(stringResource(R.string.settings_apply_weight_interval))
                }

                Text(
                    text = stringResource(R.string.settings_weight_range),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        // Interval configuration of inclination
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_inclination_reading),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.settings_inclination_interval_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = inclinationIntervalText,
                    onValueChange = { newValue ->
                        inclinationIntervalText = newValue
                    },
                    label = { Text(stringResource(R.string.settings_inclination_interval_label)) },
                    suffix = { Text(stringResource(R.string.unit_seconds)) },
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
                    Text(stringResource(R.string.settings_apply_inclination_interval))
                }

                Text(
                    text = stringResource(R.string.settings_inclination_range),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
        }

        // Additional information
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_information),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = stringResource(R.string.settings_info_short_intervals),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.settings_info_long_intervals),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.settings_info_defaults),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.settings_info_auto_save),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

/**
 * Theme selection card for the application.
 *
 * Provides a user interface to select between different available theme modes:
 * light, dark, and system. Uses a dropdown menu for selection.
 *
 * @param currentThemeMode Currently selected theme mode
 * @param onThemeModeSelected Callback invoked when user selects a new theme
 */
@Composable
private fun ThemeSelectionCard(
    currentThemeMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit
) {
    // Estado para controlar si el menú desplegable está expandido
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(R.string.settings_theme_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Botón que muestra el tema actual y abre el menú desplegable
            Box {
                Button(
                    onClick = { expanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (currentThemeMode) {
                                ThemeMode.LIGHT -> stringResource(R.string.theme_mode_light)
                                ThemeMode.DARK -> stringResource(R.string.theme_mode_dark)
                                ThemeMode.SYSTEM -> stringResource(R.string.theme_mode_system)
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                }

                // Menú desplegable con las opciones de tema
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    // Opción de tema claro
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.theme_mode_light)) },
                        onClick = {
                            onThemeModeSelected(ThemeMode.LIGHT)
                            expanded = false
                        }
                    )
                    // Opción de tema oscuro
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.theme_mode_dark)) },
                        onClick = {
                            onThemeModeSelected(ThemeMode.DARK)
                            expanded = false
                        }
                    )
                    // Opción de tema del sistema
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.theme_mode_system)) },
                        onClick = {
                            onThemeModeSelected(ThemeMode.SYSTEM)
                            expanded = false
                        }
                    )
                }
            }

            // Mostrar el estado actual del tema
            Text(
                text = when (currentThemeMode) {
                    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
                    ThemeMode.DARK -> stringResource(R.string.theme_dark)
                    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

}
}
