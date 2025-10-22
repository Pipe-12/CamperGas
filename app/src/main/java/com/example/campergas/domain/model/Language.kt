package com.example.campergas.domain.model

/**
 * Idiomas soportados por la aplicación CamperGas.
 * 
 * Esta enumeración define los idiomas disponibles para la interfaz de usuario,
 * incluyendo su código ISO 639-1 y nombre de visualización en el idioma nativo.
 * 
 * Idiomas disponibles:
 * - Español: Idioma principal de la aplicación
 * - Inglés: Idioma internacional
 * - Catalán: Idioma regional
 * - Sistema: Sigue la configuración del dispositivo
 * 
 * Cada idioma incluye:
 * @property code Código ISO 639-1 del idioma (ej: "es", "en", "ca") o "system" para seguir la configuración del sistema
 * @property displayName Nombre del idioma en su forma nativa para mostrarlo al usuario
 * 
 * @author Felipe García Gómez
 */
enum class Language(val code: String, val displayName: String) {
    /** Idioma español */
    SPANISH("es", "Español"),
    
    /** Idioma inglés */
    ENGLISH("en", "English"),
    
    /** Idioma catalán */
    CATALAN("ca", "Català"),
    
    /** Seguir configuración del sistema operativo */
    SYSTEM("system", "Sistema")
}