package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActiveCylinderUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    operator fun invoke(): Flow<GasCylinder?> {
        return repository.getActiveCylinder()
    }
    
    suspend fun getActiveCylinderSync(): GasCylinder? {
        return repository.getActiveCylinderSync()
    }
}
