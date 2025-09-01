package com.example.campergas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {

    @Test
    fun `Language enum has correct values`() {
        // Assert all languages exist with correct codes and display names
        assertEquals("es", Language.SPANISH.code)
        assertEquals("Español", Language.SPANISH.displayName)

        assertEquals("en", Language.ENGLISH.code)
        assertEquals("English", Language.ENGLISH.displayName)

        assertEquals("ca", Language.CATALAN.code)
        assertEquals("Català", Language.CATALAN.displayName)

        assertEquals("system", Language.SYSTEM.code)
        assertEquals("Sistema", Language.SYSTEM.displayName)
    }

    @Test
    fun `Language enum has all expected values`() {
        val expectedLanguages = setOf(
            Language.SPANISH,
            Language.ENGLISH,
            Language.CATALAN,
            Language.SYSTEM
        )
        
        val actualLanguages = Language.entries.toSet()
        assertEquals(expectedLanguages, actualLanguages)
    }

    @Test
    fun `Language enum entries count is correct`() {
        assertEquals(4, Language.entries.size)
    }
}