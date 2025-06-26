package com.example.frontend.screens.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.frontend.R
import com.example.frontend.screens.models.ChatMessage
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatBubble(untrimmedMessage: ChatMessage) {
    var message: ChatMessage = untrimmedMessage

    if (!untrimmedMessage.isUser && untrimmedMessage.text.endsWith("null")) {
        message = ChatMessage(
            text = untrimmedMessage.text.dropLast(4),
            isUser = false,
            timestamp = untrimmedMessage.timestamp
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
            modifier = Modifier.fillMaxWidth()
        ) {
            // AI Avatar (left side for assistant messages)
            if (!message.isUser) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.gradient_caregiver_start)
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI Assistant",
                        tint = colorResource(R.color.white),
                        modifier = Modifier.padding(6.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Message Bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (message.isUser) 20.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 20.dp
                        )
                    )
                    .background(
                        if (message.isUser) {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(R.color.card_patient),
                                    colorResource(R.color.gradient_patient_start)
                                )
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(
                                    colorResource(R.color.gradient_caregiver_start),
                                    colorResource(R.color.gradient_caregiver_end)
                                )
                            )
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    color = colorResource(R.color.white),
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            
            // User Avatar (right side for user messages)
            if (message.isUser) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = colorResource(R.color.card_patient)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User",
                        tint = colorResource(R.color.white),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = formatTimestamp(message.timestamp),
            style = MaterialTheme.typography.bodySmall,
            color = colorResource(R.color.dark_on_surface).copy(alpha = 0.7f),
            fontWeight = FontWeight.Light,
            modifier = Modifier.padding(horizontal = if (message.isUser) 40.dp else 40.dp)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun formatTimestamp(utcTimestamp: String): String {
    return try {
        val instant = Instant.parse(utcTimestamp)
        val localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .format(localDateTime)
    } catch (_: Exception) {
        utcTimestamp
    }
}