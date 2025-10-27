package com.example.campergas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.campergas.domain.model.GasCylinder

/**
 * Base de datos Room principal para la aplicación CamperGas.
 *
 * Esta base de datos gestiona la información de los cilindros de gas y las mediciones de combustible,
 * proporcionando persistencia para datos de sensores en tiempo real y seguimiento histórico.
 *
 * @property gasCylinderDao Objeto de acceso a datos para operaciones de cilindros de gas
 * @property fuelMeasurementDao Objeto de acceso a datos para operaciones de mediciones de combustible
 */
@Database(
    entities = [
        GasCylinder::class,
        FuelMeasurementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CamperGasDatabase : RoomDatabase() {

    /**
     * Proporciona acceso a las operaciones de base de datos de cilindros de gas.
     *
     * @return DAO para gestionar entidades de cilindros de gas
     */
    abstract fun gasCylinderDao(): GasCylinderDao

    /**
     * Proporciona acceso a las operaciones de base de datos de mediciones de combustible.
     *
     * @return DAO para gestionar entidades de mediciones de combustible
     */
    abstract fun fuelMeasurementDao(): FuelMeasurementDao

    companion object {
        /** Nombre del archivo de base de datos */
        const val DATABASE_NAME = "campergas_database"
    }
}
