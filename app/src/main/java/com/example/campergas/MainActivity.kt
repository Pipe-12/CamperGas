package com.example.campergas

import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.rememberNavController
import com.example.campergas.R
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.domain.model.Language
import com.example.campergas.domain.model.ThemeMode
import com.example.campergas.ui.components.PermissionDialog
import com.example.campergas.ui.navigation.NavGraph
import com.example.campergas.ui.theme.CamperGasTheme
import com.example.campergas.utils.BluetoothPermissionManager
import com.example.campergas.utils.LocaleUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataStore: PreferencesDataStore

    private lateinit var bluetoothPermissionManager: BluetoothPermissionManager

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
    }

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
            val language by preferencesDataStore.language.collectAsState(initial = Language.SYSTEM)

            // Apply language configuration and recreate activity when language changes
            var previousLanguage by remember { mutableStateOf<Language?>(null) }
            var isRecreating by remember { mutableStateOf(false) }
            
            LaunchedEffect(language) {
                // Apply locale immediately when language changes
                LocaleUtils.applyLocaleToActivity(this@MainActivity, language)
                
                // Only recreate if language actually changed, we have a previous language,
                // and we're not already in the process of recreating
                if (previousLanguage != null && 
                    previousLanguage != language && 
                    !isRecreating) {
                    
                    isRecreating = true
                    // Small delay to ensure all state updates are complete
                    kotlinx.coroutines.delay(150)
                    recreate()
                }
                previousLanguage = language
            }

            CamperGasTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // Estado para controlar si mostrar el diálogo de permisos
                    var showPermissionDialog by remember { mutableStateOf(false) }

                    // Verificar permisos al iniciar
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
}