package com.example.campergas

import com.example.campergas.domain.model.FuelMeasurementTest
import com.example.campergas.domain.usecase.AddGasCylinderUseCaseTest
import com.example.campergas.domain.usecase.GetActiveCylinderUseCaseTest
import com.example.campergas.domain.usecase.SaveFuelMeasurementUseCaseTest
import com.example.campergas.ui.screens.bleconnect.BleConnectViewModelTest
import com.example.campergas.ui.screens.consumption.ConsumptionViewModelTest
import com.example.campergas.ui.screens.inclination.InclinationViewModelTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Test Suite para toda la aplicación CamperGas
 *
 * Esta suite ejecuta todos los tests unitarios importantes de la aplicación:
 * - Tests de casos de uso (Use Cases)
 * - Tests de modelos de dominio
 * - Tests de ViewModels
 * - Tests de lógica de negocio
 */
@ExperimentalCoroutinesApi
@RunWith(Suite::class)
@Suite.SuiteClasses(
    // Use Cases Tests
    SaveFuelMeasurementUseCaseTest::class,
    GetActiveCylinderUseCaseTest::class,
    AddGasCylinderUseCaseTest::class,

    // Domain Models Tests
    FuelMeasurementTest::class,

    // ViewModels Tests
    BleConnectViewModelTest::class,
    ConsumptionViewModelTest::class,
    InclinationViewModelTest::class
)
class CamperGasTestSuite {
    /**
     * Esta clase actúa como un punto de entrada para ejecutar
     * todos los tests de la aplicación CamperGas de una vez.
     *
     * Para ejecutar todos los tests:
     * ./gradlew test
     *
     * Para ejecutar solo esta suite:
     * ./gradlew test --tests "com.example.campergas.CamperGasTestSuite"
     */
}
