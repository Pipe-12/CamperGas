package com.example.campergas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.campergas.domain.model.GasCylinder

@Database(
    entities = [
        GasCylinder::class,
        FuelMeasurementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class CamperGasDatabase : RoomDatabase() {

    abstract fun gasCylinderDao(): GasCylinderDao
    abstract fun fuelMeasurementDao(): FuelMeasurementDao

    companion object {
        const val DATABASE_NAME = "campergas_database"
    }
}
