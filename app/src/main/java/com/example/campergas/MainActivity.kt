package com.example.campergas

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.LocaleList
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.AppLanguage
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.ui.components.PermissionDialog
import com.example.campergas.ui.navigation.NavGraph
import com.example.campergas.ui.theme.CamperGasTheme
import com.example.campergas.utils.BluetoothPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

// Extension property for DataStore access in attachBaseContext
// This uses the same DataStore name as PreferencesDataStore for consistency
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Main activity for the CamperGas application.
 *
 * This activity is the entry point for the user interface and manages:
 * - Theme configuration (light, dark, or system)
 * - Bluetooth permissions required for BLE connection
 * - Navigation between screens using Jetpack Compose Navigation
 * - Edge-to-edge styling for system bars
 *
 * The application is configured exclusively in Spanish.
 *
 * Uses Jetpack Compose for all UI and Hilt for dependency injection.
 *
 * @author Felipe García Gómez
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        /**
         * Flag to track if we're in the process of recreating due to language change.
         * This prevents infinite recreation loops.
         * Volatile ensures visibility across threads.
         */
        @Volatile
        private var isRecreatingForLanguageChange = false
    }

    /**
     * User preferences data store.
     *
     * Provides access to saved settings such as:
     * - Notification state
     * - Gas level thresholds
     * - Sensor reading intervals
     */
    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    /**
     * Bluetooth permission manager.
     *
     * Handles request and verification of permissions required for:
     * - BLE device scanning
     * - BLE sensor connection
     * - Location access (required for BLE on Android)
     */
    private lateinit var bluetoothPermissionManager: BluetoothPermissionManager

    /**
     * Attaches the base context with the correct locale configuration.
     *
     * This method is called before onCreate and allows us to apply the locale
     * configuration before the activity is fully created.
     *
     * @param newBase The new base context
     */
    override fun attachBaseContext(newBase: Context) {
        val context = updateLocale(newBase)
        super.attachBaseContext(context)
    }

    /**
     * Updates the locale configuration for the given context.
     *
     * Reads the saved language preference and applies it to the context's
     * configuration. Uses runBlocking to read from DataStore.
     * This is called during attachBaseContext which happens before dependency
     * injection, so we need to access DataStore directly.
     *
     * @param context The context to update
     * @return The updated context with the correct locale
     */
    private fun updateLocale(context: Context): Context {
        var language = AppLanguage.SPANISH
        
        // Read the language preference from DataStore
        // We use runBlocking here because attachBaseContext requires synchronous execution
        // DataStore reads are typically fast (< 10ms) so ANR risk is minimal
        runBlocking {
            try {
                val dataStore = context.dataStore
                val prefs = dataStore.data.first()
                val languageString = prefs[stringPreferencesKey("app_language")] ?: AppLanguage.SPANISH.name
                language = try {
                    AppLanguage.valueOf(languageString)
                } catch (e: IllegalArgumentException) {
                    AppLanguage.SPANISH
                }
            } catch (e: Exception) {
                // If there's any error reading preferences, use default
                language = AppLanguage.SPANISH
            }
        }

        val locale = language.locale
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        configuration.setLocales(localeList)

        return context.createConfigurationContext(configuration)
    }

    /**
     * Initializes the activity and configures the user interface.
     *
     * This method performs the following operations:
     * 1. Loads saved theme from user preferences
     * 2. Configures system bars in edge-to-edge mode according to theme
     * 3. Initializes Bluetooth permission manager
     * 4. Sets up content with Jetpack Compose applying the selected theme
     * 5. Establishes navigation system
     * 6. Shows permission dialog if necessary
     *
     * @param savedInstanceState Saved activity state if previously destroyed
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configurar el gestor de permisos de Bluetooth
        bluetoothPermissionManager = BluetoothPermissionManager(
            activity = this,
            onPermissionsGranted = {
                // Permisos concedidos, la aplicación puede usar BLE
            },
            onPermissionsDenied = { deniedPermissions ->
                // Manejar permisos denegados
            }
        )

        setContent {
            // Cargar el tema guardado desde las preferencias de forma reactiva
            // Usamos collectAsState para que el tema se actualice automáticamente cuando cambia
            // El valor inicial es SYSTEM para evitar parpadeos en el primer frame
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            // Cargar el idioma guardado desde las preferencias de forma reactiva
            val appLanguage by preferencesDataStore.appLanguage.collectAsState(initial = AppLanguage.SPANISH)

            // Observar cambios en el idioma y recrear la actividad si cambia
            // Usamos una variable de estado para trackear el idioma anterior y evitar
            // recreaciones en el primer frame (cuando se carga el valor inicial)
            var previousLanguage by remember { mutableStateOf<AppLanguage?>(null) }
            
            LaunchedEffect(appLanguage) {
                if (previousLanguage != null && previousLanguage != appLanguage && !isRecreatingForLanguageChange) {
                    // Solo recrear si el idioma cambió después de la inicialización
                    // y no estamos ya en medio de una recreación
                    isRecreatingForLanguageChange = true
                    recreate()
                } else if (previousLanguage == null) {
                    // Primera carga, solo guardamos el idioma actual
                    previousLanguage = appLanguage
                    // Reset flag on first load to ensure future changes work
                    isRecreatingForLanguageChange = false
                }
            }

            // Determinar si se debe usar el tema oscuro para configurar las barras del sistema
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            // Configurar las barras del sistema según el tema actual
            LaunchedEffect(isDarkTheme) {
                configureSystemBars(isDarkTheme)
            }

            // Aplicar el tema de la aplicación
            CamperGasTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Estado para controlar si mostrar el diálogo de permisos
                    var showPermissionDialog by remember { mutableStateOf(false) }

                    // Verificar permisos al inicio
                    LaunchedEffect(Unit) {
                        if (!bluetoothPermissionManager.hasAllPermissions()) {
                            showPermissionDialog = true
                        }
                    }

                    // Mostrar diálogo de permisos si es necesario
                    if (showPermissionDialog) {
                        PermissionDialog(
                            title = stringResource(R.string.permissions_needed_title),
                            message = stringResource(R.string.permissions_needed_message),
                            onAccept = {
                                showPermissionDialog = false
                                bluetoothPermissionManager.checkAndRequestAllPermissions()
                            },
                            onDismiss = {
                                showPermissionDialog = false
                            }
                        )
                    }

                    NavGraph(navController = navController)
                }
            }
        }
    }

    /**
     * Configures system bars styling according to current theme.
     *
     * This method applies appropriate styling to status bar and navigation bar
     * based on selected theme (light or dark). Uses transparent colors
     * to allow content to extend to screen edges (edge-to-edge).
     *
     * @param isDarkTheme true to apply dark theme, false for light theme
     */
    private fun configureSystemBars(isDarkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            },
            navigationBarStyle = if (isDarkTheme) {
                SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    android.graphics.Color.TRANSPARENT,
                    android.graphics.Color.TRANSPARENT
                )
            }
        )
    }

}