package com.example.campergas.domain.model

import java.util.Locale

/**
 * Application language.
 *
 * The application supports multiple languages that affect all user interface
 * texts and system messages.
 *
 * Available languages:
 * - SPANISH: Spanish (Castilian)
 * - ENGLISH: English
 * - CATALAN: Catalan
 *
 * @author Felipe García Gómez
 */
enum class AppLanguage(val locale: Locale, val displayName: String) {
    /** Spanish language (Castilian) */
    SPANISH(Locale.forLanguageTag("es"), "Español"),

    /** English language */
    ENGLISH(Locale.forLanguageTag("en"), "English"),

    /** Catalan language */
    CATALAN(Locale.forLanguageTag("ca"), "Català");

    companion object {
        /**
         * Gets the language from a language code.
         *
         * @param languageCode Language code (e.g.: "es", "en", "ca")
         * @return Corresponding AppLanguage or SPANISH by default
         */
        fun fromLanguageCode(languageCode: String): AppLanguage {
            return entries.find { it.locale.language == languageCode } ?: SPANISH
        }
    }
}
