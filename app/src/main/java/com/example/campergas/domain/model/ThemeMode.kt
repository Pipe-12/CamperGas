package com.example.campergas.domain.model

/**
 * Modos de tema visual disponibles en la aplicación.
 * 
 * Esta enumeración define los esquemas de color que el usuario puede seleccionar
 * para la interfaz de la aplicación. El tema afecta a colores de fondo, texto,
 * componentes UI y barras del sistema.
 * 
 * Temas disponibles:
 * - LIGHT: Tema claro con fondos blancos/claros y texto oscuro
 * - DARK: Tema oscuro con fondos negros/oscuros y texto claro
 * 
 * El tema seleccionado se persiste en las preferencias del usuario y se aplica
 * inmediatamente en toda la aplicación, incluyendo las barras de estado y navegación.
 * 
 * @author Felipe García Gómez
 */
enum class ThemeMode {
    /** Modo de tema claro con fondos blancos y texto oscuro */
    LIGHT,
    
    /** Modo de tema oscuro con fondos oscuros y texto claro */
    DARK
}
