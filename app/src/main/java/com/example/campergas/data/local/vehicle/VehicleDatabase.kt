package com.example.campergas.data.local.vehicle

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [VehicleConfigEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(VehicleTypeConverter::class)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    
    companion object {
        // Migración de la versión 1 a la 2 (agregar campo distanceBetweenFrontWheels y cambiar MOTORHOME a AUTOCARAVANA)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Agregar nueva columna
                database.execSQL("ALTER TABLE vehicle_config ADD COLUMN distanceBetweenFrontWheels REAL")
                
                // Actualizar enum MOTORHOME a AUTOCARAVANA si existe
                database.execSQL("UPDATE vehicle_config SET type = 'AUTOCARAVANA' WHERE type = 'MOTORHOME'")
            }
        }
    }
}
