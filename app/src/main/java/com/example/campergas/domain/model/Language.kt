package com.example.campergas.domain.model

/**
 * Languages supported by the application
 */
enum class Language(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English"),
    CATALAN("ca", "Català"),
    SYSTEM("system", "Sistema") // Follow system configuration
}