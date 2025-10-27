package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para operaciones de bombonas de gas.
 *
 * Proporciona métodos para gestionar bombonas de gas, incluyendo inserción, actualizaciones,
 * consultas y manejo del estado de la bombona activa. Solo una bombona puede estar activa
 * a la vez.
 */
@Dao
interface GasCylinderDao {

    /**
     * Recupera todas las bombonas de gas ordenadas por fecha de creación.
     *
     * @return Flow que emite lista de todas las bombonas, las más recientes primero
     */
    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    fun getAllCylinders(): Flow<List<GasCylinder>>

    /**
     * Recupera todas las bombonas de gas de forma síncrona.
     *
     * @return Lista de todas las bombonas, las más recientes primero
     */
    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    suspend fun getAllCylindersSync(): List<GasCylinder>

    /**
     * Recupera la bombona de gas actualmente activa.
     *
     * Solo una bombona puede estar activa a la vez.
     *
     * @return Flow que emite la bombona activa, o null si ninguna está activa
     */
    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    fun getActiveCylinder(): Flow<GasCylinder?>

    /**
     * Recupera la bombona de gas actualmente activa de forma síncrona.
     *
     * @return La bombona activa, o null si ninguna está activa
     */
    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCylinderSync(): GasCylinder?

    /**
     * Recupera una bombona específica por su ID.
     *
     * @param id ID de la bombona a recuperar
     * @return La bombona con el ID especificado, o null si no se encuentra
     */
    @Query("SELECT * FROM gas_cylinders WHERE id = :id")
    suspend fun getCylinderById(id: Long): GasCylinder?

    /**
     * Inserta una nueva bombona de gas.
     *
     * @param cylinder La bombona a insertar
     * @return ID de fila de la bombona insertada
     */
    @Insert
    suspend fun insertCylinder(cylinder: GasCylinder): Long

    /**
     * Actualiza una bombona de gas existente.
     *
     * @param cylinder La bombona a actualizar
     */
    @Update
    suspend fun updateCylinder(cylinder: GasCylinder)

    /**
     * Desactiva todas las bombonas de gas.
     *
     * Se usa internamente para asegurar que solo una bombona esté activa a la vez.
     */
    @Query("UPDATE gas_cylinders SET isActive = 0")
    suspend fun deactivateAllCylinders()

    /**
     * Establece una bombona como activa, desactivando todas las demás.
     *
     * Esta es una operación transaccional que asegura la consistencia de datos.
     *
     * @param cylinderId ID de la bombona a establecer como activa
     */
    @Transaction
    suspend fun setActiveCylinder(cylinderId: Long) {
        deactivateAllCylinders()
        updateCylinderActiveStatus(cylinderId, true)
    }

    /**
     * Actualiza el estado activo de una bombona específica.
     *
     * @param id ID de la bombona a actualizar
     * @param isActive Nuevo estado activo
     */
    @Query("UPDATE gas_cylinders SET isActive = :isActive WHERE id = :id")
    suspend fun updateCylinderActiveStatus(id: Long, isActive: Boolean)
}
