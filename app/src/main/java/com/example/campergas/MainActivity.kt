package com.example.campergas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
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
import javax.inject.Inject

/**
 * Actividad principal de la aplicación CamperGas.
 * 
 * Esta actividad es el punto de entrada de la interfaz de usuario y gestiona:
 * - La configuración del tema (modo oscuro únicamente)
 * - Los permisos de Bluetooth necesarios para la conexión BLE
 * - La navegación entre pantallas mediante Jetpack Compose Navigation
 * - El estilo edge-to-edge para las barras del sistema
 * 
 * La aplicación está configurada exclusivamente en español.
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
     * - Estado de notificaciones
     * - Umbrales de nivel de gas
     * - Intervalos de lectura de sensores
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
     * Inicializa la actividad y configura la interfaz de usuario.
     * 
     * Este método realiza las siguientes operaciones:
     * 1. Configura las barras del sistema en modo edge-to-edge con tema oscuro
     * 2. Inicializa el gestor de permisos de Bluetooth
     * 3. Configura el contenido con Jetpack Compose
     * 4. Establece el sistema de navegación
     * 5. Muestra el diálogo de permisos si es necesario
     * 
     * @param savedInstanceState Estado guardado de la actividad si fue destruida previamente
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure initial edge-to-edge styling with dark theme
        // The application uses only dark mode
        configureSystemBars(true)

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
            CamperGasTheme(themeMode = ThemeMode.DARK) {
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
     * Configura el estilo de las barras del sistema con tema oscuro.
     * 
     * Este método aplica el estilo apropiado a la barra de estado y la barra de navegación
     * para el tema oscuro. Utiliza colores transparentes para permitir que el contenido 
     * se extienda hasta los bordes de la pantalla (edge-to-edge).
     * 
     * @param isDarkTheme siempre true ya que la aplicación usa solo tema oscuro
     */
    private fun configureSystemBars(isDarkTheme: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
    }
}