package com.example.frontend.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.frontend.R

@Composable
fun CloseAppDialog(
    onDismiss: () -> Unit,
    onConfirmClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = colorResource(R.color.warning_orange)
            )
        },
        title = {
            Text(
                text = "Close MindTrace?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_on_surface),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Are you sure you want to close MindTrace?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.dark_on_surface),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "You will be signed out and returned to the main screen.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmClose,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.warning_orange),
                    contentColor = colorResource(R.color.dark_on_primary)
                )
            ) {
                Text(
                    "Yes, Close",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colorResource(R.color.dark_on_surface)
                )
            ) {
                Text(
                    "Cancel",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = colorResource(R.color.dark_surface),
        titleContentColor = colorResource(R.color.dark_on_surface),
        textContentColor = colorResource(R.color.dark_on_surface)
    )
}
