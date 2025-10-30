package com.example.campergas.data.local.vehicle

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Objeto de Acceso a Datos (DAO) para operaciones de configuración de vehículo.
 *
 * Gestiona la persistencia de las dimensiones físicas del vehículo y configuración de tipo,
 * que se utilizan para cálculos de estabilidad y visualización en la UI.
 */
@Dao
interface VehicleDao {
    /**
     * Inserta o reemplaza la configuración del vehículo.
     *
     * @param config La configuración del vehículo a insertar
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicleConfig(config: VehicleConfigEntity)

    /**
     * Actualiza una configuración de vehículo existente.
     *
     * @param config La configuración del vehículo a actualizar
     * @return Número de filas actualizadas
     */
    @Update
    suspend fun updateVehicleConfig(config: VehicleConfigEntity): Int

    /**
     * Recupera la configuración del vehículo como un Flow.
     *
     * @param id ID de configuración (por defecto: "default_config")
     * @return Flow que emite la configuración del vehículo, o null si no se encuentra
     */
    @Query("SELECT * FROM vehicle_config WHERE id = :id")
    fun getVehicleConfig(id: String = "default_config"): Flow<VehicleConfigEntity?>

    /**
     * Verifica si existe una configuración de vehículo.
     *
     * @param id ID de configuración (por defecto: "default_config")
     * @return True si la configuración existe, false en caso contrario
     */
    @Query("SELECT EXISTS(SELECT 1 FROM vehicle_config WHERE id = :id)")
    suspend fun configExists(id: String = "default_config"): Boolean
}
