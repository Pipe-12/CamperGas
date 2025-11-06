package com.example.campergas

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
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
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.ui.components.PermissionDialog
import com.example.campergas.ui.navigation.NavGraph
import com.example.campergas.ui.theme.CamperGasTheme
import com.example.campergas.utils.BluetoothPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Main activity for the CamperGas application.
 *
 * This activity is the entry point for the user interface and manages:
 * - Theme configuration (light, dark, or system)
 * - Bluetooth permissions required for BLE connection
 * - Navigation between screens using Jetpack Compose Navigation
 * - Edge-to-edge styling for system bars
 *
 * Uses Jetpack Compose for all UI and Hilt for dependency injection.
 *
 * @author Felipe García Gómez
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

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
            
            // Determinar el tema actual del sistema
            val systemIsDark = androidx.compose.foundation.isSystemInDarkTheme()

            // Convertir SYSTEM al tema actual del sistema en el primer inicio
            // LaunchedEffect(Unit) garantiza que solo se ejecute una vez por sesión de la app
            // Nota: Esta migración ocurre de forma asíncrona. Mientras tanto, el tema SYSTEM
            // se maneja correctamente en la UI usando el valor systemIsDark.
            LaunchedEffect(Unit) {
                // Obtener el tema actual de las preferencias
                val currentTheme = preferencesDataStore.themeMode.first()
                if (currentTheme == ThemeMode.SYSTEM) {
                    // Si el tema es SYSTEM, convertirlo al tema actual del sistema
                    val newTheme = if (systemIsDark) ThemeMode.DARK else ThemeMode.LIGHT
                    preferencesDataStore.setThemeMode(newTheme)
                }
            }

            // Determinar si se debe usar el tema oscuro para configurar las barras del sistema
            val isDarkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> systemIsDark
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