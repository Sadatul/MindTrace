package com.example.frontend.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.frontend.screens.models.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun ChatBubble(message: ChatMessage) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (message.isUser) 16.dp else 0.dp,
                        bottomEnd = if (message.isUser) 0.dp else 16.dp
                    )
                )
                .background(
                    if (message.isUser) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.secondaryContainer
                )
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = if (message.isUser) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Start
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}

private fun formatTimestamp(utcTimestamp: String): String {
    return try {
        val instant = Instant.parse(utcTimestamp)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .format(localDateTime)
    } catch (e: Exception) {
        utcTimestamp
    }
}