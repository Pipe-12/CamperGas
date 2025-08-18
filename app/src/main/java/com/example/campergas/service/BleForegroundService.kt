package com.example.campergas.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.campergas.data.repository.BleRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BleForegroundService : Service() {

    @Inject
    lateinit var bleRepository: BleRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val notificationId = 123
    private val channelId = "ble_service_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("CamperGas en funcionamiento")
        startForeground(notificationId, notification)

        val deviceAddress = intent?.getStringExtra(KEY_DEVICE_ADDRESS)

        if (deviceAddress != null) {
            connectToDevice(deviceAddress)
        }

        // Retornamos START_STICKY para que el servicio se reinicie automáticamente si el sistema lo mata
        return START_STICKY
    }

    private fun connectToDevice(deviceAddress: String) {
        serviceScope.launch {
            try {
                bleRepository.connectToSensor(deviceAddress)

                // Monitorear las mediciones de combustible y actualizar la notificación
                launch {
                    bleRepository.fuelMeasurementData.collect { fuelMeasurement ->
                        if (fuelMeasurement != null) {
                            updateNotification("Combustible: ${fuelMeasurement.getFormattedFuelKilograms()} (${fuelMeasurement.getFormattedPercentage()})")
                        }
                    }
                }

                // Guardar la dirección del dispositivo conectado
                bleRepository.saveLastConnectedDevice(deviceAddress)

            } catch (_: Exception) {
                stopSelf()
            }
        }
    }

    private fun createNotificationChannel() {
        val name = "CamperGas BLE Service"
        val descriptionText = "Canal para el servicio BLE de CamperGas"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(channelId, name, importance).apply {
            description = descriptionText
        }

        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(message: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("CamperGas")
            .setContentText(message)
            // .setSmallIcon(R.drawable.ic_notification) // Debes crear este icono
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(message: String) {
        val notification = createNotification(message)
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    override fun onDestroy() {
        serviceScope.launch {
            bleRepository.disconnectSensor()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val KEY_DEVICE_ADDRESS = "device_address"

//        fun startService(context: Context, deviceAddress: String) {
//            val intent = Intent(context, BleForegroundService::class.java).apply {
//                putExtra(KEY_DEVICE_ADDRESS, deviceAddress)
//            }
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(intent)
//            } else {
//                context.startService(intent)
//            }
//        }
//
//        fun stopService(context: Context) {
//            val intent = Intent(context, BleForegroundService::class.java)
//            context.stopService(intent)
//        }
    }
}
