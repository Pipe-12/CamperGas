package com.example.campergas.utils

import android.content.Context
import android.content.res.Configuration
import com.example.campergas.domain.model.Language
import java.util.Locale

object LocaleUtils {
    
    fun setLocale(context: Context, language: Language): Context {
        val locale = when (language) {
            Language.SPANISH -> Locale("es")
            Language.ENGLISH -> Locale("en")
            Language.CATALAN -> Locale("ca")
            Language.SYSTEM -> Locale.getDefault()
        }
        
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        return context.createConfigurationContext(config)
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