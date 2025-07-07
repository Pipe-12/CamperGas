package com.example.campergas.di

import android.content.Context
import androidx.room.Room
import com.example.campergas.data.local.db.CamperGasDatabase
import com.example.campergas.data.local.db.FuelMeasurementDao
import com.example.campergas.data.local.db.GasCylinderDao
import com.example.campergas.data.local.vehicle.VehicleDao
import com.example.campergas.data.local.vehicle.VehicleDatabase
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
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideGasCylinderDao(database: CamperGasDatabase): GasCylinderDao {
        return database.gasCylinderDao()
    }

    @Provides
    fun provideFuelMeasurementDao(database: CamperGasDatabase): FuelMeasurementDao {
        return database.fuelMeasurementDao()
    }

    @Provides
    @Singleton
    fun provideVehicleDatabase(@ApplicationContext context: Context): VehicleDatabase {
        return Room.databaseBuilder(
            context,
            VehicleDatabase::class.java,
            "vehicle_database"
        )
            .addMigrations(VehicleDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideVehicleDao(database: VehicleDatabase): VehicleDao {
        return database.vehicleDao()
    }

}
