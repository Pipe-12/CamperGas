package com.example.campergas

import android.app.Application
import com.example.campergas.widget.WidgetUpdateManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CamperGasApplication : Application() {
    
    @Inject
    lateinit var widgetUpdateManager: WidgetUpdateManager
    
    override fun onCreate() {
        super.onCreate()
        // El widgetUpdateManager se inicializa automáticamente al inyectarse
        // y comenzará a escuchar cambios en los datos
    }
}
