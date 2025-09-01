package com.example.campergas.domain.model

/**
 * Idiomas soportados por la aplicación
 */
enum class Language(val code: String, val displayName: String) {
    SPANISH("es", "Español"),
    ENGLISH("en", "English"),
    CATALAN("ca", "Català"),
    SYSTEM("system", "Sistema") // Seguir configuración del sistema
}