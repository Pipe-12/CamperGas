package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import javax.inject.Inject

class SetActiveCylinderUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    suspend operator fun invoke(cylinderId: Long): Result<Unit> {
        return try {
            val cylinder = repository.getCylinderById(cylinderId)
            if (cylinder != null) {
                repository.setActiveCylinder(cylinderId)
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Bombona no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
