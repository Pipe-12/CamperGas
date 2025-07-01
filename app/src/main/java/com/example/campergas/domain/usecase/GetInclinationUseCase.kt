package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Inclination
import kotlinx.coroutines.flow.Flow
import kotlin.math.abs
import javax.inject.Inject

class GetInclinationUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): Flow<Inclination?> {
        return bleRepository.inclinationData
    }
    
    // Considera que la inclinación está nivelada si está dentro de un umbral
    private fun isAxisLevel(axisValue: Float): Boolean {
        val levelThreshold = 0.5f // Ejemplo: 0.5 grados de tolerancia
        return abs(axisValue) <= levelThreshold
    }
}
