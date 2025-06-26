package com.example.campergas.ui.screens.bleconnect

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campergas.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BleConnectScreen(
    navController: NavController,
    viewModel: BleConnectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Conectar Dispositivos BLE",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* TODO: Implementar scan */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Buscar Dispositivos")
            }
            
            Button(
                onClick = { /* TODO: Implementar stop scan */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Detener Búsqueda")
            }
        }

        if (uiState.isScanning) {
            Row(
                modifier = Modifier.padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buscando dispositivos...")
            }
        }

        // TODO: Implementar lista de dispositivos BLE encontrados
        Text(
            text = "Lista de dispositivos BLE - En desarrollo",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
