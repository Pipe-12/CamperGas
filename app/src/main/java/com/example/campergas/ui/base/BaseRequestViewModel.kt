package com.example.campergas.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campergas.domain.usecase.CheckBleConnectionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel para manejar peticiones manuales con cooldown y control de spam
 * Centraliza la lógica común de los ViewModels que hacen peticiones BLE bajo demanda
 */
abstract class BaseRequestViewModel(
    private val checkBleConnectionUseCase: CheckBleConnectionUseCase
) : ViewModel() {

    // Control de peticiones para evitar spam
    private var lastRequestTime = 0L
    private val requestCooldownMs = 2000L // 2 segundos entre peticiones

    private val _isRequestingData = MutableStateFlow(false)
    val isRequestingData: StateFlow<Boolean> = _isRequestingData

    /**
     * Ejecuta una petición manual con protección contra spam
     * @param requestAction La acción específica a ejecutar (use case específico)
     * @param logTag Tag para los logs de debug
     * @param dataTypeDescription Descripción del tipo de datos para los logs
     */
    protected fun executeManualRequest(
        requestAction: () -> Unit,
        logTag: String,
        dataTypeDescription: String
    ) {
        val currentTime = System.currentTimeMillis()

        // Verificar si ha pasado suficiente tiempo desof the última petición
        if (currentTime - lastRequestTime < requestCooldownMs) {
            android.util.Log.d(logTag, "⏱️ Petición bloqueada - cooldown activo")
            return
        }

        // Verificar si ya hay una petición en curso
        if (_isRequestingData.value) {
            android.util.Log.d(logTag, "⏱️ Petición bloqueada - ya hay una en curso")
            return
        }

        android.util.Log.d(logTag, "📊 Solicitando datos de $dataTypeDescription manualmente")
        _isRequestingData.value = true
        lastRequestTime = currentTime

        requestAction()

        // Resetear el estado después de un tiempo razonable
        viewModelScope.launch {
            kotlinx.coroutines.delay(1500) // 1.5 segundos
            _isRequestingData.value = false
        }
    }

    /**
     * Verifica si hay una conexión BLE activa
     */
    fun isConnected(): Boolean {
        return checkBleConnectionUseCase.isConnected()
    }

    /**
     * Verifica si se puede hacer una nueva petición (no está en cooldown)
     */
    fun canMakeRequest(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastRequestTime >= requestCooldownMs) && !_isRequestingData.value
    }
}