package com.example.campergas.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Clase de utilidad para manejar las restricciones de inicio de servicios en primer plano en Android 12+
 */
object ForegroundServiceUtils {
    
    private const val TAG = "ForegroundServiceUtils"
    
    /**
     * Verifica si la aplicación puede iniciar un servicio en primer plano desde el contexto actual
     * 
     * A partir de Android 12 (API 31), existen limitaciones estrictas sobre cuándo
     * las aplicaciones en segundo plano pueden iniciar servicios en primer plano. Este método
     * verifica si el contexto actual permite iniciar servicios en primer plano.
     */
    fun canStartForegroundService(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Para Android 12+, verificar si la aplicación está en primer plano
            isAppInForeground(context)
        } else {
            // Para Android 11 y anteriores, los servicios en primer plano se pueden iniciar más libremente
            true
        }
    }
    
    /**
     * Verifica si la aplicación está actualmente en primer plano
     */
    private fun isAppInForeground(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            
            if (appProcesses.isNullOrEmpty()) {
                Log.d(TAG, "No se encontraron procesos de aplicación en ejecución")
                return false
            }
            
            val packageName = context.packageName
            val foregroundProcess = appProcesses.find { 
                it.processName == packageName && 
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
            }
            
            val isInForeground = foregroundProcess != null
            Log.d(TAG, "Estado de primer plano de la aplicación: $isInForeground")
            return isInForeground
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar el estado de primer plano", e)
            // Por defecto falso por seguridad cuando no podemos determinar el estado
            false
        }
    }
    
    /**
     * Verifica si un servicio específico está actualmente ejecutándose
     */
    fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            
            // Para Android 7.0+ (API 24), usar getRunningServices con límite menor por rendimiento
            val maxServices = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 100 else Integer.MAX_VALUE
            @Suppress("DEPRECATION") // Necesario para compatibilidad con widgets
            val runningServices = activityManager.getRunningServices(maxServices)
            
            val serviceName = serviceClass.name
            val isRunning = runningServices.any { service ->
                service.service.className == serviceName
            }
            
            Log.d(TAG, "Servicio ${serviceClass.simpleName} ejecutándose: $isRunning")
            return isRunning
            
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar si el servicio está ejecutándose", e)
            // En caso de error, asumimos que no está ejecutándose para permitir reintentos
            false
        }
    }
    
    /**
     * Intenta iniciar un servicio en primer plano de forma segura, con respaldo a servicio regular
     * Devuelve true si algún servicio se inició exitosamente
     */
    fun startServiceSafely(
        context: Context,
        serviceClass: Class<*>,
        configureIntent: (android.content.Intent) -> Unit = {}
    ): Boolean {
        return try {
            val intent = android.content.Intent(context, serviceClass).apply {
                configureIntent(this)
            }
            
            if (canStartForegroundService(context)) {
                // Intentar iniciar como servicio en primer plano
                Log.d(TAG, "Iniciando servicio en primer plano para ${serviceClass.simpleName}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                true
            } else {
                // Respaldo: iniciar como servicio regular (los widgets seguirán recibiendo actualizaciones cuando se abra la aplicación)
                Log.d(TAG, "No se puede iniciar servicio en primer plano, iniciando servicio regular para ${serviceClass.simpleName}")
                context.startService(intent)
                true
            }
        } catch (e: Exception) {
            // Verificar si esta es una ForegroundServiceStartNotAllowedException (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                e::class.java.simpleName == "ForegroundServiceStartNotAllowedException") {
                Log.w(TAG, "Inicio de servicio en primer plano no permitido, intentando iniciar servicio regular", e)
                return try {
                    val intent = android.content.Intent(context, serviceClass).apply {
                        configureIntent(this)
                    }
                    context.startService(intent)
                    true
                } catch (regularServiceException: Exception) {
                    Log.e(TAG, "Error al iniciar servicio regular como respaldo", regularServiceException)
                    false
                }
            } else {
                Log.e(TAG, "Error al iniciar servicio", e)
                false
            }
        }
    }
    
    /**
     * Intenta iniciar un servicio de forma segura solo si no está ya ejecutándose
     * Evita múltiples intentos de inicio que pueden causar bucles infinitos
     */
    fun startServiceSafelyIfNotRunning(
        context: Context,
        serviceClass: Class<*>,
        configureIntent: (android.content.Intent) -> Unit = {}
    ): Boolean {
        return if (isServiceRunning(context, serviceClass)) {
            Log.d(TAG, "Servicio ${serviceClass.simpleName} ya está ejecutándose - no se requiere acción")
            true
        } else {
            Log.d(TAG, "Servicio ${serviceClass.simpleName} no está ejecutándose - iniciando...")
            startServiceSafely(context, serviceClass, configureIntent)
        }
    }
}