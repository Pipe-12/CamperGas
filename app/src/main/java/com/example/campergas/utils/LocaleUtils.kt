package com.example.campergas.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.example.campergas.domain.model.Language
import java.util.Locale

object LocaleUtils {

    private var lastAppliedLanguage: Language? = null

    fun setLocale(context: Context, language: Language): Context {
        // Always use Spanish locale
        val locale = Locale.forLanguageTag("es")

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
        // Prevent infinite loops by checking if we just applied this language
        if (lastAppliedLanguage == language) {
            return
        }

        // Always use Spanish locale
        val locale = Locale.forLanguageTag("es")

        // Set the default locale globally
        Locale.setDefault(locale)
        
        // Track the last applied language to prevent immediate re-application
        lastAppliedLanguage = language
        
        // For immediate locale changes, we need to recreate the activity
        // This is the modern recommended approach for runtime locale changes
        activity.recreate()
    }

    /**
     * Reset the last applied language tracking (for testing or when needed)
     */
    fun resetLastAppliedLanguage() {
        lastAppliedLanguage = null
    }

    fun getCurrentLanguageFromLocale(): Language {
        // Always return Spanish
        return Language.SPANISH
    }
}