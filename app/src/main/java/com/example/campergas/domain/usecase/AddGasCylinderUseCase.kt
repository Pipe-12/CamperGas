package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
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
                return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
            }
            
            if (tare < 0) {
                return Result.failure(IllegalArgumentException("La tara no puede ser negativa"))
            }
            
            if (capacity <= 0) {
                return Result.failure(IllegalArgumentException("La capacidad debe ser mayor que cero"))
            }
            
            val cylinder = GasCylinder(
                name = name.trim(),
                tare = tare,
                capacity = capacity,
                isActive = setAsActive
            )
            
            val id = repository.insertCylinder(cylinder)
            
            // Si se debe establecer como activa, desactivar las demás
            if (setAsActive) {
                repository.setActiveCylinder(id)
            }
            
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
