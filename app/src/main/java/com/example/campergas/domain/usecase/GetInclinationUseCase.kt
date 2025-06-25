package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Inclination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.math.absoluteValue

class GetInclinationUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): Flow<Inclination?> {
        return bleRepository.inclinationData.map { pairData ->
            pairData?.let { (xAxis, yAxis) ->
                val timestamp = System.currentTimeMillis()
                val isLevelX = isAxisLevel(xAxis)
                val isLevelY = isAxisLevel(yAxis)
                
                Inclination(
                    xAxis = xAxis,
                    yAxis = yAxis,
                    timestamp = timestamp,
                    isLevelX = isLevelX,
                    isLevelY = isLevelY
                )
            }
        }
    }
    
    // Considera que la inclinación está nivelada si está dentro de un umbral
    private fun isAxisLevel(axisValue: Float): Boolean {
        val levelThreshold = 0.5f // Ejemplo: 0.5 grados de tolerancia
        return axisValue.absoluteValue <= levelThreshold
    }
}
