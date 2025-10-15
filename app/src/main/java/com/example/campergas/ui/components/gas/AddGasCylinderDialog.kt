package com.example.campergas.ui.components.gas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.campergas.R

@Composable
fun AddGasCylinderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Float, Float, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var tare by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var setAsActive by remember { mutableStateOf(true) }
    var nameError by remember { mutableStateOf(false) }
    var tareError by remember { mutableStateOf(false) }
    var capacityError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.add_cylinder_title))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nombre of the cylinder") },
                    placeholder = { Text("Ej: Bombona Principal") },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("El nombre es requerido") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tare,
                    onValueChange = {
                        tare = it
                        tareError = it.toFloatOrNull() == null || it.toFloatOrNull()!! < 0
                    },
                    label = { Text("Empty cylinder weight (kg)") },
                    placeholder = { Text("Ej: 15.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = tareError,
                    supportingText = if (tareError) {
                        { Text("Enter a valid weight") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = capacity,
                    onValueChange = {
                        capacity = it
                        capacityError = it.toFloatOrNull() == null || it.toFloatOrNull()!! <= 0
                    },
                    label = { Text("Capacidad de gas (kg)") },
                    placeholder = { Text("Ej: 13.0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = capacityError,
                    supportingText = if (capacityError) {
                        { Text("Enter a valid capacity") }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = setAsActive,
                        onCheckedChange = { setAsActive = it }
                    )
                    Text(
                        text = "Establecer como cylinder activa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tareValue = tare.toFloatOrNull()
                    val capacityValue = capacity.toFloatOrNull()

                    if (name.isNotBlank() && tareValue != null && tareValue >= 0 &&
                        capacityValue != null && capacityValue > 0
                    ) {
                        onConfirm(name, tareValue, capacityValue, setAsActive)
                    }
                },
                enabled = name.isNotBlank() &&
                        tare.toFloatOrNull() != null && tare.toFloatOrNull()!! >= 0 &&
                        capacity.toFloatOrNull() != null && capacity.toFloatOrNull()!! > 0
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
