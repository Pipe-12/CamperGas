package com.example.campergas.data.local.vehicle

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Base de datos Room para almacenamiento de configuración de vehículo.
 *
 * Mantiene las dimensiones físicas del vehículo e información de tipo utilizada para
 * cálculos de estabilidad. Incluye migración de versión 1 a 2 para soportar
 * tipos de vehículo actualizados y mediciones adicionales.
 */
@Database(
    entities = [VehicleConfigEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(VehicleTypeConverter::class)
abstract class VehicleDatabase : RoomDatabase() {
    /**
     * Proporciona acceso a operaciones de base de datos de configuración de vehículo.
     *
     * @return DAO para gestionar configuración de vehículo
     */
    abstract fun vehicleDao(): VehicleDao

    companion object {
        /**
         * Migración de la versión 1 a la versión 2 de la base de datos.
         *
         * Añade la columna distanceBetweenFrontWheels para soporte de autocaravanas
         * y actualiza el valor del enum MOTORHOME a AUTOCARAVANA.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new column
                database.execSQL("ALTER TABLE vehicle_config ADD COLUMN distanceBetweenFrontWheels REAL")

                // Update enum MOTORHOME to AUTOCARAVANA if it exists
                database.execSQL("UPDATE vehicle_config SET type = 'AUTOCARAVANA' WHERE type = 'MOTORHOME'")
            }
        }
    }
}
