package com.example.campergas

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.compose.rememberNavController
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.Language
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.ui.components.PermissionDialog
import com.example.campergas.ui.navigation.NavGraph
import com.example.campergas.ui.theme.CamperGasTheme
import com.example.campergas.utils.BluetoothPermissionManager
import com.example.campergas.utils.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

/**
 * Actividad principal de la aplicación CamperGas.
 * 
 * Esta actividad es el punto de entrada de la interfaz de usuario y gestiona:
 * - La configuración del tema (claro/oscuro)
 * - La internacionalización (idioma del sistema o seleccionado)
 * - Los permisos de Bluetooth necesarios para la conexión BLE
 * - La navegación entre pantallas mediante Jetpack Compose Navigation
 * - El estilo edge-to-edge para las barras del sistema
 * 
 * Utiliza Jetpack Compose para toda la UI y Hilt para la inyección de dependencias.
 * 
 * @author Felipe García Gómez
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * Almacén de preferencias del usuario.
     * 
     * Proporciona acceso a las configuraciones guardadas como:
     * - Modo de tema (claro/oscuro)
     * - Idioma seleccionado
     * - Estado de notificaciones
     * - Umbrales de nivel de gas
     */
    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    /**
     * Gestor de permisos de Bluetooth.
     * 
     * Maneja la solicitud y verificación de permisos necesarios para:
     * - Escaneo de dispositivos BLE
     * - Conexión a sensores BLE
     * - Acceso a ubicación (requerido para BLE en Android)
     */
    private lateinit var bluetoothPermissionManager: BluetoothPermissionManager

    /**
     * Adjunta el contexto base con la configuración de idioma aplicada.
     * 
     * Este método se llama antes de onCreate y permite aplicar el idioma guardado
     * en las preferencias del usuario al contexto de la actividad. Esto asegura
     * que la aplicación muestre el idioma correcto incluso después de recreaciones
     * de la actividad (como cambios de configuración o rotación de pantalla).
     * 
     * @param newBase El contexto base proporcionado por el sistema
     */
    override fun attachBaseContext(newBase: Context?) {
        val context = newBase ?: return super.attachBaseContext(newBase)
        
        // Apply the saved locale setting to preserve user's language choice
        // This ensures language persists across activity recreation (e.g., language changes)
        try {
            // Create temporary preferences data store to get saved language
            val tempPreferences = PreferencesDataStore(context)
            val savedLanguage = try {
                runBlocking { tempPreferences.language.first() }
            } catch (_: Exception) {
                Language.SYSTEM // Fallback to system if loading fails
            }
            
            val wrappedContext = LocaleUtils.setLocale(context, savedLanguage)
            super.attachBaseContext(wrappedContext)
        } catch (_: Exception) {
            // Fallback to original context if locale setting fails
            super.attachBaseContext(newBase)
        }
    }

    /**
     * Inicializa la actividad y configura la interfaz de usuario.
     * 
     * Este método realiza las siguientes operaciones:
     * 1. Carga las preferencias del usuario (tema e idioma)
     * 2. Configura las barras del sistema en modo edge-to-edge
     * 3. Inicializa el gestor de permisos de Bluetooth
     * 4. Configura el contenido con Jetpack Compose
     * 5. Establece el sistema de navegación
     * 6. Muestra el diálogo de permisos si es necesario
     * 
     * @param savedInstanceState Estado guardado de la actividad si fue destruida previamente
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure initial edge-to-edge styling with user's saved theme preference
        // This prevents white system bars during activity recreation (e.g., when language changes)
        
        // Load saved theme preference to determine correct system bar styling
        val savedThemeMode = try {
            runBlocking { preferencesDataStore.themeMode.first() }
        } catch (_: Exception) {
            ThemeMode.DARK // Fallback to dark theme if loading fails
        }
        
        val isDarkThemeForSystemBars = when (savedThemeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        
        // Apply system bars configuration immediately to prevent white flash
        configureSystemBars(isDarkThemeForSystemBars)

        // Configure permissions manager
        bluetoothPermissionManager = BluetoothPermissionManager(
            activity = this,
            onPermissionsGranted = {
                // Permissions granted, the app can use BLE
            },
            onPermissionsDenied = { deniedPermissions ->
                // Handle denied permissions
            }
        )

        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = ThemeMode.DARK)
            val language by preferencesDataStore.language.collectAsState(initial = Language.SYSTEM)

            // Determine if dark theme should be used
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Configure edge-to-edge with proper system bar styling based on theme
            LaunchedEffect(isDarkTheme) {
                configureSystemBars(isDarkTheme)
            }

            // Apply locale changes when language preference changes
            LaunchedEffect(language) {
                // Only apply locale if it's different from current system locale
                // This prevents recreation on initial load when no change is needed
                val currentLanguage = LocaleUtils.getCurrentLanguageFromLocale()
                if (language != Language.SYSTEM && language != currentLanguage) {
                    LocaleUtils.applyLocaleToActivity(this@MainActivity, language)
                }
            }

            CamperGasTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // State to control whether to show permissions dialog
                    var showPermissionDialog by remember { mutableStateOf(false) }

                    // Verify permissions on start
                    LaunchedEffect(Unit) {
                        if (!bluetoothPermissionManager.hasAllPermissions()) {
                            showPermissionDialog = true
                        }
                    }

                    // Show permissions dialog if necessary
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
     * Configura el estilo de las barras del sistema según el tema activo.
     * 
     * Este método aplica el estilo apropiado a la barra de estado y la barra de navegación
     * basándose en si el tema oscuro está activo o no. Utiliza colores transparentes para
     * permitir que el contenido se extienda hasta los bordes de la pantalla (edge-to-edge).
     * 
     * El método previene el destello blanco que puede ocurrir durante la recreación de la
     * actividad al aplicar inmediatamente el estilo correcto de las barras del sistema.
     * 
     * @param isDarkTheme true si el tema oscuro está activo, false para tema claro
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