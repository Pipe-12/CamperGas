package com.example.campergas.data.repository

import com.example.campergas.data.local.db.GasCylinderDao
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar cilindros de gas en la base de datos.
 * 
 * Este repositorio proporciona una capa de abstracción para todas las operaciones
 * CRUD relacionadas con cilindros de gas. Gestiona tanto la persistencia de datos
 * como la lógica de negocio relacionada con el estado activo de los cilindros.
 * 
 * Funcionalidad clave:
 * - Solo un cilindro puede estar activo a la vez
 * - Al activar un cilindro, los demás se desactivan automáticamente
 * - Soporte para múltiples cilindros (diferentes tipos, tamaños, capacidades)
 * - Acceso reactivo mediante Flows para actualización automática de UI
 * 
 * Los cilindros almacenan:
 * - Nombre descriptivo
 * - Tara (peso vacío) para cálculos precisos de gas disponible
 * - Capacidad máxima de gas
 * - Estado activo/inactivo
 * - Fecha de creación
 * 
 * @property gasCylinderDao DAO de Room para acceso a la base de datos
 * @author Felipe García Gómez
 */
@Singleton
class GasCylinderRepository @Inject constructor(
    private val gasCylinderDao: GasCylinderDao
) {

    /**
     * Obtiene todos los cilindros de gas registrados.
     * 
     * @return Flow que emite la lista de todos los cilindros ordenados por fecha de creación
     */
    fun getAllCylinders(): Flow<List<GasCylinder>> = gasCylinderDao.getAllCylinders()

    /**
     * Obtiene el cilindro actualmente activo.
     * 
     * Solo un cilindro puede estar activo a la vez. El cilindro activo es el
     * que se utiliza para todas las mediciones actuales de peso y gas.
     * 
     * @return Flow que emite el cilindro activo o null si no hay ninguno activo
     */
    fun getActiveCylinder(): Flow<GasCylinder?> = gasCylinderDao.getActiveCylinder()

    /**
     * Obtiene el cilindro activo de forma síncrona.
     * 
     * Ejecuta una consulta directa sin crear suscripción reactiva.
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @return El cilindro activo o null si no hay ninguno activo
     */
    suspend fun getActiveCylinderSync(): GasCylinder? = gasCylinderDao.getActiveCylinderSync()

    /**
     * Obtiene un cilindro específico por su ID.
     * 
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @param id ID del cilindro a buscar
     * @return El cilindro encontrado o null si no existe
     */
    suspend fun getCylinderById(id: Long): GasCylinder? = gasCylinderDao.getCylinderById(id)

    /**
     * Obtiene todos los cilindros de forma síncrona.
     * 
     * Ejecuta una consulta directa sin crear suscripción reactiva.
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @return Lista de todos los cilindros
     */
    suspend fun getAllCylindersSync(): List<GasCylinder> = gasCylinderDao.getAllCylindersSync()

    /**
     * Inserta un nuevo cilindro en la base de datos.
     * 
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @param cylinder Cilindro a insertar
     * @return ID asignado al cilindro insertado
     */
    suspend fun insertCylinder(cylinder: GasCylinder): Long {
        return gasCylinderDao.insertCylinder(cylinder)
    }


    /**
     * Establece un cilindro como activo, desactivando todos los demás.
     * 
     * Esta operación es atómica: primero desactiva todos los cilindros
     * y luego activa el especificado. Garantiza que solo un cilindro
     * esté activo a la vez.
     * 
     * El cilindro activo es crítico porque:
     * - Todas las mediciones de peso se asocian a este cilindro
     * - Su tara se usa para calcular el gas disponible
     * - Define el 100% de capacidad para calcular porcentajes
     * 
     * Esta función debe llamarse desde una coroutine o función suspend.
     * 
     * @param cylinderId ID del cilindro a activar
     */
    suspend fun setActiveCylinder(cylinderId: Long) {
        gasCylinderDao.setActiveCylinder(cylinderId)
    }


}
