package com.example.frontend.screens.components

import android.content.Context
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getTelegramURL
import com.example.frontend.api.getTelegramUUID

@Composable
fun DialogTelegramBot(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    context: Context,
    scope: CoroutineScope,
    onResult: (Boolean) -> Unit // true = YES, false = NO
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Confirmation") },
            text = { Text("Do You Want To Get OTP in Telegram?") },
            confirmButton = {
                TextButton(onClick = {
                    onDismiss()
                    onResult(true)
                    scope.launch {
                        try {
                            val uuidBody = RetrofitInstance.dementiaAPI.getTelegramUUID()
                            if (uuidBody != null) {
                                val uuid = uuidBody.value
                                val telegramUrl = RetrofitInstance.dementiaAPI.getTelegramURL(uuid)
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(telegramUrl))
                                context.startActivity(intent)
                            }
                        } catch (e: Exception) {
                            Log.e("DialogTelegramBot", "Telegram UUID API exception", e)
                        }
                    }
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onDismiss()
                    onResult(false)
                }) {
                    Text("No")
                }
            }
        )
    }
}
