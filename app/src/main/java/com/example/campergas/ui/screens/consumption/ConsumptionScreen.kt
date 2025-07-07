package com.example.campergas.ui.screens.consumption

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campergas.domain.model.Consumption
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionScreen(
    navController: NavController,
    viewModel: ConsumptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historial de Consumo",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Filtros de fecha
        DateFiltersSection(
            startDate = uiState.startDate,
            endDate = uiState.endDate,
            onStartDateClick = { showStartDatePicker = true },
            onEndDateClick = { showEndDatePicker = true },
            onClearFilter = { viewModel.clearDateFilter() },
            onLastWeekClick = { viewModel.setLastWeekFilter() },
            onLastMonthClick = { viewModel.setLastMonthFilter() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Información del tracking inteligente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "📊 Tracking Inteligente",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Solo se guardan registros con cambios ≥1% o cada 15 min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "📡 Datos online | 💾 Datos offline/históricos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.error != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else if (uiState.consumptions.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay registros de consumo${if (uiState.startDate != null || uiState.endDate != null) " en el rango seleccionado" else ""}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            // Agrupar consumos por bombona
            val groupedConsumptions = uiState.consumptions.groupBy { it.cylinderName }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedConsumptions.forEach { (cylinderName, consumptions) ->
                    item {
                        Text(
                            text = cylinderName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    items(consumptions.size) { index ->
                        val consumption = consumptions[index]
                        val previousConsumption = if (index < consumptions.size - 1) {
                            consumptions[index + 1]
                        } else null

                        ConsumptionItem(
                            consumption = consumption,
                            previousConsumption = previousConsumption
                        )
                    }
                }
            }
        }
    }

    // Date Pickers
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)
            
            // Aplicar la fecha seleccionada cuando se confirma
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { selectedDate ->
                    val startOfDay = selectedDate - (selectedDate % (24 * 60 * 60 * 1000L))
                    viewModel.setDateRange(startOfDay, uiState.endDate)
                }
            }
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker = false }
                ) {
                    Text("Cancelar")
                }
            }
        ) {
            val datePickerState = rememberDatePickerState()
            DatePicker(state = datePickerState)
            
            // Aplicar la fecha seleccionada cuando se confirma
            LaunchedEffect(datePickerState.selectedDateMillis) {
                datePickerState.selectedDateMillis?.let { selectedDate ->
                    val endOfDay = selectedDate + (24 * 60 * 60 * 1000L - 1)
                    viewModel.setDateRange(uiState.startDate, endOfDay)
                }
            }
        }
    }
}

@Composable
fun DateFiltersSection(
    startDate: Long?,
    endDate: Long?,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    onClearFilter: () -> Unit,
    onLastWeekClick: () -> Unit,
    onLastMonthClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = "Filtros de Fecha",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (startDate != null || endDate != null) {
                    FilterChip(
                        selected = true,
                        onClick = onClearFilter,
                        label = { Text("Limpiar") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpiar filtros",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros rápidos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = onLastWeekClick,
                    label = { Text("7 días") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = false,
                    onClick = onLastMonthClick,
                    label = { Text("30 días") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selección de fechas específicas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (startDate != null) {
                            "Desde: ${formatDateOnly(startDate)}"
                        } else {
                            "Fecha inicio"
                        }
                    )
                }
                
                OutlinedButton(
                    onClick = onEndDateClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (endDate != null) {
                            "Hasta: ${formatDateOnly(endDate)}"
                        } else {
                            "Fecha fin"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConsumptionItem(
    consumption: Consumption,
    previousConsumption: Consumption? = null
) {
    val isSignificantChange = previousConsumption?.let { prev ->
        val percentageChange = kotlin.math.abs(consumption.fuelPercentage - prev.fuelPercentage)
        val timeDifference = consumption.date - prev.date
        val timeDifferenceMinutes = timeDifference / (60 * 1000)

        percentageChange >= 1.0f || timeDifferenceMinutes >= 15L
    } ?: true // Primera medición siempre es significativa

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = if (isSignificantChange) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDate(consumption.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Indicador de offline/histórico
                    if (consumption.isHistorical) {
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OfflineBolt,
                                    contentDescription = "Dato offline",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📡",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Online",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                if (isSignificantChange) {
                    Text(
                        text = "📊",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Porcentaje de combustible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${String.format("%.1f", consumption.fuelPercentage)}%",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                consumption.fuelPercentage > 50 -> MaterialTheme.colorScheme.primary
                                consumption.fuelPercentage > 20 -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.error
                            }
                        )

                        // Mostrar cambio respecto a la medición anterior
                        previousConsumption?.let { prev ->
                            val change = consumption.fuelPercentage - prev.fuelPercentage
                            if (kotlin.math.abs(change) >= 0.1f) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (change > 0) "+${String.format("%.1f", change)}%"
                                    else "${String.format("%.1f", change)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (change > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Combustible disponible",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.2f", consumption.fuelKilograms)} kg",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
