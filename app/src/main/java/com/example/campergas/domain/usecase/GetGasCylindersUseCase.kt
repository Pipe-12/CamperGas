package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.GasCylinderRepository
import com.example.campergas.domain.model.GasCylinder
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGasCylindersUseCase @Inject constructor(
    private val repository: GasCylinderRepository
) {
    operator fun invoke(): Flow<List<GasCylinder>> {
        return repository.getAllCylinders()
    }

    fun getActiveCylinder(): Flow<GasCylinder?> {
        return repository.getActiveCylinder()
    }

    suspend fun getActiveCylinderSync(): GasCylinder? {
        return repository.getActiveCylinderSync()
    }
}
