package com.example.campergas.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.campergas.domain.model.GasCylinder

@Database(
    entities = [
        GasCylinder::class,
        FuelMeasurementEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class CamperGasDatabase : RoomDatabase() {
    
    abstract fun gasCylinderDao(): GasCylinderDao
    abstract fun fuelMeasurementDao(): FuelMeasurementDao
    
    companion object {
        const val DATABASE_NAME = "campergas_database"
        
        // Migración de la versión 1 a la 2 (agregar tablas de bombonas y pesos)
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear tabla de bombonas
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS gas_cylinders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        tare REAL NOT NULL,
                        capacity REAL NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Crear tabla de mediciones de peso
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS weight_measurements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        value REAL NOT NULL,
                        timestamp INTEGER NOT NULL,
                        unit TEXT NOT NULL DEFAULT 'kg',
                        isCalibrated INTEGER NOT NULL DEFAULT 1,
                        cylinderId INTEGER,
                        isHistorical INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(cylinderId) REFERENCES gas_cylinders(id) ON DELETE SET NULL
                    )
                """.trimIndent())
                
                // Crear índices
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weight_measurements_cylinderId ON weight_measurements(cylinderId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weight_measurements_timestamp ON weight_measurements(timestamp)")
            }
        }
        
        // Migración de la versión 3 a la 4 (unificar tablas en fuel_measurements)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Crear nueva tabla unificada de mediciones de combustible
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS fuel_measurements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        cylinderId INTEGER NOT NULL,
                        cylinderName TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        fuelKilograms REAL NOT NULL,
                        fuelPercentage REAL NOT NULL,
                        totalWeight REAL NOT NULL,
                        isCalibrated INTEGER NOT NULL DEFAULT 1,
                        isHistorical INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(cylinderId) REFERENCES gas_cylinders(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Crear índices para la nueva tabla
                database.execSQL("CREATE INDEX IF NOT EXISTS index_fuel_measurements_cylinderId ON fuel_measurements(cylinderId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_fuel_measurements_timestamp ON fuel_measurements(timestamp)")
                
                // Migrar datos de consumption_table si existe
                database.execSQL("""
                    INSERT INTO fuel_measurements (cylinderId, cylinderName, timestamp, fuelKilograms, fuelPercentage, totalWeight, isCalibrated, isHistorical)
                    SELECT cylinderId, cylinderName, date, fuelKilograms, fuelPercentage, 
                           fuelKilograms + (SELECT tare FROM gas_cylinders WHERE id = cylinderId), 1, 0
                    FROM consumption_table 
                    WHERE EXISTS (SELECT 1 FROM sqlite_master WHERE type='table' AND name='consumption_table')
                """.trimIndent())
                
                // Eliminar tablas antiguas
                database.execSQL("DROP TABLE IF EXISTS consumption_table")
                database.execSQL("DROP TABLE IF EXISTS weight_measurements")
            }
        }
    }
}
