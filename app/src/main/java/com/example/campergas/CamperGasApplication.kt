package com.example.campergas

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.widget.WidgetUpdateManager
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Clase principal de la aplicación CamperGas.
 *
 * Esta clase es el punto de entrada de la aplicación y se inicializa cuando la aplicación
 * comienza. Extiende Application y está anotada con @HiltAndroidApp para habilitar la
 * inyección de dependencias con Hilt en toda la aplicación.
 *
 * Responsabilidades:
 * - Inicializar el contenedor de inyección de dependencias Hilt
 * - Mantener referencia al gestor de actualización de widgets
 * - Proporcionar el contexto de la aplicación a través de todo el ciclo de vida
 * - Configurar el idioma de la aplicación antes de crear las actividades
 *
 * @author Felipe García Gómez
 */
@HiltAndroidApp
class CamperGasApplication : Application() {

    /**
     * Gestor de actualización de widgets de la aplicación.
     *
     * Se inyecta mediante Hilt y se utiliza para coordinar las actualizaciones
     * de los widgets de la pantalla de inicio (home screen widgets) con los datos
     * más recientes de cilindros de gas y estabilidad del vehículo.
     */
    @Inject
    lateinit var widgetUpdateManager: WidgetUpdateManager

    /**
     * DataStore de preferencias para acceso a configuraciones del usuario.
     */
    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    override fun onCreate() {
        super.onCreate()

        // Configurar el idioma de la aplicación una sola vez al inicio
        // Esto previene bucles de recreación de actividades
        kotlinx.coroutines.runBlocking {
            val appLanguage = preferencesDataStore.appLanguage.first()
            AppCompatDelegate.setApplicationLocales(appLanguage.toLocaleList())
        }
    }

}
