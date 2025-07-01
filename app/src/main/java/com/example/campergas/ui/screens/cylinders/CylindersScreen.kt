package com.example.campergas.ui.screens.cylinders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.Weight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CylindersScreen(
    viewModel: CylindersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val cylinders by viewModel.cylinders.collectAsState()
    val activeCylinder by viewModel.activeCylinder.collectAsState()
    val currentWeight by viewModel.currentWeight.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Diálogo para agregar bombona
    if (showAddDialog) {
        AddCylinderDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, tare, capacity, setAsActive ->
                viewModel.addCylinder(name, tare, capacity, setAsActive)
                showAddDialog = false
            }
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Gestión de Bombonas",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Total: ${cylinders.size} bombonas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Bombona activa actual
        activeCylinder?.let { cylinder ->
            ActiveCylinderCard(
                cylinder = cylinder,
                currentWeight = currentWeight,
                onDeactivate = { viewModel.deactivateCylinder() }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Botón agregar
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Bombona")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Error display
        uiState.error?.let { error ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { viewModel.clearError() }
                    ) {
                        Text("Cerrar")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Lista de bombonas
        Text(
            text = "Todas las Bombonas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        if (cylinders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No hay bombonas registradas",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Agrega tu primera bombona",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(cylinders) { cylinder ->
                    CylinderCard(
                        cylinder = cylinder,
                        isActive = cylinder.id == activeCylinder?.id,
                        currentWeight = currentWeight,
                        onSetActive = { viewModel.setActiveCylinder(cylinder.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveCylinderCard(
    cylinder: GasCylinder,
    currentWeight: Weight?,
    onDeactivate: () -> Unit
) {
    val gasPercentage = currentWeight?.let { cylinder.calculateGasPercentage(it.value) } ?: 0f
    val gasContent = currentWeight?.let { cylinder.calculateGasContent(it.value) } ?: 0f
    val isEmpty = currentWeight?.let { cylinder.isEmpty(it.value) } ?: false
    val isLowGas = currentWeight?.let { cylinder.isLowGas(it.value) } ?: false
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isEmpty -> MaterialTheme.colorScheme.errorContainer
                isLowGas -> Color(0xFFFF9800) // Orange
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Bombona Activa",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = cylinder.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = cylinder.getDisplayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                OutlinedButton(
                    onClick = onDeactivate,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text("Desactivar")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Información del gas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Gas actual",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "%.1f kg".format(gasContent),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Nivel",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "%.0f%%".format(gasPercentage),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Barra de progreso
            LinearProgressIndicator(
                progress = { gasPercentage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    isEmpty -> MaterialTheme.colorScheme.error
                    isLowGas -> Color(0xFFFF5722) // Deep Orange
                    else -> MaterialTheme.colorScheme.primary
                }
            )
            
            if (currentWeight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Última medición: ${currentWeight.getFullFormattedTimestamp()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun CylinderCard(
    cylinder: GasCylinder,
    isActive: Boolean,
    currentWeight: Weight?,
    onSetActive: () -> Unit
) {
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 8.dp else 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isActive) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = cylinder.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = cylinder.getDisplayName(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (!isActive) {
                        TextButton(onClick = onSetActive) {
                            Text("Activar")
                        }
                    }
                }
            }
            
            if (isActive && currentWeight != null) {
                Spacer(modifier = Modifier.height(8.dp))
                val gasPercentage = cylinder.calculateGasPercentage(currentWeight.value)
                val gasContent = cylinder.calculateGasContent(currentWeight.value)
                
                Text(
                    text = "Gas: %.1f kg (%.0f%%)".format(gasContent, gasPercentage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
