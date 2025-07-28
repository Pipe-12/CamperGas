package com.example.campergas.domain.model

import org.junit.Test
import org.junit.Assert.*
import java.util.Date

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
    fun `getFormattedDate returns correct format`() {
        // Arrange
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2023, 11, 25, 14, 30, 0) // December 25, 2023, 14:30:00
        val timestamp = calendar.timeInMillis

        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = timestamp,
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act
        val formattedDate = measurement.getFormattedDate()

        // Assert
        assertTrue(formattedDate.contains("25"))
        assertTrue(formattedDate.contains("12") || formattedDate.contains("Dec"))
        assertTrue(formattedDate.contains("2023"))
    }

    @Test
    fun `getFormattedTime returns correct format`() {
        // Arrange
        val calendar = java.util.Calendar.getInstance()
        calendar.set(2023, 11, 25, 14, 30, 0) // December 25, 2023, 14:30:00
        val timestamp = calendar.timeInMillis

        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = timestamp,
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act
        val formattedTime = measurement.getFormattedTime()

        // Assert
        assertTrue(formattedTime.contains("14:30") || formattedTime.contains("2:30"))
    }

    @Test
    fun `isLowFuel returns true when percentage below threshold`() {
        // Arrange
        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 1.0f,
            fuelPercentage = 15.0f, // Below 20% threshold
            totalWeight = 6.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act & Assert
        assertTrue(measurement.isLowFuel())
    }

    @Test
    fun `isLowFuel returns false when percentage above threshold`() {
        // Arrange
        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 3.0f,
            fuelPercentage = 30.0f, // Above 20% threshold
            totalWeight = 8.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act & Assert
        assertFalse(measurement.isLowFuel())
    }

    @Test
    fun `isLowFuel returns true when percentage exactly at threshold`() {
        // Arrange
        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 2.2f,
            fuelPercentage = 20.0f, // Exactly at threshold
            totalWeight = 7.2f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act & Assert
        assertTrue(measurement.isLowFuel())
    }

    @Test
    fun `isCriticalFuel returns true when percentage below critical threshold`() {
        // Arrange
        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 0.5f,
            fuelPercentage = 8.0f, // Below 10% critical threshold
            totalWeight = 5.5f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act & Assert
        assertTrue(measurement.isCriticalFuel())
    }

    @Test
    fun `isCriticalFuel returns false when percentage above critical threshold`() {
        // Arrange
        val measurement = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 1.5f,
            fuelPercentage = 15.0f, // Above 10% critical threshold
            totalWeight = 6.5f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act & Assert
        assertFalse(measurement.isCriticalFuel())
    }

    @Test
    fun `data class equality works correctly`() {
        // Arrange
        val timestamp = System.currentTimeMillis()
        val measurement1 = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = timestamp,
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )
        val measurement2 = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = timestamp,
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )
        val measurement3 = FuelMeasurement(
            id = 2L, // Different ID
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = timestamp,
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Assert
        assertEquals(measurement1, measurement2)
        assertNotEquals(measurement1, measurement3)
        assertEquals(measurement1.hashCode(), measurement2.hashCode())
    }

    @Test
    fun `copy creates new instance with modified values`() {
        // Arrange
        val original = FuelMeasurement(
            id = 1L,
            cylinderId = 1L,
            cylinderName = "Test",
            timestamp = System.currentTimeMillis(),
            fuelKilograms = 5.0f,
            fuelPercentage = 45.0f,
            totalWeight = 10.0f,
            isCalibrated = true,
            isHistorical = false
        )

        // Act
        val copied = original.copy(fuelPercentage = 50.0f, isHistorical = true)

        // Assert
        assertEquals(original.id, copied.id)
        assertEquals(original.cylinderId, copied.cylinderId)
        assertEquals(original.fuelKilograms, copied.fuelKilograms, 0.01f)
        assertEquals(50.0f, copied.fuelPercentage, 0.01f) // Modified
        assertTrue(copied.isHistorical) // Modified
        assertEquals(original.isCalibrated, copied.isCalibrated) // Unchanged
    }
}
