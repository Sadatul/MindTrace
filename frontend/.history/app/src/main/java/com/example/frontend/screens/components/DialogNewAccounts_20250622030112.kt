package com.example.frontend.screens.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.frontend.R

@Composable
fun DialogNewAccounts(
    onDismiss: () -> Unit,
    onConfirmSignInLauncher: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.AccountBox,
                contentDescription = "Account Switch",
                tint = colorResource(R.color.dark_primary)
            )
        },
        title = {
            Text(
                text = "Switch Account",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.dark_on_surface),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column {
                Text(
                    text = "Do you want to sign in with a different Google account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colorResource(R.color.dark_on_surface),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "This will sign you out of your current account and show available Google accounts to choose from.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSignInLauncher,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.dark_primary),
                    contentColor = colorResource(R.color.dark_on_primary)
                )
            ) {
                Text(
                    "Choose Account",
                    fontWeight = FontWeight.Medium
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