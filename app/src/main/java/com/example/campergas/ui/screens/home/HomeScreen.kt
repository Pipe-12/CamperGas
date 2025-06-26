package com.example.campergas.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.navigation.NavController
import com.example.campergas.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val weightValue by viewModel.weight.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            "CamperGas",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de conexión y peso
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Estado del Sistema",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            text = "Conexión: ${if (connectionState) "✅ Conectado" else "❌ Desconectado"}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        weightValue?.let {
                            Text(
                                text = "Peso actual: ${it.value} kg",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            // Título de navegación
            item {
                Text(
                    text = "Funciones Principales",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Botones de navegación
            item {
                NavigationButton(
                    title = "Monitoreo de Peso",
                    description = "Ver peso actual y estadísticas",
                    icon = Icons.Default.Star,
                    onClick = { navController.navigate(Screen.Weight.route) }
                )
            }
            
            item {
                NavigationButton(
                    title = "Inclinación",
                    description = "Verificar nivelación del vehículo",
                    icon = Icons.Default.Info,
                    onClick = { navController.navigate(Screen.Inclination.route) }
                )
            }
            
            item {
                NavigationButton(
                    title = "Historial de Consumo",
                    description = "Ver consumo de gas histórico",
                    icon = Icons.Default.Home,
                    onClick = { navController.navigate(Screen.Consumption.route) }
                )
            }
            
            item {
                NavigationButton(
                    title = "Conectar Dispositivo",
                    description = "Gestionar conexiones Bluetooth",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate(Screen.BleConnect.route) }
                )
            }
            
            item {
                NavigationButton(
                    title = "Configurar Caravana",
                    description = "Ajustar parámetros del vehículo",
                    icon = Icons.Default.Build,
                    onClick = { navController.navigate(Screen.CaravanConfig.route) }
                )
            }
            
            item {
                NavigationButton(
                    title = "Ajustes",
                    description = "Configuración de la aplicación",
                    icon = Icons.Default.Settings,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    }
}

@Composable
private fun NavigationButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(8.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
