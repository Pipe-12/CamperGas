package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.ConsumptionRepository
import com.example.campergas.domain.model.Consumption
import javax.inject.Inject

class SaveConsumptionUseCase @Inject constructor(
    private val consumptionRepository: ConsumptionRepository
) {
    suspend operator fun invoke(
        initialWeight: Float,
        finalWeight: Float,
        durationMinutes: Long
    ) {
        val consumption = Consumption(
            date = System.currentTimeMillis(),
            initialWeight = initialWeight,
            finalWeight = finalWeight,
            consumptionValue = initialWeight - finalWeight,
            duration = durationMinutes
        )
        
        consumptionRepository.saveConsumption(consumption)
    }
    
    suspend fun saveConsumption(consumption: Consumption) {
        consumptionRepository.saveConsumption(consumption)
    }
}
