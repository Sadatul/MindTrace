package com.example.frontend.screens.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getTelegramURL
import com.example.frontend.api.getTelegramUUID
import com.example.frontend.R

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
        title = { Text("Telegram") },
        text = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_telegram_logo),
                    contentDescription = "Telegram",
                    modifier = Modifier.size(24.dp),
                    tint = Color.Unspecified
                )
                Text("Connect to Telegram")
            }
        },
        confirmButton = {
            TextButton(onClick = {
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
                    onDismiss()
                    onYes()
                }
            }) {
                Text("Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                onNo()
            }) {
                Text("Skip")
            }
        }
    )
}
