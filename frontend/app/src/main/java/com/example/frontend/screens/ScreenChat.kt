package com.example.frontend.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.models.RequestChat
import com.example.frontend.screens.components.ChatBubble
import com.example.frontend.screens.models.ChatMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(token: String) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Set the auth token
    LaunchedEffect(token) {
        RetrofitInstance.setAuthToken(token)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat messages
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                ChatBubble(message)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input field and send button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp)),
                placeholder = { Text("Type your message...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            val userMessage = inputText
                            messages = messages + ChatMessage(userMessage, true)
                            inputText = ""

                            try {
                                val response = RetrofitInstance.dementiaAPI.sendChatMessage(
                                    RequestChat(userMessage)
                                )
                                if (response.isSuccessful) {
                                    response.body()?.string()?.let { reply ->
                                        messages = messages + ChatMessage(reply, false)
                                    }
                                } else {
                                    messages = messages + ChatMessage(
                                        "Error: ${response.code()}",
                                        false
                                    )
                                }
                            } catch (e: Exception) {
                                messages = messages + ChatMessage(
                                    "Error: ${e.message}",
                                    false
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                },
                enabled = !isLoading && inputText.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send"
                )
            }

//            Button(
//                onClick = {
//                    if (inputText.isNotBlank()) {
//                        scope.launch {
//                            isLoading = true
//                            val userMessage = inputText
//                            messages = messages + ChatMessage(userMessage, true)
//                            inputText = ""
//
//                            try {
//                                val response = RetrofitInstance.dementiaAPI.sendChatMessage(
//                                    RequestChat(userMessage)
//                                )
//                                if (response.isSuccessful) {
//                                    response.body()?.string()?.let { reply ->
//                                        messages = messages + ChatMessage(reply, false)
//                                    }
//                                } else {
//                                    messages = messages + ChatMessage(
//                                        "Error: ${response.code()}",
//                                        false
//                                    )
//                                }
//                            } catch (e: Exception) {
//                                messages = messages + ChatMessage(
//                                    "Error: ${e.message}",
//                                    false
//                                )
//                            } finally {
//                                isLoading = false
//                            }
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .size(48.dp)
//                    .clip(RoundedCornerShape(24.dp)),
//                enabled = !isLoading && inputText.isNotBlank()
//            ) {
////                Icon(
////                    imageVector = Icons.Filled.Send,
////                    contentDescription = "Send"
////                )
//                Text("📤", fontSize = 24.sp)
//
//            }
        }
    }
}