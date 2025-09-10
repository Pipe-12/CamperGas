package com.example.campergas.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.example.campergas.domain.model.Language
import java.util.Locale

object LocaleUtils {

    fun setLocale(context: Context, language: Language): Context {
        val locale = when (language) {
            Language.SPANISH -> Locale.forLanguageTag("es")
            Language.ENGLISH -> Locale.forLanguageTag("en")
            Language.CATALAN -> Locale.forLanguageTag("ca")
            Language.SYSTEM -> Locale.getDefault()
        }

        // Set the default locale globally
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Apply locale configuration using modern Android approach
     * This replaces the deprecated updateConfiguration method
     */
    fun applyLocaleToActivity(activity: Activity, language: Language) {
        val locale = when (language) {
            Language.SPANISH -> Locale.forLanguageTag("es")
            Language.ENGLISH -> Locale.forLanguageTag("en")
            Language.CATALAN -> Locale.forLanguageTag("ca")
            Language.SYSTEM -> Locale.getDefault()
        }

        // Set the default locale globally
        Locale.setDefault(locale)
        
        // For immediate locale changes, we need to recreate the activity
        // This is the modern recommended approach for runtime locale changes
        activity.recreate()
    }

    fun getCurrentLanguageFromLocale(): Language {
        val currentLocale = Locale.getDefault()
        return when (currentLocale.language) {
            "es" -> Language.SPANISH
            "en" -> Language.ENGLISH
            "ca" -> Language.CATALAN
            else -> Language.SYSTEM
        }
    }
}