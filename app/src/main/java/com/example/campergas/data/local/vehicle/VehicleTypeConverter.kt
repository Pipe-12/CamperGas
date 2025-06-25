package com.example.campergas.data.local.vehicle

import androidx.room.TypeConverter
import com.example.campergas.domain.model.VehicleType

class VehicleTypeConverter {
    @TypeConverter
    fun fromVehicleType(vehicleType: VehicleType): String {
        return vehicleType.name
    }
    
    @TypeConverter
    fun toVehicleType(value: String): VehicleType {
        return try {
            VehicleType.valueOf(value)
        } catch (e: Exception) {
            VehicleType.CARAVAN // Valor por defecto
        }
    }
}
