package com.example.campergas.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import com.example.campergas.R

@Composable
fun PermissionDialog(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Warning,
    onAccept: () -> Unit,
    onDismiss: () -> Unit,
    acceptText: String = stringResource(R.string.dialog_accept),
    dismissText: String = stringResource(R.string.dialog_cancel),
    dismissible: Boolean = true
) {
    AlertDialog(
        onDismissRequest = if (dismissible) onDismiss else {
            {}
        },
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
        title = stringResource(R.string.bluetooth_permission_title),
        message = stringResource(R.string.bluetooth_permission_message),
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = stringResource(R.string.bluetooth_permission_grant),
        dismissText = stringResource(R.string.bluetooth_permission_later)
    )
}

@Composable
fun BluetoothDisabledDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    PermissionDialog(
        title = stringResource(R.string.bluetooth_disabled_title),
        message = stringResource(R.string.bluetooth_disabled_message),
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = stringResource(R.string.bluetooth_disabled_enable),
        dismissText = stringResource(R.string.dialog_cancel)
    )
}

@Composable
fun LocationDisabledDialog(
    onAccept: () -> Unit,
    onDismiss: () -> Unit
) {
    PermissionDialog(
        title = stringResource(R.string.location_disabled_title),
        message = stringResource(R.string.location_disabled_message),
        onAccept = onAccept,
        onDismiss = onDismiss,
        acceptText = stringResource(R.string.location_disabled_enable),
        dismissText = stringResource(R.string.dialog_cancel)
    )
}
