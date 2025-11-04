package com.example.campergas.domain.model

/**
 * Idioma de la aplicación CamperGas.
 * 
 * La aplicación está configurada exclusivamente en Español (Castellano).
 * Esta enumeración se mantiene para compatibilidad con el código existente,
 * pero solo contiene la opción de español.
 * 
 * @property code Código ISO 639-1 del idioma ("es")
 * @property displayName Nombre del idioma para mostrar al usuario
 * 
 * @author Felipe García Gómez
 */
enum class Language(val code: String, val displayName: String) {
    /** Idioma español - único idioma soportado */
    SPANISH("es", "Español")
}