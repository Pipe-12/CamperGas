package com.example.campergas.ui.screens.cylinders

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCylinderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, tare: Float, capacity: Float, setAsActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tare by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var setAsActive by remember { mutableStateOf(true) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var tareError by remember { mutableStateOf<String?>(null) }
    var capacityError by remember { mutableStateOf<String?>(null) }
    
    fun validateInputs(): Boolean {
        nameError = null
        tareError = null
        capacityError = null
        
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "El nombre es obligatorio"
            isValid = false
        }
        
        val tareValue = tare.toFloatOrNull()
        if (tareValue == null || tareValue < 0) {
            tareError = "Ingrese un peso válido (≥ 0)"
            isValid = false
        }
        
        val capacityValue = capacity.toFloatOrNull()
        if (capacityValue == null || capacityValue <= 0) {
            capacityError = "Ingrese una capacidad válida (> 0)"
            isValid = false
        }
        
        return isValid
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Agregar Nueva Bombona",
                    style = MaterialTheme.typography.titleLarge
                )
                
                // Campo nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre de la bombona") },
                    placeholder = { Text("Ej: Bombona Principal") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } }
                )
                
                // Campo tara
                OutlinedTextField(
                    value = tare,
                    onValueChange = { 
                        tare = it
                        tareError = null
                    },
                    label = { Text("Tara (kg)") },
                    placeholder = { Text("Peso de la bombona vacía") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = tareError != null,
                    supportingText = tareError?.let { { Text(it) } }
                )
                
                // Campo capacidad
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { 
                        capacity = it
                        capacityError = null
                    },
                    label = { Text("Capacidad de gas (kg)") },
                    placeholder = { Text("Capacidad máxima de gas") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = capacityError != null,
                    supportingText = capacityError?.let { { Text(it) } }
                )
                
                // Checkbox para activar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = setAsActive,
                        onCheckedChange = { setAsActive = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Establecer como bombona activa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Botones
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (validateInputs()) {
                                onConfirm(
                                    name.trim(),
                                    tare.toFloat(),
                                    capacity.toFloat(),
                                    setAsActive
                                )
                            }
                        }
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}
