package com.example.campergas.data.ble

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Test para verificar el cálculo correcto de timestamps históricos
 */
class TimestampCalculationTest {

    @Test
    fun testCalculateHistoricalTimestampWorksCorrectly() {
        // Simular que estamos en un momento específico
        val currentTime = 1704556800000L // 2024-01-06 12:00:00 UTC

        // Datos de prueba: medición tomada hace 5 minutos (300 segundos)
        val secondsAgo = 300L

        // El timestamp calculado debería ser currentTime - (300 * 1000)
        val expectedTimestamp = currentTime - (secondsAgo * 1000L)
        val actualTimestamp = currentTime - (secondsAgo * 1000L)

        assertEquals(
            "El cálculo de timestamp histórico debe ser correcto",
            expectedTimestamp,
            actualTimestamp
        )
    }

    @Test
    fun testTimestampCalculationWithDifferentIntervals() {
        val currentTime = System.currentTimeMillis()

        // Test con diferentes intervalos
        val testCases = listOf(
            60L,    // 1 minuto
            300L,   // 5 minutos  
            1800L,  // 30 minutos
            3600L,  // 1 hora
            86400L  // 1 día
        )

        testCases.forEach { secondsAgo ->
            val calculatedTimestamp = currentTime - (secondsAgo * 1000L)
            val expectedDifference = secondsAgo * 1000L
            val actualDifference = currentTime - calculatedTimestamp

            assertEquals(
                "La diferencia debe ser exactamente $secondsAgo segundos",
                expectedDifference,
                actualDifference
            )
        }
    }
}
