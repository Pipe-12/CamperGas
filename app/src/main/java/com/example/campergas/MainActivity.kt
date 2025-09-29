package com.example.campergas

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
        val context = newBase ?: return super.attachBaseContext(newBase)
        
        // Apply the saved locale setting to preserve user's language choice
        // This ensures language persists across activity recreation (e.g., language changes)
        try {
            // Create temporary preferences data store to get saved language
            val tempPreferences = PreferencesDataStore(context)
            val savedLanguage = try {
                runBlocking { tempPreferences.language.first() }
            } catch (e: Exception) {
                Language.SYSTEM // Fallback to system if loading fails
            }
            
            val wrappedContext = LocaleUtils.setLocale(context, savedLanguage)
            super.attachBaseContext(wrappedContext)
        } catch (e: Exception) {
            // Fallback to original context if locale setting fails
            super.attachBaseContext(newBase)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure initial edge-to-edge styling with user's saved theme preference
        // This prevents white system bars during activity recreation (e.g., when language changes)
        
        // Load saved theme preference to determine correct system bar styling
        val savedThemeMode = try {
            runBlocking { preferencesDataStore.themeMode.first() }
        } catch (e: Exception) {
            ThemeMode.LIGHT // Fallback to light theme if loading fails
        }
        
        val isDarkThemeForSystemBars = when (savedThemeMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }
        
        // Apply system bars configuration immediately to prevent white flash
        configureSystemBars(isDarkThemeForSystemBars)

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
            val themeMode by preferencesDataStore.themeMode.collectAsState(initial = ThemeMode.LIGHT)
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

    /**
     * Configure system bars (status bar and navigation bar) styling based on theme
     * This method ensures consistent styling and prevents white flash during activity recreation
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