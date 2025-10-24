package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import javax.inject.Inject

/**
 * Caso de uso para establecer un cilindro de gas como activo en el sistema.
 * 
 * Este caso de uso encapsula la lógica de negocio para cambiar qué cilindro de gas
 * está actualmente activo. Solo un cilindro puede estar activo a la vez, y el cambio
 * de cilindro activo desactiva automáticamente el anterior.
 * 
 * El cilindro activo es crucial porque:
 * - Todas las mediciones de peso se asocian a este cilindro
 * - La tara del cilindro activo se usa para calcular el gas disponible
 * - Los widgets y pantallas principales muestran información de este cilindro
 * 
 * Validaciones realizadas:
 * - Verifica que el cilindro existe antes de activarlo
 * - Captura y reporta errores de base de datos
 * 
 * Casos de uso comunes:
 * - Cambiar de cilindro de butano a propano
 * - Activar un cilindro de repuesto cuando se agota el principal
 * - Seleccionar el cilindro correcto después de añadir varios
 * 
 * @property repository Repositorio de cilindros de gas que accede a la base de datos
 * @author Felipe García Gómez
 */
class SetActiveCylinderUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    /**
     * Establece un cilindro específico como activo en el sistema.
     * 
     * Busca el cilindro por su ID y, si existe, lo marca como activo.
     * Automáticamente desactiva cualquier otro cilindro que estuviera activo.
     * 
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @param cylinderId ID del cilindro que se desea activar
     * @return Result.success(Unit) si el cilindro se activó correctamente
     * @return Result.failure con IllegalArgumentException si el cilindro no existe
     * @return Result.failure con la excepción correspondiente si hay error de base de datos
     */
    suspend operator fun invoke(cylinderId: Long): Result<Unit> {
        return try {
            val cylinder = repository.getCylinderById(cylinderId)
            if (cylinder != null) {
                repository.setActiveCylinder(cylinderId)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Bombona no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
