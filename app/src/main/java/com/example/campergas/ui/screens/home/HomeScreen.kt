package com.example.campergas.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campergas.domain.model.VehicleType
import com.example.campergas.ui.components.VehicleInclinationView
import com.example.campergas.ui.components.gas.AddGasCylinderDialog
import com.example.campergas.ui.components.gas.GasCylinderViewModel
import com.example.campergas.ui.navigation.Screen
import com.example.campergas.ui.screens.weight.GasCylinderVisualizer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
    gasCylinderViewModel: GasCylinderViewModel = hiltViewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val fuelData by viewModel.fuelData.collectAsState()
    val vehicleConfig by viewModel.vehicleConfig.collectAsState()
    val lastDayConsumption by viewModel.lastDayConsumption.collectAsState()
    val lastWeekConsumption by viewModel.lastWeekConsumption.collectAsState()
    val inclinationPitch by viewModel.inclinationPitch.collectAsState()
    val inclinationRoll by viewModel.inclinationRoll.collectAsState()

    var showAddCylinderDialog by remember { mutableStateOf(false) }

    // Hacer petición de datos del sensor al abrir la pantalla
    LaunchedEffect(Unit) {
        viewModel.requestSensorDataOnScreenOpen()
    }

    // Diálogo para agregar bombona
    if (showAddCylinderDialog) {
        AddGasCylinderDialog(
            onDismiss = { showAddCylinderDialog = false },
            onConfirm = { name, tare, capacity, setAsActive ->
                gasCylinderViewModel.addCylinder(name, tare, capacity, setAsActive)
                showAddCylinderDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        
                        // Connection status in topbar
                        Text(
                            text = if (connectionState) "✅ Conectado" else "❌ Desconectado",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Main content area - scrollable for smaller screens
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top section: Weight and History buttons side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Weight button (left)
                    NavigationButtonWithPreview(
                        title = "Monitoreo de Peso",
                        description = "Toca para acceder a la vista de peso actual y estadísticas",
                        onClick = { navController.navigate(Screen.Weight.route) },
                        modifier = Modifier.weight(1f),
                        isLargeButton = true
                    ) {
                        // Mostrar la bombona con el porcentaje actual
                        fuelData?.let { fuel ->
                            GasCylinderVisualizer(
                                fuelPercentage = fuel.fuelPercentage,
                                modifier = Modifier.size(60.dp, 90.dp)
                            )
                        } ?: run {
                            // Bombona vacía si no hay datos
                            GasCylinderVisualizer(
                                fuelPercentage = 0f,
                                modifier = Modifier.size(60.dp, 90.dp)
                            )
                        }
                    }

                    // History button (right)
                    NavigationButtonWithPreview(
                        title = "Historial de Consumo",
                        description = "Toca para acceder a la vista de consumo de gas histórico",
                        onClick = { navController.navigate(Screen.Consumption.route) },
                        modifier = Modifier.weight(1f),
                        isLargeButton = true
                    ) {
                        // Mostrar resumen compacto de consumo
                        ConsumptionPreview(
                            lastDayConsumption = lastDayConsumption,
                            lastWeekConsumption = lastWeekConsumption
                        )
                    }
                }

                // Bottom section: Inclination button with larger space for drawing
                NavigationButtonWithPreview(
                    title = "Inclinación",
                    description = "Toca para acceder a la vista de nivelación del vehículo",
                    onClick = { navController.navigate(Screen.Inclination.route) },
                    isLargeButton = true
                ) {
                    // Mostrar vista más grande del vehículo con inclinación
                    vehicleConfig?.let { config ->
                        VehicleInclinationView(
                            vehicleType = config.type,
                            pitchAngle = inclinationPitch,
                            rollAngle = inclinationRoll,
                            modifier = Modifier.height(160.dp)
                        )
                    } ?: run {
                        // Vista por defecto si no hay configuración
                        VehicleInclinationView(
                            vehicleType = VehicleType.CARAVAN,
                            pitchAngle = inclinationPitch,
                            rollAngle = inclinationRoll,
                            modifier = Modifier.height(160.dp)
                        )
                    }
                }
            }

            // Add cylinder button section - positioned above configuration
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            ) {
                Button(
                    onClick = { showAddCylinderDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Añadir Bombona")
                }
            }

            // Static bottom configuration buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configuración",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón de conexión BLE
                    ConfigurationButton(
                        title = "Conectar BLE",
                        text = "🔗",
                        onClick = { navController.navigate(Screen.BleConnect.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // Botón de configuración
                    ConfigurationButton(
                        title = "Configuración",
                        icon = Icons.Default.Settings,
                        onClick = { navController.navigate(Screen.Settings.route) },
                        modifier = Modifier.weight(1f)
                    )

                    // Botón de ajustes de vehículo
                    val vehicleIcon = getVehicleIcon(vehicleConfig?.type)
                    ConfigurationButton(
                        title = "Ajustes del Vehículo",
                        text = vehicleIcon,
                        onClick = { navController.navigate(Screen.CaravanConfig.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationButtonWithPreview(
    title: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLargeButton: Boolean = false,
    content: @Composable () -> Unit
) {
    val buttonHeight = if (isLargeButton) 280.dp else 160.dp
    
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
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
            
            // Contenido del preview (bombona, gráfico, etc.)
            Box(
                modifier = Modifier.width(if (isLargeButton) 160.dp else 80.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

@Composable
private fun ConsumptionPreview(
    lastDayConsumption: Float,
    lastWeekConsumption: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "📊 Resumen",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "24h: ${String.format("%.1f", lastDayConsumption)} kg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "7d: ${String.format("%.1f", lastWeekConsumption)} kg",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
private fun ConfigurationButton(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    text: String? = null
) {
    ElevatedCard(
        modifier = modifier.height(80.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun getVehicleIcon(vehicleType: VehicleType?): String {
    return when (vehicleType) {
        VehicleType.CARAVAN -> "🚐"
        VehicleType.AUTOCARAVANA -> "🚌"
        null -> "🚗"
    }
}
