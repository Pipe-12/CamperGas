package com.example.campergas.domain.usecase

import com.example.campergas.data.repository.BleRepository
import com.example.campergas.domain.model.Inclination
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.math.abs

class GetInclinationUseCase @Inject constructor(
    private val bleRepository: BleRepository
) {
    operator fun invoke(): Flow<Inclination?> {
        return bleRepository.inclinationData
    }


}
