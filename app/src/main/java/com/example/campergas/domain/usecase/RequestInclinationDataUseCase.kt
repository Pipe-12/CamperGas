package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import javax.inject.Inject

/**
 * Caso de uso para solicitar una lectura inmediata de inclinación del sensor BLE.
 *
 * Este caso de uso encapsula la lógica de negocio para forzar una lectura
 * de inclinación (pitch y roll) del sensor BLE fuera del ciclo de lecturas
 * periódicas automáticas.
 *
 * Utilidad principal:
 * - Obtener medición actualizada cuando el usuario necesita verificar la nivelación
 * - Actualizar datos de estabilidad antes de mostrar widgets o gráficos
 * - Verificar inclinación sin esperar al próximo ciclo automático
 * - Permitir al usuario "refrescar" manualmente los datos de nivelación
 *
 * Funcionamiento:
 * El sensor BLE lee la característica de inclinación bajo demanda (READ operation).
 * La respuesta se procesa de forma asíncrona y actualiza el Flow de datos
 * de inclinación automáticamente cuando llega.
 *
 * Diferencia con lecturas automáticas:
 * - Lecturas automáticas: Ocurren cada X segundos según configuración
 * - Lectura bajo demanda: Ocurre inmediatamente al invocar este caso de uso
 *
 * Requisitos:
 * - Debe haber un sensor BLE conectado
 * - El sensor debe tener acelerómetro/giroscopio funcional
 *
 * @property bleRepository Repositorio BLE que gestiona la comunicación con el sensor
 * @author Felipe García Gómez
 */
class RequestInclinationDataUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    /**
     * Solicita inmediatamente una lectura de inclinación del sensor BLE.
     *
     * Ejecuta una lectura BLE de la característica de inclinación sin esperar
     * al próximo ciclo de lectura automática. El resultado se entregará de
     * forma asíncrona a través del Flow de datos de inclinación.
     *
     * La invocación es segura incluso si no hay sensor conectado (no hace nada
     * en ese caso). No bloquea la ejecución.
     */
    operator fun invoke() {
        bleRepository.readInclinationDataOnDemand()
    }
}
