package com.example.frontend.screens.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getTelegramURL
import com.example.frontend.api.getTelegramUUID

@Composable
fun DialogTelegramBot(
    onDismiss: () -> Unit,
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmation") },
        text = { Text("Do You Want To Get OTP in Telegram?") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                scope.launch {
                    try {
                        val uuidBody = RetrofitInstance.dementiaAPI.getTelegramUUID()
                        if (uuidBody != null) {
                            val uuid = uuidBody.value
                            val telegramUrl = RetrofitInstance.dementiaAPI.getTelegramURL(uuid)
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                            context.startActivity(intent)
                        }
                    } catch (e: Exception) {
                        Log.e("DialogTelegramBot", "Telegram UUID API exception", e)
                    }
                    onYes()
                }
            }) {
                Text("Yes")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onNo()
            }) {
                Text("No")
            }
        }
    )
}
