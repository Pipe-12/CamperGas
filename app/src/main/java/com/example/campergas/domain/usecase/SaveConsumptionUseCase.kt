package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.Consumption
import javax.inject.Inject

class SaveConsumptionUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository,
    private val gasCylinderRepository: GasCylinderRepository
) {
    suspend operator fun invoke(
        cylinderId: Long,
        totalWeight: Float,
        durationMinutes: Long = 0,
        customTimestamp: Long? = null
    ) {
        // Obtener la bombona para calcular el combustible
        val cylinder = gasCylinderRepository.getGasCylinderById(cylinderId)
        
        if (cylinder != null) {
            val fuelKilograms = cylinder.calculateGasContent(totalWeight)
            val fuelPercentage = cylinder.calculateGasPercentage(totalWeight)
            
            val consumption = Consumption(
                cylinderId = cylinderId,
                cylinderName = cylinder.name,
                date = customTimestamp ?: System.currentTimeMillis(),
                fuelPercentage = fuelPercentage,
                fuelKilograms = fuelKilograms,
                duration = durationMinutes
            )
            
            consumptionRepository.saveConsumption(consumption)
        }
    }
    
    suspend fun saveConsumption(consumption: Consumption) {
        consumptionRepository.saveConsumption(consumption)
    }
}
