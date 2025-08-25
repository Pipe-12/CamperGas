package com.example.campergas.utils

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.util.Log

/**
 * Utility class to handle foreground service start restrictions in Android 12+
 */
object ForegroundServiceUtils {
    
    private const val TAG = "ForegroundServiceUtils"
    
    /**
     * Checks if the app can start a foreground service from the current context
     * 
     * Starting from Android 12 (API 31), there are strict limitations on when
     * background apps can start foreground services. This method checks if
     * the current context allows starting foreground services.
     */
    fun canStartForegroundService(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android 12+, check if app is in foreground
            isAppInForeground(context)
        } else {
            // For Android 11 and below, foreground services can be started more freely
            true
        }
    }
    
    /**
     * Checks if the application is currently in the foreground
     */
    private fun isAppInForeground(context: Context): Boolean {
        return try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val appProcesses = activityManager.runningAppProcesses
            
            if (appProcesses.isNullOrEmpty()) {
                Log.d(TAG, "No running app processes found")
                return false
            }
            
            val packageName = context.packageName
            val foregroundProcess = appProcesses.find { 
                it.processName == packageName && 
                it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND 
            }
            
            val isInForeground = foregroundProcess != null
            Log.d(TAG, "App foreground status: $isInForeground")
            return isInForeground
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking foreground status", e)
            // Default to false for safety when we can't determine status
            false
        }
    }
    
    /**
     * Safely attempts to start a foreground service, with fallback to regular service
     * Returns true if any service was started successfully
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
                // Try to start as foreground service
                Log.d(TAG, "Starting foreground service for ${serviceClass.simpleName}")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
                true
            } else {
                // Fallback: start as regular service (widgets will still get updates when app is opened)
                Log.d(TAG, "Cannot start foreground service, starting regular service for ${serviceClass.simpleName}")
                context.startService(intent)
                true
            }
        } catch (e: Exception) {
            // Check if this is a ForegroundServiceStartNotAllowedException (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && 
                e::class.java.simpleName == "ForegroundServiceStartNotAllowedException") {
                Log.w(TAG, "Foreground service start not allowed, attempting regular service start", e)
                return try {
                    val intent = android.content.Intent(context, serviceClass).apply {
                        configureIntent(this)
                    }
                    context.startService(intent)
                    true
                } catch (regularServiceException: Exception) {
                    Log.e(TAG, "Failed to start regular service as fallback", regularServiceException)
                    false
                }
            } else {
                Log.e(TAG, "Failed to start service", e)
                false
            }
        }
    }
}