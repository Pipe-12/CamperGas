package com.example.campergas

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.navigation.compose.rememberNavController
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.ui.components.PermissionDialog
import com.example.campergas.ui.navigation.NavGraph
import com.example.campergas.ui.theme.CamperGasTheme
import com.example.campergas.utils.BluetoothPermissionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    private lateinit var bluetoothPermissionManager: BluetoothPermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configurar el gestor de permisos
        bluetoothPermissionManager = BluetoothPermissionManager(
            activity = this,
            onPermissionsGranted = {
                // Permisos concedidos, la app puede usar BLE
            },
            onPermissionsDenied = { deniedPermissions ->
                // Manejar permisos denegados
            }
        )

        setContent {
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            CamperGasTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Estado para controlar si mostrar el diálogo de permisos
                    var showPermissionDialog by remember { mutableStateOf(false) }
                    var permissionMessage by remember { mutableStateOf("") }

                    // Verificar permisos al iniciar
                    LaunchedEffect(Unit) {
                        if (!bluetoothPermissionManager.hasAllPermissions()) {
                            showPermissionDialog = true
                            permissionMessage =
                                "CamperGas necesita permisos de Bluetooth y ubicación para funcionar correctamente."
                        }
                    }

                    // Mostrar diálogo de permisos si es necesario
                    if (showPermissionDialog) {
                        PermissionDialog(
                            title = "Permisos necesarios",
                            message = permissionMessage,
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
}