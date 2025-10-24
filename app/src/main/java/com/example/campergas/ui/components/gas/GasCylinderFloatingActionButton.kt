package com.example.campergas.ui.components.gas

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Floating action button for adding a new gas cylinder.
 *
 * Material 3 FAB with a plus icon, typically positioned at the bottom-right
 * of the screen to allow users to quickly add new cylinders.
 *
 * @param onClick Callback invoked when button is clicked
 * @param modifier Modifier for layout customization
 */
@Composable
fun GasCylinderFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add cylinder",
            tint = MaterialTheme.colorScheme.onPrimary
        )
    }
}
