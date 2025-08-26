package com.example.campergas.service

import android.util.Log
import com.example.campergas.data.local.preferences.PreferencesDataStore
import com.example.campergas.data.repository.BleRepository
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class BleForegroundServiceLogicTest {

    private val mockBleRepository: BleRepository = mockk()
    private val mockPreferencesDataStore: PreferencesDataStore = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    // Simple test class to test the notification logic without Android dependencies
    private class NotificationLogicTester {
        private var lastAlertThreshold: Float? = null
        private var hasAlertBeenSent = false

        fun checkGasLevelThreshold(
            currentPercentage: Float,
            threshold: Float,
            notificationsEnabled: Boolean
        ): Boolean {
            if (!notificationsEnabled) return false

            // Solo enviar alerta si el gas está por debajo del umbral
            if (currentPercentage <= threshold) {
                // Evitar spam: solo enviar si no se ha enviado para este umbral o si el umbral cambió
                if (!hasAlertBeenSent || lastAlertThreshold != threshold) {
                    hasAlertBeenSent = true
                    lastAlertThreshold = threshold
                    return true // Would send notification
                }
            } else {
                // Reset del estado de alerta cuando el gas está por encima del umbral
                if (hasAlertBeenSent) {
                    hasAlertBeenSent = false
                    lastAlertThreshold = null // Reset the threshold to allow notifications for same threshold again
                }
            }
            return false // Would not send notification
        }
    }

    @Before
    fun setUp() {
        // Mock Android Log
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0

        // Default mock responses
        every { mockPreferencesDataStore.areNotificationsEnabled } returns flowOf(true)
        every { mockPreferencesDataStore.gasLevelThreshold } returns flowOf(15.0f)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
        clearAllMocks()
    }

    @Test
    fun `notification logic allows notification after gas level recovers`() = runTest {
        val tester = NotificationLogicTester()
        val threshold = 15.0f

        // Simulate gas going below threshold (should send notification)
        val firstAlert = tester.checkGasLevelThreshold(10.0f, threshold, true)
        assertTrue("First alert should be sent", firstAlert)

        // Simulate gas staying below threshold (should not send notification again)
        val secondAlert = tester.checkGasLevelThreshold(8.0f, threshold, true)
        assertFalse("Second alert should not be sent for same threshold breach", secondAlert)

        // Simulate gas going above threshold (should reset alert state)
        tester.checkGasLevelThreshold(20.0f, threshold, true)

        // Simulate gas going below threshold again (should send notification again)
        val thirdAlert = tester.checkGasLevelThreshold(12.0f, threshold, true)
        assertTrue("Third alert should be sent after recovery", thirdAlert)
    }

    @Test
    fun `notification logic does not send duplicate notifications for same threshold breach`() = runTest {
        val tester = NotificationLogicTester()
        val threshold = 15.0f

        // Simulate gas going below threshold twice without recovery
        val firstAlert = tester.checkGasLevelThreshold(10.0f, threshold, true)
        val secondAlert = tester.checkGasLevelThreshold(8.0f, threshold, true)

        assertTrue("First alert should be sent", firstAlert)
        assertFalse("Second alert should not be sent for same threshold", secondAlert)
    }

    @Test
    fun `notification logic respects notifications disabled setting`() = runTest {
        val tester = NotificationLogicTester()
        val threshold = 15.0f

        // Simulate gas going below threshold with notifications disabled
        val alert = tester.checkGasLevelThreshold(10.0f, threshold, false)

        assertFalse("No alert should be sent when notifications are disabled", alert)
    }

    @Test
    fun `notification logic handles threshold changes correctly`() = runTest {
        val tester = NotificationLogicTester()

        // Send notification for 15% threshold
        val firstAlert = tester.checkGasLevelThreshold(10.0f, 15.0f, true)
        assertTrue("First alert should be sent", firstAlert)

        // Change threshold to 20% while still below both thresholds
        val secondAlert = tester.checkGasLevelThreshold(10.0f, 20.0f, true)
        assertTrue("Second alert should be sent for new threshold", secondAlert)

        // Same threshold again should not send notification
        val thirdAlert = tester.checkGasLevelThreshold(12.0f, 20.0f, true)
        assertFalse("Third alert should not be sent for same threshold", thirdAlert)
    }
}