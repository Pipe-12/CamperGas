package com.example.campergas.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Warning,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    acceptText: String = "Aceptar",
    dismissText: String = "Cancelar",
    dismissible: Boolean = true
) {
    AlertDialog(
        onDismissRequest = if (dismissible) onDismiss else { {} },
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(
                onClick = onAccept
            ) {
                Text(acceptText)
            }
        },
        dismissButton = if (dismissible) {
            @Composable {
                TextButton(
                    onClick = onDismiss
                ) {
                    Text(dismissText)
                }
            }
        } else null,
        properties = DialogProperties(
            dismissOnBackPress = dismissible,
            dismissOnClickOutside = dismissible
        )
    )
}

@Composable
fun BluetoothPermissionDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    PermissionDialog(
        title = "Permisos de Bluetooth",
        message = "CamperGas necesita acceso a Bluetooth y ubicación para conectarse con los dispositivos de la caravana y obtener datos de peso, inclinación y consumo de gas.",
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = "Conceder permisos",
        dismissText = "Ahora no"
    )
}

@Composable
fun BluetoothDisabledDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    PermissionDialog(
        title = "Bluetooth Desactivado",
        message = "Para usar CamperGas necesitas activar el Bluetooth. ¿Quieres activarlo ahora?",
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = "Activar Bluetooth",
        dismissText = "Cancelar"
    )
}

@Composable
fun LocationDisabledDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    PermissionDialog(
        title = "Ubicación Desactivada",
        message = "Para escanear dispositivos Bluetooth necesitas tener la ubicación activada. ¿Quieres activarla ahora?",
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = "Activar ubicación",
        dismissText = "Cancelar"
    )
}
