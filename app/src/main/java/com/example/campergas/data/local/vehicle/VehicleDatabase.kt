package com.example.campergas.data.local.vehicle

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [VehicleConfigEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(VehicleTypeConverter::class)
abstract class VehicleDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
}
