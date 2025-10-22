package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Caso de uso para verificar el estado de conexión BLE con sensores CamperGas.
 * 
 * Este caso de uso encapsula la lógica de negocio para monitorear el estado
 * de conexión con el sensor BLE. Proporciona acceso tanto reactivo (Flow)
 * como directo al estado de conexión.
 * 
 * El estado de conexión es crítico para:
 * - Habilitar/deshabilitar funcionalidades que requieren sensor conectado
 * - Mostrar indicadores visuales de conexión en la UI
 * - Decidir si iniciar lectura de datos o mostrar pantalla de conexión
 * - Manejar reconexiones automáticas cuando se pierde la conexión
 * 
 * Estados posibles:
 * - true: Sensor BLE conectado y funcional
 * - false: Sin sensor conectado o conexión perdida
 * 
 * @property bleRepository Repositorio BLE que gestiona el estado de conexión
 * @author Felipe García Gómez
 */
class CheckBleConnectionUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Obtiene el estado de conexión BLE como un StateFlow reactivo.
     * 
     * Retorna un StateFlow que emite el estado actual de conexión y se actualiza
     * automáticamente cuando el estado cambia (conectado/desconectado).
     * Permite a la UI reaccionar en tiempo real a cambios de conexión.
     * 
     * @return StateFlow que emite true si hay conexión activa, false si no hay conexión
     */
    operator fun invoke(): StateFlow<Boolean> {
        return bleRepository.connectionState
    }

    /**
     * Verifica si existe una conexión BLE activa en este momento.
     * 
     * Obtiene el valor actual del estado de conexión sin crear una suscripción
     * reactiva. Útil para verificaciones puntuales.
     * 
     * @return true si hay un sensor BLE conectado, false en caso contrario
     */
    fun isConnected(): Boolean {
        return bleRepository.connectionState.value
    }
}
