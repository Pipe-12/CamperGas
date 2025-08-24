package com.example.campergas.ui.screens.consumption

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.campergas.domain.usecase.ChartDataPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

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
            .statusBarsPadding()
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
            onLastDayClick = { viewModel.setLastDayFilter() },
            onLastWeekClick = { viewModel.setLastWeekFilter() },
            onLastMonthClick = { viewModel.setLastMonthFilter() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Resumen de consumo
        ConsumptionSummarySection(
            lastDayConsumption = uiState.lastDayConsumption,
            lastWeekConsumption = uiState.lastWeekConsumption,
            lastMonthConsumption = uiState.lastMonthConsumption,
            customPeriodConsumption = uiState.customPeriodConsumption,
            hasCustomPeriod = uiState.startDate != null && uiState.endDate != null,
            startDate = uiState.startDate,
            endDate = uiState.endDate
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Gráfico de consumo
        if (uiState.chartData.isNotEmpty()) {
            ConsumptionChart(
                chartData = uiState.chartData,
                modifier = Modifier.height(200.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
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
    onLastDayClick: () -> Unit,
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
                    onClick = onLastDayClick,
                    label = { Text("24h") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )

                FilterChip(
                    selected = false,
                    onClick = onLastWeekClick,
                    label = { Text("7 días") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
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
                            imageVector = Icons.Default.DateRange,
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

private fun formatDateOnly(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Composable
fun ConsumptionSummarySection(
    lastDayConsumption: Float,
    lastWeekConsumption: Float,
    lastMonthConsumption: Float,
    customPeriodConsumption: Float,
    hasCustomPeriod: Boolean,
    startDate: Long?,
    endDate: Long?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
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
                    text = "📊 Resumen de Consumo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "kg consumidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryItem(
                    title = "Últimas 24h",
                    value = lastDayConsumption,
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    title = "Última semana",
                    value = lastWeekConsumption,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryItem(
                    title = "Último mes",
                    value = lastMonthConsumption,
                    modifier = Modifier.weight(1f)
                )
                
                if (hasCustomPeriod && startDate != null && endDate != null) {
                    SummaryItem(
                        title = "Período seleccionado",
                        subtitle = "${formatDateOnly(startDate)} - ${formatDateOnly(endDate)}",
                        value = customPeriodConsumption,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun SummaryItem(
    title: String,
    subtitle: String? = null,
    value: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = String.format(Locale.US, "%.2f kg", value),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun ConsumptionChart(
    chartData: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    if (chartData.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📈 Gráfico de Consumo Diario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (chartData.size < 2) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Se necesitan al menos 2 días de datos para mostrar el gráfico",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                SimpleLineChart(
                    data = chartData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Increased height for axis labels
                )
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    Canvas(modifier = modifier.background(surfaceVariant)) {
        if (data.size < 2) return@Canvas
        
        val chartWidth = size.width
        val chartHeight = size.height
        val padding = 60f // Increased padding for labels
        
        // Calculate bounds
        val minValue = data.minOf { it.kilograms }
        val maxValue = data.maxOf { it.kilograms }
        val valueRange = max(maxValue - minValue, 0.1f) // Avoid division by zero
        
        val minDate = data.minOf { it.date }
        val maxDate = data.maxOf { it.date }
        val dateRange = max(maxDate - minDate, 1L) // Avoid division by zero
        
        // Draw grid lines (horizontal) and Y-axis labels
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = padding + (i * (chartHeight - 2 * padding) / gridLines)
            
            // Draw horizontal grid line
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(padding, y),
                end = Offset(chartWidth - padding, y),
                strokeWidth = 1f
            )
            
            // Draw Y-axis labels (kg values)
            val kgValue = maxValue - (i * valueRange / gridLines)
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = onSurfaceVariant.toArgb()
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                canvas.nativeCanvas.drawText(
                    String.format("%.1f kg", kgValue),
                    padding - 10f,
                    y + 5f,
                    paint
                )
            }
        }
        
        // Draw vertical grid lines and X-axis labels (dates)
        val xAxisLabels = 4 // Show 4 date labels
        for (i in 0..xAxisLabels) {
            val x = padding + (i * (chartWidth - 2 * padding) / xAxisLabels)
            
            // Draw vertical grid line
            drawLine(
                color = Color.Gray.copy(alpha = 0.3f),
                start = Offset(x, padding),
                end = Offset(x, chartHeight - padding),
                strokeWidth = 1f
            )
            
            // Draw X-axis labels (dates)
            if (i < data.size || data.size <= xAxisLabels) {
                val dateIndex = if (data.size <= xAxisLabels) {
                    // If we have few data points, show each one
                    minOf(i, data.size - 1)
                } else {
                    // If we have many data points, sample across the range
                    (i * (data.size - 1) / xAxisLabels)
                }
                
                val timestamp = data[dateIndex].date
                val dateFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
                val dateLabel = dateFormat.format(Date(timestamp))
                
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint().apply {
                        color = onSurfaceVariant.toArgb()
                        textSize = 24f
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(
                        dateLabel,
                        x,
                        chartHeight - padding + 25f,
                        paint
                    )
                }
            }
        }
        
        // Calculate points
        val points = data.map { point ->
            val x = padding + ((point.date - minDate).toFloat() / dateRange) * (chartWidth - 2 * padding)
            val y = chartHeight - padding - ((point.kilograms - minValue) / valueRange) * (chartHeight - 2 * padding)
            Offset(x, y)
        }
        
        // Draw line
        if (points.size > 1) {
            val path = Path()
            path.moveTo(points.first().x, points.first().y)
            
            for (i in 1 until points.size) {
                path.lineTo(points[i].x, points[i].y)
            }
            
            drawPath(
                path = path,
                color = primaryColor,
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )
        }
        
        // Draw points
        points.forEach { point ->
            drawCircle(
                color = primaryColor,
                radius = 4f,
                center = point
            )
        }
    }
}
