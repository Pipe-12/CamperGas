package com.example.campergas.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Modelo de dominio y entidad de base de datos que representa un cilindro de gas.
 * 
 * Esta clase de datos representa un cilindro de gas GPL (Gas Licuado de Petróleo) utilizado
 * en caravanas y autocaravanas. Almacena tanto las características físicas del cilindro
 * (peso vacío, capacidad) como su estado en el sistema (activo/inactivo).
 * 
 * Funcionalidad clave:
 * - Solo un cilindro puede estar activo a la vez en el sistema
 * - El cilindro activo es el que se usa para las mediciones actuales de peso
 * - La tara (peso vacío) se utiliza para calcular el gas disponible restando del peso total
 * - La capacidad define el 100% de gas para calcular porcentajes
 * 
 * Casos de uso:
 * - Gestión de múltiples cilindros (butano, propano, diferentes tamaños)
 * - Cambio entre cilindros sin perder historial de mediciones
 * - Cálculo preciso de gas disponible mediante calibración con tara
 * 
 * Se persiste en la base de datos Room con el nombre de tabla "gas_cylinders".
 * 
 * @property id Identificador único autogenerado del cilindro en la base de datos
 * @property name Nombre descriptivo del cilindro (ej: "Butano 12.5kg", "Propano 11kg")
 * @property tare Peso del cilindro vacío en kilogramos (se resta del peso total para obtener el gas disponible)
 * @property capacity Capacidad máxima de gas del cilindro en kilogramos
 * @property isActive Indica si este cilindro es el que está actualmente en uso (solo uno puede estar activo)
 * @property createdAt Timestamp Unix (milisegundos) de cuándo se creó el registro del cilindro
 * @author Felipe García Gómez
 */
@Entity(tableName = "gas_cylinders")
data class GasCylinder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val tare: Float,
    val capacity: Float,
    val isActive: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {

}
