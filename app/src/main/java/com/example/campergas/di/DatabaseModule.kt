package com.example.campergas.di

import android.content.Context
import androidx.room.Room
import com.example.campergas.data.local.db.ConsumptionDatabase
import com.example.campergas.data.local.db.ConsumptionDao
import com.example.campergas.data.local.vehicle.VehicleDatabase
import com.example.campergas.data.local.vehicle.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVehicleDatabase(@ApplicationContext context: Context): VehicleDatabase {
        return Room.databaseBuilder(
            context,
            VehicleDatabase::class.java,
            "vehicle_database"
        ).build()
    }

    @Provides
    fun provideVehicleDao(database: VehicleDatabase): VehicleDao {
        return database.vehicleDao()
    }

    @Provides
    @Singleton
    fun provideConsumptionDatabase(@ApplicationContext context: Context): ConsumptionDatabase {
        return Room.databaseBuilder(
            context,
            ConsumptionDatabase::class.java,
            "consumption_database"
        ).build()
    }

    @Provides
    fun provideConsumptionDao(database: ConsumptionDatabase): ConsumptionDao {
        return database.consumptionDao()
    }
}
