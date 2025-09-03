package com.example.campergas.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.example.campergas.domain.model.Language
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LocaleUtilsTest {

    private val mockContext = mockk<Context>()
    private val mockResources = mockk<Resources>()
    private val mockConfiguration = mockk<Configuration>()

    @Before
    fun setUp() {
        // Mock static Locale methods
        mockkStatic(Locale::class)
        
        // Setup mock context
        every { mockContext.resources } returns mockResources
        every { mockResources.configuration } returns mockConfiguration
        every { mockContext.createConfigurationContext(any()) } returns mockContext
    }

    @After
    fun tearDown() {
        unmockkStatic(Locale::class)
    }

    @Test
    fun `setLocale does not modify global default locale`() {
        // Arrange
        val testLanguage = Language.SPANISH
        
        // Act
        LocaleUtils.setLocale(mockContext, testLanguage)
        
        // Assert - Global default should not be changed (this is the fix for the infinite loop)
        verify(exactly = 0) { Locale.setDefault(any()) }
    }

    @Test
    fun `setLocale with SYSTEM language uses current default without modifying it`() {
        // Arrange
        val systemLocale = Locale("en", "US")
        every { Locale.getDefault() } returns systemLocale
        
        // Act
        LocaleUtils.setLocale(mockContext, Language.SYSTEM)
        
        // Assert - Should use the current system default but not set it globally
        verify { Locale.getDefault() }
        verify(exactly = 0) { Locale.setDefault(any()) }
    }

    @Test
    fun `getCurrentLanguageFromLocale returns correct language`() {
        // Test Spanish
        every { Locale.getDefault() } returns Locale("es")
        assertEquals(Language.SPANISH, LocaleUtils.getCurrentLanguageFromLocale())

        // Test English
        every { Locale.getDefault() } returns Locale("en")
        assertEquals(Language.ENGLISH, LocaleUtils.getCurrentLanguageFromLocale())

        // Test Catalan
        every { Locale.getDefault() } returns Locale("ca")
        assertEquals(Language.CATALAN, LocaleUtils.getCurrentLanguageFromLocale())

        // Test unknown language defaults to SYSTEM
        every { Locale.getDefault() } returns Locale("fr")
        assertEquals(Language.SYSTEM, LocaleUtils.getCurrentLanguageFromLocale())
    }
}