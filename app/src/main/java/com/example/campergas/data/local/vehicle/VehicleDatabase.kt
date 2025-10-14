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
        // Migration from version 1 to 2 (add field distanceBetweenFrontWheels and change MOTORHOME to AUTOCARAVANA)
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
