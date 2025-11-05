package com.example.campergas.domain.model

/**
 * Application language options supported by CamperGas.
 *
 * The SYSTEM option follows the device language. Others use BCP-47 tags.
 */
enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ES("es"),
    EN("en"),
    CA("ca");

    companion object {
        /**
         * Map a persisted language tag to an AppLanguage value.
         * Accepts "system" (legacy) or empty for SYSTEM, otherwise tries by tag.
         */
        fun fromStored(value: String?): AppLanguage {
            if (value.isNullOrBlank() || value.equals("system", ignoreCase = true)) return SYSTEM
            return entries.firstOrNull { it.tag.equals(value, ignoreCase = true) } ?: SYSTEM
        }
    }
}
