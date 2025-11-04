package com.example.campergas.domain.model

/**
 * Modo de tema visual de la aplicación.
 *
 * La aplicación soporta temas claro, oscuro y del sistema. El tema afecta a colores
 * de fondo, texto, componentes UI y barras del sistema.
 *
 * Temas disponibles:
 * - LIGHT: Tema claro con fondos claros y texto oscuro
 * - DARK: Tema oscuro con fondos negros/oscuros y texto claro
 * - SYSTEM: Usa el tema del sistema (Android 10+)
 *
 * @author Felipe García Gómez
 */
enum class ThemeMode {
    /** Modo de tema claro con fondos claros y texto oscuro */
    LIGHT,

    /** Modo de tema oscuro con fondos oscuros y texto claro */
    DARK,

    /** Usa el tema del sistema operativo */
    SYSTEM
}
