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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
 * - La configuración del tema (claro, oscuro o del sistema)
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
     * 1. Carga el tema guardado desde las preferencias del usuario
     * 2. Configura las barras del sistema en modo edge-to-edge según el tema
     * 3. Inicializa el gestor de permisos de Bluetooth
     * 4. Configura el contenido con Jetpack Compose aplicando el tema seleccionado
     * 5. Establece el sistema de navegación
     * 6. Muestra el diálogo de permisos si es necesario
     * 
     * @param savedInstanceState Estado guardado de la actividad si fue destruida previamente
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
     * Configura el estilo de las barras del sistema según el tema actual.
     * 
     * Este método aplica el estilo apropiado a la barra de estado y la barra de navegación
     * basándose en el tema seleccionado (claro u oscuro). Utiliza colores transparentes 
     * para permitir que el contenido se extienda hasta los bordes de la pantalla (edge-to-edge).
     * 
     * @param isDarkTheme true para aplicar tema oscuro, false para tema claro
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