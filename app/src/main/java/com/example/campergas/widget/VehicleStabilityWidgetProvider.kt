package com.example.campergas.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.campergas.MainActivity
import com.example.campergas.R
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VehicleStabilityWidgetProvider : AppWidgetProvider() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Get repositories using Hilt EntryPoint pattern for reliable dependency injection in widgets
     */
    private fun getEntryPoint(context: Context): WidgetEntryPoint {
        return EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
    }

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
            val serviceStarted = com.example.campergas.service.BleForegroundService.startForWidgets(context)
            if (serviceStarted) {
                Log.d("VehicleStabilityWidget", "Servicio BLE iniciado para widgets")
            } else {
                Log.w("VehicleStabilityWidget", "No se pudo iniciar servicio BLE - continuando sin servicio de fondo")
            }
        } catch (e: Exception) {
            Log.e("VehicleStabilityWidget", "Error al iniciar servicio BLE", e)
            // Don't rethrow - this prevents infinite loops
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
                // Get repositories using EntryPoint
                val entryPoint = getEntryPoint(context)
                val bleRepository = entryPoint.bleRepository()
                
                // Obtener datos de inclinación actuales
                val inclinationData = bleRepository.inclinationData.first()
                val isConnected = bleRepository.connectionState.first()

                // Crear las vistas remotas
                val views = RemoteViews(context.packageName, R.layout.vehicle_stability_widget)

                // Configurar datos de inclinación
                if (inclinationData != null) {
                    views.setTextViewText(R.id.widget_pitch_value, "P: %.1f°".format(inclinationData.pitch))
                    views.setTextViewText(R.id.widget_roll_value, "R: %.1f°".format(inclinationData.roll))
                    
                    // Estado de nivelación con indicador simple
                    val stabilityText = if (inclinationData.isLevel) "✅ ESTABLE" else "⚠️ INCLINADO"
                    val stabilityIndicator = if (inclinationData.isLevel) "✓" else "✗"
                    views.setTextViewText(R.id.widget_stability_status, stabilityText)
                    views.setTextViewText(R.id.widget_stability_indicator, stabilityIndicator)
                    
                    // Timestamp
                    views.setTextViewText(R.id.widget_last_update, "Actualizado: ${inclinationData.getFormattedTimestamp()}")
                } else {
                    views.setTextViewText(R.id.widget_pitch_value, "P: --°")
                    views.setTextViewText(R.id.widget_roll_value, "R: --°")
                    views.setTextViewText(R.id.widget_stability_status, "⚠️ SIN DATOS")
                    views.setTextViewText(R.id.widget_stability_indicator, "❓")
                    views.setTextViewText(R.id.widget_last_update, "Sin datos disponibles")
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
                        // Get repositories using EntryPoint
                        val entryPoint = getEntryPoint(context)
                        val bleRepository = entryPoint.bleRepository()
                        
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