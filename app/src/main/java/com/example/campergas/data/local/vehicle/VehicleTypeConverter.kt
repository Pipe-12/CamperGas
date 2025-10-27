package com.example.campergas.data.local.vehicle

import androidx.room.TypeConverter
import com.example.campergas.domain.model.VehicleType

/**
 * Conversor de tipos para base de datos Room para manejar el enum VehicleType.
 *
 * Convierte entre valores del enum VehicleType y representación String
 * para almacenamiento en base de datos.
 */
class VehicleTypeConverter {
    /**
     * Convierte enum VehicleType a String para almacenamiento en base de datos.
     *
     * @param vehicleType El valor del enum VehicleType
     * @return Representación String del tipo de vehículo
     */
    @TypeConverter
    fun fromVehicleType(vehicleType: VehicleType): String {
        return vehicleType.name
    }

    /**
     * Convierte String de base de datos a enum VehicleType.
     *
     * @param value Representación String del tipo de vehículo
     * @return Valor del enum VehicleType, por defecto CARAVAN si la conversión falla
     */
    @TypeConverter
    fun toVehicleType(value: String): VehicleType {
        return try {
            VehicleType.valueOf(value)
        } catch (_: Exception) {
            VehicleType.CARAVAN // Valor por defecto
        }
    }
}
