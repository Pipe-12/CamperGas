package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Caso de uso para obtener la lista de todos los cilindros de gas registrados.
 *
 * Este caso de uso encapsula la lógica de negocio para recuperar todos los cilindros
 * de gas almacenados en el sistema, así como acceder al cilindro activo actual.
 *
 * Funcionalidades proporcionadas:
 * - Obtener lista completa de cilindros (reactiva con Flow)
 * - Obtener el cilindro activo actual (reactiva y síncrona)
 *
 * Casos de uso comunes:
 * - Mostrar lista de cilindros en pantalla de configuración
 * - Permitir al usuario seleccionar un cilindro para activar
 * - Visualizar todos los cilindros con sus características (tara, capacidad)
 * - Gestionar múltiples cilindros (añadir, editar, activar, eliminar)
 *
 * @property repository Repositorio de cilindros de gas que accede a la base de datos
 * @author Felipe García Gómez
 */
class GetGasCylindersUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    /**
     * Obtiene todos los cilindros de gas como un Flow reactivo.
     *
     * Devuelve un Flow que emite la lista completa de cilindros registrados
     * y se actualiza automáticamente cuando hay cambios (añadir, eliminar, modificar).
     *
     * @return Flow que emite la lista de todos los cilindros (puede estar vacía si no hay cilindros)
     */
    operator fun invoke(): Flow<List<GasCylinder>> {
        return repository.getAllCylinders()
    }

}
