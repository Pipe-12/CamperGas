package com.example.campergas.domain.model

import org.junit.Test
import org.junit.Assert.*

class FuelMeasurementTest {

    @Test
    fun `data class properties are set correctly`() {
        // Arrange
        val id = 123L
        val cylinderId = 1L
        val cylinderName = "Test Cylinder"
        val timestamp = System.currentTimeMillis()
        val fuelKilograms = 5.5f
        val fuelPercentage = 50.0f
        val totalWeight = 10.5f
        val isCalibrated = true
        val isHistorical = false

        // Act
        val measurement = FuelMeasurement(
            id = id,
            cylinderId = cylinderId,
            cylinderName = cylinderName,
            timestamp = timestamp,
            fuelKilograms = fuelKilograms,
            fuelPercentage = fuelPercentage,
            totalWeight = totalWeight,
            isCalibrated = isCalibrated,
            isHistorical = isHistorical
        )

        // Assert
        assertEquals(id, measurement.id)
        assertEquals(cylinderId, measurement.cylinderId)
        assertEquals(cylinderName, measurement.cylinderName)
        assertEquals(timestamp, measurement.timestamp)
        assertEquals(fuelKilograms, measurement.fuelKilograms, 0.01f)
        assertEquals(fuelPercentage, measurement.fuelPercentage, 0.01f)
        assertEquals(totalWeight, measurement.totalWeight, 0.01f)
        assertEquals(isCalibrated, measurement.isCalibrated)
        assertEquals(isHistorical, measurement.isHistorical)
    }

    @Test
    fun `getFormattedFuelKilograms returns correct format`() {
        val measurement = createTestMeasurement(fuelKilograms = 5.25f)
        assertEquals("5.25 kg", measurement.getFormattedFuelKilograms())
    }

    @Test
    fun `getFormattedPercentage returns correct format`() {
        val measurement = createTestMeasurement(fuelPercentage = 52.5f)
        assertEquals("52.5%", measurement.getFormattedPercentage())
    }

    @Test
    fun `getFormattedTimestamp returns time format`() {
        val measurement = createTestMeasurement(timestamp = 1640995200000L) // 1 Jan 2022 00:00:00
        val formatted = measurement.getFormattedTimestamp()
        // Should contain time with colons
        assertTrue("Formatted timestamp should contain time", formatted.contains(":"))
    }

    @Test
    fun `getFullFormattedTimestamp returns date and time format`() {
        val measurement = createTestMeasurement(timestamp = 1640995200000L)
        val formatted = measurement.getFullFormattedTimestamp()
        // Should contain date with slashes and time with colons
        assertTrue("Full formatted timestamp should contain date and time", 
            formatted.contains("/") && formatted.contains(":"))
    }

    @Test
    fun `isValid returns true for valid data`() {
        val measurement = createTestMeasurement(
            fuelKilograms = 5.0f,
            fuelPercentage = 50.0f
        )
        assertTrue(measurement.isValid())
    }

    @Test
    fun `isValid returns false for negative fuel`() {
        val measurement = createTestMeasurement(fuelKilograms = -1.0f)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for percentage over 100`() {
        val measurement = createTestMeasurement(fuelPercentage = 150.0f)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for negative percentage`() {
        val measurement = createTestMeasurement(fuelPercentage = -5.0f)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for NaN fuel kilograms`() {
        val measurement = createTestMeasurement(fuelKilograms = Float.NaN)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for infinite fuel kilograms`() {
        val measurement = createTestMeasurement(fuelKilograms = Float.POSITIVE_INFINITY)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for NaN fuel percentage`() {
        val measurement = createTestMeasurement(fuelPercentage = Float.NaN)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `isValid returns false for infinite fuel percentage`() {
        val measurement = createTestMeasurement(fuelPercentage = Float.POSITIVE_INFINITY)
        assertFalse(measurement.isValid())
    }

    @Test
    fun `default values are set correctly`() {
        val measurement = FuelMeasurement(
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = 123456789L,
            fuelKilograms = 5.0f,
            fuelPercentage = 50.0f,
            totalWeight = 15.0f
        )
        
        assertEquals(0L, measurement.id) // Default id
        assertTrue(measurement.isCalibrated) // Default isCalibrated
        assertFalse(measurement.isHistorical) // Default isHistorical
    }

    private fun createTestMeasurement(
        id: Long = 1L,
        cylinderId: Long = 1L,
        cylinderName: String = "Test Cylinder",
        timestamp: Long = System.currentTimeMillis(),
        fuelKilograms: Float = 5.0f,
        fuelPercentage: Float = 50.0f,
        totalWeight: Float = 15.0f,
        isCalibrated: Boolean = true,
        isHistorical: Boolean = false
    ) = FuelMeasurement(
        id = id,
        cylinderId = cylinderId,
        cylinderName = cylinderName,
        timestamp = timestamp,
        fuelKilograms = fuelKilograms,
        fuelPercentage = fuelPercentage,
        totalWeight = totalWeight,
        isCalibrated = isCalibrated,
        isHistorical = isHistorical
    )
}
