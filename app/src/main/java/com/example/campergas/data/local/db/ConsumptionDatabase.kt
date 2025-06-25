package com.example.campergas.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [ConsumptionEntity::class], version = 1, exportSchema = false)
abstract class ConsumptionDatabase : RoomDatabase() {
    abstract fun consumptionDao(): ConsumptionDao
}
