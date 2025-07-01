package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Weight
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetWeightUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): Flow<Weight?> {
        return bleRepository.weightData
    }
}
