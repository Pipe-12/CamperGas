package com.example.campergas.domain.model

import java.util.Locale

/**
 * Idioma de la aplicación.
 *
 * La aplicación soporta múltiples idiomas que afectan a todos los textos
 * de la interfaz de usuario y mensajes del sistema.
 *
 * Idiomas disponibles:
 * - SPANISH: Español (castellano)
 * - ENGLISH: Inglés
 * - CATALAN: Catalán
 *
 * @author Felipe García Gómez
 */
enum class AppLanguage(val locale: Locale, val displayName: String) {
    /** Idioma español (castellano) */
    SPANISH(Locale.forLanguageTag("es"), "Español"),

    /** Idioma inglés */
    ENGLISH(Locale.forLanguageTag("en"), "English"),

    /** Idioma catalán */
    CATALAN(Locale.forLanguageTag("ca"), "Català");

    companion object {
        /**
         * Obtiene el idioma desde un código de idioma.
         *
         * @param languageCode Código del idioma (ej: "es", "en", "ca")
         * @return AppLanguage correspondiente o SPANISH por defecto
         */
        fun fromLanguageCode(languageCode: String): AppLanguage {
            return entries.find { it.locale.language == languageCode } ?: SPANISH
        }
    }
}
