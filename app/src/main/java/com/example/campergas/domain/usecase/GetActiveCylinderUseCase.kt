package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener el cilindro de gas actualmente activo.
 *
 * Este caso de uso encapsula la lógica de negocio para recuperar el cilindro
 * de gas que está marcado como activo en el sistema. El cilindro activo es el
 * que se utiliza para las mediciones actuales de peso y consumo de gas.
 *
 * Proporciona dos variantes de acceso:
 * - Reactiva (Flow): Para observar cambios en tiempo real del cilindro activo
 * - Síncrona (suspend): Para obtener el valor actual en un momento específico
 *
 * Casos de uso principales:
 * - Mostrar información del cilindro actual en la pantalla principal
 * - Calcular gas disponible usando la tara del cilindro activo
 * - Asociar mediciones de peso al cilindro correcto
 * - Validar que existe un cilindro activo antes de realizar operaciones
 *
 * @property repository Repositorio de cilindros de gas que accede a la base de datos
 * @author Felipe García Gómez
 */
class GetActiveCylinderUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    /**
     * Obtiene el cilindro activo como un Flow reactivo.
     *
     * Devuelve un Flow que emite el cilindro activo actual y se actualiza
     * automáticamente cuando cambia en la base de datos. Permite a la UI
     * reaccionar a cambios en tiempo real.
     *
     * @return Flow que emite el cilindro activo o null si no hay ninguno activo
     */
    operator fun invoke(): Flow<GasCylinder?> {
        return repository.getActiveCylinder()
    }

    /**
     * Obtiene el cilindro activo de forma síncrona.
     *
     * Ejecuta una consulta directa a la base de datos para obtener el cilindro
     * activo actual. Útil cuando se necesita el valor en un momento específico
     * sin observar cambios continuos.
     *
     * Esta función debe llamarse desde una coroutine o función suspend.
     *
     * @return El cilindro activo actual o null si no hay ninguno activo
     */
    suspend fun getActiveCylinderSync(): GasCylinder? {
        return repository.getActiveCylinderSync()
    }
}
