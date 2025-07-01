package com.example.campergas.di

import android.content.Context
import androidx.room.Room
import com.example.campergas.data.local.db.*
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
    fun provideCamperGasDatabase(@ApplicationContext context: Context): CamperGasDatabase {
        return Room.databaseBuilder(
            context,
            CamperGasDatabase::class.java,
            CamperGasDatabase.DATABASE_NAME
        )
        .addMigrations(CamperGasDatabase.MIGRATION_1_2)
        .build()
    }

    @Provides
    fun provideGasCylinderDao(database: CamperGasDatabase): GasCylinderDao {
        return database.gasCylinderDao()
    }

    @Provides
    fun provideWeightDao(database: CamperGasDatabase): WeightDao {
        return database.weightDao()
    }

    @Provides
    fun provideConsumptionDao(database: CamperGasDatabase): ConsumptionDao {
        return database.consumptionDao()
    }

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

}
