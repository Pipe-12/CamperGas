package com.example.campergas.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para operaciones de cilindros de gas.
 *
 * Proporciona métodos para gestionar cilindros de gas, incluyendo inserción, actualizaciones,
 * consultas y manejo del estado del cilindro activo. Solo un cilindro puede estar activo
 * a la vez.
 */
@Dao
interface GasCylinderDao {

    /**
     * Recupera todos los cilindros de gas ordenados por fecha de creación.
     *
     * @return Flow que emite lista de todos los cilindros, los más recientes primero
     */
    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    fun getAllCylinders(): Flow<List<GasCylinder>>

    /**
     * Recupera todos los cilindros de gas de forma síncrona.
     *
     * @return Lista de todos los cilindros, los más recientes primero
     */
    @Query("SELECT * FROM gas_cylinders ORDER BY createdAt DESC")
    suspend fun getAllCylindersSync(): List<GasCylinder>

    /**
     * Recupera el cilindro de gas actualmente activo.
     *
     * Solo un cilindro puede estar activo a la vez.
     *
     * @return Flow que emite el cilindro activo, o null si ninguno está activo
     */
    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    fun getActiveCylinder(): Flow<GasCylinder?>

    /**
     * Recupera el cilindro de gas actualmente activo de forma síncrona.
     *
     * @return El cilindro activo, o null si ninguno está activo
     */
    @Query("SELECT * FROM gas_cylinders WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCylinderSync(): GasCylinder?

    /**
     * Recupera un cilindro específico por su ID.
     *
     * @param id ID del cilindro a recuperar
     * @return El cilindro con el ID especificado, o null si no se encuentra
     */
    @Query("SELECT * FROM gas_cylinders WHERE id = :id")
    suspend fun getCylinderById(id: Long): GasCylinder?

    /**
     * Inserta un nuevo cilindro de gas.
     *
     * @param cylinder El cilindro a insertar
     * @return ID de fila del cilindro insertado
     */
    @Insert
    suspend fun insertCylinder(cylinder: GasCylinder): Long

    /**
     * Actualiza un cilindro de gas existente.
     *
     * @param cylinder El cilindro a actualizar
     */
    @Update
    suspend fun updateCylinder(cylinder: GasCylinder)

    /**
     * Desactiva todos los cilindros de gas.
     *
     * Se usa internamente para asegurar que solo un cilindro esté activo a la vez.
     */
    @Query("UPDATE gas_cylinders SET isActive = 0")
    suspend fun deactivateAllCylinders()

    /**
     * Establece un cilindro como activo, desactivando todos los demás.
     *
     * Esta es una operación transaccional que asegura la consistencia de datos.
     *
     * @param cylinderId ID del cilindro a establecer como activo
     */
    @Transaction
    suspend fun setActiveCylinder(cylinderId: Long) {
        deactivateAllCylinders()
        updateCylinderActiveStatus(cylinderId, true)
    }

    /**
     * Actualiza el estado activo de un cilindro específico.
     *
     * @param id ID del cilindro a actualizar
     * @param isActive Nuevo estado activo
     */
    @Query("UPDATE gas_cylinders SET isActive = :isActive WHERE id = :id")
    suspend fun updateCylinderActiveStatus(id: Long, isActive: Boolean)
}
