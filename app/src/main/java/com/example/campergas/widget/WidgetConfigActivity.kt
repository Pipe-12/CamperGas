package com.example.campergas.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.campergas.R
import com.example.campergas.ui.theme.CamperGasTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configurar resultado como cancelado por defecto
        setResult(RESULT_CANCELED)
        
        // Obtener el ID del widget desde el intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // Si no hay ID válido, cerrar
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            CamperGasTheme {
                WidgetConfigScreen(
                    onConfirm = {
                        // Actualizar el widget y confirmar configuración
                        val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigActivity)
                        
                        // Determinar qué tipo de widget es y actualizarlo
                        updateWidget(appWidgetManager)
                        
                        // Configurar resultado exitoso
                        val resultValue = Intent().apply {
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        }
                        setResult(RESULT_OK, resultValue)
                        finish()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun updateWidget(appWidgetManager: AppWidgetManager) {
        // Intentar actualizar ambos tipos de widgets
        // En una implementación real, podrías determinar el tipo específico
        try {
            GasCylinderWidgetProvider().onUpdate(
                this,
                appWidgetManager,
                intArrayOf(appWidgetId)
            )
        } catch (_: Exception) {
            try {
                VehicleStabilityWidgetProvider().onUpdate(
                    this,
                    appWidgetManager,
                    intArrayOf(appWidgetId)
                )
            } catch (_: Exception) {
                // Si ambos fallan, al menos intentar actualizar todos los widgets
                GasCylinderWidgetProvider.updateAllWidgets(this)
                VehicleStabilityWidgetProvider.updateAllWidgets(this)
            }
        }
    }
}

@Composable
fun WidgetConfigScreen(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.widget_config_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.widget_config_ready_message),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• Toca el widget para abrir la aplicación\n• Toca el botón de actualizar para solicitar nuevos datos\n• El widget se actualiza automáticamente cuando hay cambios",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar")
                    }
                }
            }
        }
    }
}