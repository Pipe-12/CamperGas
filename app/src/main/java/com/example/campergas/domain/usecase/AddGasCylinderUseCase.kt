package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import javax.inject.Inject

class AddGasCylinderUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    suspend operator fun invoke(
        name: String,
        tare: Float,
        capacity: Float,
        setAsActive: Boolean = false
    ): Result<Long> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Name cannot be empty"))
            }

            if (tare < 0) {
                return Result.failure(IllegalArgumentException("Tare cannot be negative"))
            }

            if (capacity <= 0) {
                return Result.failure(IllegalArgumentException("Capacity must be greater than zero"))
            }

            val cylinder = GasCylinder(
                name = name.trim(),
                tare = tare,
                capacity = capacity,
                isActive = setAsActive
            )

            val id = repository.insertCylinder(cylinder)

            // If should be set as active, deactivate the others
            if (setAsActive) {
                repository.setActiveCylinder(id)
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
