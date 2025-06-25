package com.example.campergas.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
                title = { Text("CamperGas - Inicio") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Estado de conexión
            Text(
                text = "Estado: ${if (connectionState) "Conectado" else "Desconectado"}",
                style = MaterialTheme.typography.headlineSmall
            )
            
            // Peso actual
            weightValue?.let {
                Text(
                    text = "Peso actual: ${it.value} kg",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            // Navegación a otras pantallas
            Button(
                onClick = { navController.navigate(Screen.Weight.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Monitoreo de Peso")
            }
            
            Button(
                onClick = { navController.navigate(Screen.Inclination.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Inclinación")
            }
            
            Button(
                onClick = { navController.navigate(Screen.Consumption.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Historial de Consumo")
            }
            
            Button(
                onClick = { navController.navigate(Screen.BleConnect.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Conectar Dispositivo")
            }
            
            Button(
                onClick = { navController.navigate(Screen.CaravanConfig.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar Caravana")
            }
            
            Button(
                onClick = { navController.navigate(Screen.Settings.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajustes")
            }
        }
    }
}
