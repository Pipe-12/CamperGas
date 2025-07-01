package com.example.campergas.data.local.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.campergas.domain.model.GasCylinder
import com.example.campergas.domain.model.Weight

@Database(
    entities = [
        GasCylinder::class,
        Weight::class,
        ConsumptionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CamperGasDatabase : RoomDatabase() {
    
    abstract fun gasCylinderDao(): GasCylinderDao
    abstract fun weightDao(): WeightDao
    abstract fun consumptionDao(): ConsumptionDao
    
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
    }
}
