package com.example.campergas.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class LanguageTest {

    @Test
    fun `Language enum has correct values`() {
        // Assert Spanish language exists with correct code and display name
        assertEquals("es", Language.SPANISH.code)
        assertEquals("Español", Language.SPANISH.displayName)
    }

    @Test
    fun `Language enum has only Spanish language`() {
        val expectedLanguages = setOf(
            Language.SPANISH
        )
        
        val actualLanguages = Language.entries.toSet()
        assertEquals(expectedLanguages, actualLanguages)
    }

    @Test
    fun `Language enum entries count is correct`() {
        assertEquals(1, Language.entries.size)
    }
}