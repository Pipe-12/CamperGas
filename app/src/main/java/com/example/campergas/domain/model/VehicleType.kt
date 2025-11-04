package com.example.campergas.domain.model

/**
 * Tipos de vehículos recreativos soportados por la aplicación.
 *
 * Esta enumeración define los tipos de vehículos que pueden ser configurados
 * en CamperGas para el monitoreo de cilindros de gas y estabilidad.
 *
 * Tipos disponibles:
 * - CARAVAN: Caravana remolcada (trailer) sin motor propio
 * - AUTOCARAVANA: Vehículo motorizado con habitáculo integrado
 *
 * El tipo de vehículo puede afectar a:
 * - Cálculos de consumo de gas
 * - Configuración de estabilidad y nivelación
 * - Ubicación recomendada de sensores
 * - Cálculos de elevación de ruedas
 *
 * @author Felipe García Gómez
 */
enum class VehicleType {
    /** Caravana remolcada sin motor propio */
    CARAVAN,

    /** Autocaravana motorizada con habitáculo integrado */
    AUTOCARAVANA
}
