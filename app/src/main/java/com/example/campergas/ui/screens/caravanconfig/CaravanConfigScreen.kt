package com.example.campergas.ui.screens.caravanconfig

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campergas.domain.model.VehicleType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaravanConfigScreen(
    navController: NavController,
    viewModel: CaravanConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración del Vehículo",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Tipo de vehículo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .selectableGroup()
            ) {
                Text(
                    text = "Tipo de Vehículo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                VehicleType.entries.forEach { vehicleType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (uiState.selectedVehicleType == vehicleType),
                                onClick = { viewModel.updateVehicleType(vehicleType) }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (uiState.selectedVehicleType == vehicleType),
                            onClick = { viewModel.updateVehicleType(vehicleType) }
                        )
                        Text(
                            text = when (vehicleType) {
                                VehicleType.CARAVAN -> "Caravana"
                                VehicleType.AUTOCARAVANA -> "Autocaravana"
                            },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }

        // Dimensiones
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Dimensiones (cm)",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Distancia entre ruedas traseras (común para ambos tipos)
                OutlinedTextField(
                    value = if (uiState.distanceBetweenWheels == 0f) "" else uiState.distanceBetweenWheels.toString(),
                    onValueChange = { value ->
                        value.toFloatOrNull()?.let { viewModel.updateDistanceBetweenWheels(it) }
                    },
                    label = { Text("Distancia entre ruedas traseras") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                // Campo específico según el tipo de vehículo
                when (uiState.selectedVehicleType) {
                    VehicleType.CARAVAN -> {
                        OutlinedTextField(
                            value = if (uiState.distanceToFrontSupport == 0f) "" else uiState.distanceToFrontSupport.toString(),
                            onValueChange = { value ->
                                value.toFloatOrNull()
                                    ?.let { viewModel.updateDistanceToFrontSupport(it) }
                            },
                            label = { Text("Distancia al apoyo delantero") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    VehicleType.AUTOCARAVANA -> {
                        OutlinedTextField(
                            value = if (uiState.distanceBetweenFrontWheels == 0f) "" else uiState.distanceBetweenFrontWheels.toString(),
                            onValueChange = { value ->
                                value.toFloatOrNull()
                                    ?.let { viewModel.updateDistanceBetweenFrontWheels(it) }
                            },
                            label = { Text("Distancia entre ruedas delanteras") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Indicador de carga
        if (uiState.isLoading || uiState.isSaving) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Mensaje de error
        uiState.error?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving
            ) {
                Text("Cancelar")
            }

            Button(
                onClick = { viewModel.saveConfiguration() },
                modifier = Modifier.weight(1f),
                enabled = !uiState.isSaving && !uiState.isLoading
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar")
                }
            }
        }
    }
}
