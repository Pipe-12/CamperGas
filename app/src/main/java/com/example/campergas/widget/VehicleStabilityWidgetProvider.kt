package com.example.campergas.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.widget.RemoteViews
import com.example.campergas.MainActivity
import com.example.campergas.R
import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Inclination
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class VehicleStabilityWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var bleRepository: BleRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // Asegurar que el servicio BLE está ejecutándose para las solicitudes periódicas
        ensureBleServiceRunning(context)
        
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Este método se llama cuando se agrega el primer widget de este tipo
        Log.d("VehicleStabilityWidget", "Primer widget añadido - iniciando servicio BLE")
        ensureBleServiceRunning(context)
    }
    
    override fun onDisabled(context: Context) {
        // Este método se llama cuando se elimina el último widget de este tipo
        Log.d("VehicleStabilityWidget", "Último widget eliminado")
        // Verificar si queda algún widget activo antes de detener el servicio
        checkAndStopServiceIfNoWidgets(context)
    }
    
    private fun ensureBleServiceRunning(context: Context) {
        try {
            com.example.campergas.service.BleForegroundService.startForWidgets(context)
            Log.d("VehicleStabilityWidget", "Servicio BLE iniciado para widgets")
        } catch (e: Exception) {
            Log.e("VehicleStabilityWidget", "Error al iniciar servicio BLE", e)
        }
    }
    
    private fun checkAndStopServiceIfNoWidgets(context: Context) {
        scope.launch {
            try {
                // Verificar si hay widgets de gas activos
                val widgetManager = AppWidgetManager.getInstance(context)
                val gasComponentName = ComponentName(context, GasCylinderWidgetProvider::class.java)
                val gasWidgetIds = widgetManager.getAppWidgetIds(gasComponentName)
                
                // Verificar si hay widgets de estabilidad activos
                val stabilityComponentName = ComponentName(context, VehicleStabilityWidgetProvider::class.java)
                val stabilityWidgetIds = widgetManager.getAppWidgetIds(stabilityComponentName)
                
                // Si no hay widgets activos, detener el servicio
                if (gasWidgetIds.isEmpty() && stabilityWidgetIds.isEmpty()) {
                    Log.d("VehicleStabilityWidget", "No hay widgets activos - deteniendo servicio BLE")
                    com.example.campergas.service.BleForegroundService.stopService(context)
                }
            } catch (e: Exception) {
                Log.e("VehicleStabilityWidget", "Error al verificar widgets activos", e)
            }
        }
    }

    private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        scope.launch {
            try {
                // Obtener datos de inclinación actuales
                val inclinationData = bleRepository.inclinationData.first()
                val isConnected = bleRepository.connectionState.first()

                // Crear las vistas remotas
                val views = RemoteViews(context.packageName, R.layout.vehicle_stability_widget)

                // Configurar datos de inclinación
                if (inclinationData != null) {
                    views.setTextViewText(R.id.widget_pitch_value, "P: %.1f°".format(inclinationData.pitch))
                    views.setTextViewText(R.id.widget_roll_value, "R: %.1f°".format(inclinationData.roll))
                    
                    // Estado de nivelación
                    val stabilityText = if (inclinationData.isLevel) "✅ ESTABLE" else "⚠️ INCLINADO"
                    val stabilityColor = if (inclinationData.isLevel) "#4CAF50" else "#FF9800"
                    views.setTextViewText(R.id.widget_stability_status, stabilityText)
                    
                    // Crear imagen del vehículo inclinado
                    val vehicleBitmap = createVehicleBitmap(inclinationData.pitch, inclinationData.roll, inclinationData.isLevel)
                    views.setImageViewBitmap(R.id.widget_vehicle_image, vehicleBitmap)
                    
                    // Timestamp
                    views.setTextViewText(R.id.widget_last_update, "Actualizado: ${inclinationData.getFormattedTimestamp()}")
                } else {
                    views.setTextViewText(R.id.widget_pitch_value, "P: --°")
                    views.setTextViewText(R.id.widget_roll_value, "R: --°")
                    views.setTextViewText(R.id.widget_stability_status, "⚠️ SIN DATOS")
                    views.setTextViewText(R.id.widget_last_update, "Sin datos disponibles")
                    
                    // Imagen del vehículo sin datos
                    val vehicleBitmap = createVehicleBitmap(0f, 0f, false)
                    views.setImageViewBitmap(R.id.widget_vehicle_image, vehicleBitmap)
                }

                // Configurar estado de conexión
                val connectionText = if (isConnected) "🟢 Conectado" else "🔴 Desconectado"
                views.setTextViewText(R.id.widget_connection_status, connectionText)

                // Configurar intents
                setupIntents(context, views)

                // Actualizar widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
                
            } catch (e: Exception) {
                Log.e("VehicleStabilityWidget", "Error updating widget", e)
                // Configurar vista de error
                val views = RemoteViews(context.packageName, R.layout.vehicle_stability_widget)
                views.setTextViewText(R.id.widget_pitch_value, "P: --°")
                views.setTextViewText(R.id.widget_roll_value, "R: --°")
                views.setTextViewText(R.id.widget_stability_status, "❌ ERROR")
                views.setTextViewText(R.id.widget_connection_status, "🔴 Error")
                views.setTextViewText(R.id.widget_last_update, "Error de conexión")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun setupIntents(context: Context, views: RemoteViews) {
        // Intent para abrir la aplicación
        val openAppIntent = Intent(context, MainActivity::class.java)
        val openAppPendingIntent = PendingIntent.getActivity(
            context, 0, openAppIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

        // Intent para solicitar datos de inclinación
        val requestDataIntent = Intent(context, VehicleStabilityWidgetProvider::class.java).apply {
            action = ACTION_REQUEST_INCLINATION_DATA
        }
        val requestDataPendingIntent = PendingIntent.getBroadcast(
            context, 2, requestDataIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh_button, requestDataPendingIntent)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_REQUEST_INCLINATION_DATA -> {
                Log.d("VehicleStabilityWidget", "Manual inclination data request from widget")
                scope.launch {
                    try {
                        // Solicitar datos de inclinación manualmente
                        bleRepository.readInclinationDataOnDemand()
                        
                        // Actualizar todos los widgets
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val componentName = ComponentName(context, VehicleStabilityWidgetProvider::class.java)
                        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                        onUpdate(context, appWidgetManager, appWidgetIds)
                        
                    } catch (e: Exception) {
                        Log.e("VehicleStabilityWidget", "Error requesting inclination data", e)
                    }
                }
            }
        }
    }

    private fun createVehicleBitmap(pitch: Float, roll: Float, isStable: Boolean): Bitmap {
        val width = 300
        val height = 200
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint().apply {
            isAntiAlias = true
        }

        // Fondo transparente
        canvas.drawColor(Color.TRANSPARENT)

        // Calcular transformaciones basadas en pitch y roll
        val centerX = width / 2f
        val centerY = height / 2f
        
        // Vehículo base (caravana)
        val vehicleWidth = 200f
        val vehicleHeight = 80f
        
        // Color del vehículo según estabilidad
        val vehicleColor = when {
            isStable -> Color.parseColor("#4CAF50") // Verde estable
            kotlin.math.abs(pitch) > 5 || kotlin.math.abs(roll) > 5 -> Color.parseColor("#F44336") // Rojo peligroso
            else -> Color.parseColor("#FF9800") // Naranja advertencia
        }

        // Guardar estado del canvas
        canvas.save()
        
        // Rotar según roll e inclinar según pitch
        canvas.rotate(roll, centerX, centerY)
        
        // Efecto de pitch simulado moviendo verticalmente
        val pitchOffset = pitch * 2f
        
        // Dibujar el cuerpo del vehículo
        paint.color = vehicleColor
        paint.style = Paint.Style.FILL
        val vehicleRect = RectF(
            centerX - vehicleWidth / 2,
            centerY - vehicleHeight / 2 + pitchOffset,
            centerX + vehicleWidth / 2,
            centerY + vehicleHeight / 2 + pitchOffset
        )
        canvas.drawRoundRect(vehicleRect, 15f, 15f, paint)

        // Contorno del vehículo
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawRoundRect(vehicleRect, 15f, 15f, paint)

        // Ventanas del vehículo
        paint.color = Color.parseColor("#87CEEB") // Azul claro para ventanas
        paint.style = Paint.Style.FILL
        
        // Ventana frontal
        val windowRect1 = RectF(
            centerX - vehicleWidth / 2 + 20,
            centerY - vehicleHeight / 2 + 15 + pitchOffset,
            centerX - vehicleWidth / 2 + 60,
            centerY + vehicleHeight / 2 - 15 + pitchOffset
        )
        canvas.drawRoundRect(windowRect1, 8f, 8f, paint)
        
        // Ventana trasera
        val windowRect2 = RectF(
            centerX + vehicleWidth / 2 - 60,
            centerY - vehicleHeight / 2 + 15 + pitchOffset,
            centerX + vehicleWidth / 2 - 20,
            centerY + vehicleHeight / 2 - 15 + pitchOffset
        )
        canvas.drawRoundRect(windowRect2, 8f, 8f, paint)

        // Ruedas
        paint.color = Color.BLACK
        paint.style = Paint.Style.FILL
        val wheelRadius = 15f
        
        // Rueda trasera izquierda
        canvas.drawCircle(centerX - vehicleWidth / 2 + 30, centerY + vehicleHeight / 2 + 10 + pitchOffset, wheelRadius, paint)
        // Rueda trasera derecha
        canvas.drawCircle(centerX - vehicleWidth / 2 + 30, centerY - vehicleHeight / 2 - 10 + pitchOffset, wheelRadius, paint)
        
        // Rueda delantera (una sola para caravana)
        canvas.drawCircle(centerX + vehicleWidth / 2 - 30, centerY + pitchOffset, wheelRadius, paint)

        // Restaurar estado del canvas
        canvas.restore()

        // Línea de referencia horizontal (nivel)
        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.pathEffect = android.graphics.DashPathEffect(floatArrayOf(10f, 5f), 0f)
        canvas.drawLine(50f, centerY, width - 50f, centerY, paint)

        return bitmap
    }

    companion object {
        const val ACTION_REQUEST_INCLINATION_DATA = "com.example.campergas.widget.REQUEST_INCLINATION_DATA"
        
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, VehicleStabilityWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, VehicleStabilityWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            context.sendBroadcast(intent)
        }
    }
}