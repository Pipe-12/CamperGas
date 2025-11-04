package com.example.campergas.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.DisplayMetrics
import com.example.campergas.domain.model.Language
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertEquals
import java.util.Locale

class LocaleUtilsTest {
    
    private lateinit var context: Context
    private lateinit var activity: Activity
    private lateinit var resources: Resources
    private lateinit var configuration: Configuration
    private lateinit var displayMetrics: DisplayMetrics
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        resources = mockk(relaxed = true)
        configuration = Configuration()
        displayMetrics = DisplayMetrics()
        
        every { context.resources } returns resources
        every { resources.configuration } returns configuration
        
        every { activity.resources } returns resources
        every { resources.displayMetrics } returns displayMetrics
        
        // Reset LocaleUtils state before each test
        LocaleUtils.resetLastAppliedLanguage()
    }
    
    private fun getLocaleFromConfig(config: Configuration): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.locales.get(0)
        } else {
            @Suppress("DEPRECATION")
            config.locale ?: Locale.getDefault()
        }
    }
    
    @Test
    fun `setLocale always creates context with Spanish locale`() {
        // Given
        val language = Language.SPANISH
        every { context.createConfigurationContext(any()) } returns context
        
        // When
        val result = LocaleUtils.setLocale(context, language)
        
        // Then
        val configSlot = slot<Configuration>()
        verify { context.createConfigurationContext(capture(configSlot)) }
        val capturedLocale = getLocaleFromConfig(configSlot.captured)
        assertEquals("es", capturedLocale.language)
        assertEquals(context, result)
    }
    
    @Test
    fun `applyLocaleToActivity recreates activity and sets Spanish locale`() {
        // Given
        val language = Language.SPANISH
        
        // When
        LocaleUtils.applyLocaleToActivity(activity, language)
        
        // Then
        verify { activity.recreate() }
        // Verify that the default locale was set to Spanish
        assertEquals("es", Locale.getDefault().language)
    }
    
    @Test
    fun `applyLocaleToActivity prevents infinite loop by tracking last applied language`() {
        // Given
        val language = Language.SPANISH
        LocaleUtils.resetLastAppliedLanguage() // Reset state for test
        
        // When - Apply the same language twice
        LocaleUtils.applyLocaleToActivity(activity, language)
        LocaleUtils.applyLocaleToActivity(activity, language) // Second call should be ignored
        
        // Then - Activity should only be recreated once
        verify(exactly = 1) { activity.recreate() }
    }
    
    @Test
    fun `getCurrentLanguageFromLocale always returns Spanish`() {
        // When
        val result = LocaleUtils.getCurrentLanguageFromLocale()
        
        // Then
        assertEquals(Language.SPANISH, result)
    }
    
    @Test
    fun `getCurrentLanguageFromLocale returns Spanish regardless of system locale`() {
        // Given - Set default locale to something else
        Locale.setDefault(Locale.forLanguageTag("en"))
        
        // When
        val result = LocaleUtils.getCurrentLanguageFromLocale()
        
        // Then - Still returns Spanish as it's the only supported language
        assertEquals(Language.SPANISH, result)
    }
}