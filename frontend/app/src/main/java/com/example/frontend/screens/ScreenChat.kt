package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.getIdToken
import com.example.frontend.api.models.RequestChat
import com.example.frontend.screens.components.ChatBubble
import com.example.frontend.screens.models.ChatMessage
import kotlinx.coroutines.launch
import java.time.Instant

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ChatScreen() {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pageSize = 20


    // Load initial messages
    LaunchedEffect(Unit) {
        loadMessages(0, pageSize) { newMessages, hasMore ->
            messages = newMessages
            hasMorePages = hasMore
            currentPage = 0
            // Scroll to bottom after initial load
            if (newMessages.isNotEmpty()) {
                scope.launch {
                    listState.scrollToItem(0) // Since reverseLayout=true, 0 is the newest message
                }
            }
        }
    }

    // Handle scroll to load more - Fixed logic
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            // Since reverseLayout=true, the "last" visible item is actually the oldest message
            // Load more when we're near the end of the list (oldest messages)
            if (lastVisibleIndex != null &&
                lastVisibleIndex >= messages.size - 2 && // Near the end
                !isLoadingMore &&
                hasMorePages &&
                messages.isNotEmpty()) {

                isLoadingMore = true
                val nextPage = currentPage + 1

                loadMessages(nextPage, pageSize) { newMessages, hasMore ->
                    if (newMessages.isNotEmpty()) {
                        // Add older messages to the end of the list (since reverseLayout=true)
                        messages = messages + newMessages
                        currentPage = nextPage
                        hasMorePages = hasMore
                        println("Loaded page $nextPage with ${newMessages.size} messages")
                    }
                    isLoadingMore = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Chat messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            // Show loading indicator at the top (which appears at bottom due to reverseLayout)
            if (isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            items(messages) { message ->
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
                            // Add new message to the beginning of the list (newest first)
                            messages = listOf(ChatMessage(
                                text = userMessage,
                                isUser = true,
                                timestamp = Instant.now().toString()
                            )) + messages
                            inputText = ""

                            // Auto-scroll to bottom (newest message) immediately after adding user message
                            listState.animateScrollToItem(0)

                            try {
                                val token = RetrofitInstance.dementiaAPI.getIdToken()
                                Log.d("ScreenChat", "Token: $token")

                                val response = RetrofitInstance.dementiaAPI.sendChatMessage(
                                    "Bearer $token",
                                    RequestChat(userMessage)
                                )
                                if (response.isSuccessful) {
                                    response.body()?.string()?.let { reply ->
                                        // Add assistant response to the beginning
                                        messages = listOf(ChatMessage(
                                            text = reply,
                                            isUser = false,
                                            timestamp = Instant.now().toString()
                                        )) + messages
                                        // Scroll to bottom (newest message) - index 0 with reverseLayout=true
                                        listState.animateScrollToItem(0)
                                    }
                                } else {
                                    messages = listOf(ChatMessage(
                                        text = "Error: ${response.code()}",
                                        isUser = false,
                                        timestamp = Instant.now().toString()
                                    )) + messages
                                    // Scroll to bottom after error message
                                    listState.animateScrollToItem(0)
                                }
                            } catch (e: Exception) {
                                messages = listOf(ChatMessage(
                                    text = "Error: ${e.message}",
                                    isUser = false,
                                    timestamp = Instant.now().toString()
                                )) + messages
                                // Scroll to bottom after error message
                                listState.animateScrollToItem(0)
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
        }
    }
}

private suspend fun loadMessages(
    page: Int,
    size: Int,
    onComplete: (List<ChatMessage>, Boolean) -> Unit
) {
    try {
        val token = RetrofitInstance.dementiaAPI.getIdToken()
        Log.d("ScreenChat", "Token: $token")
        val response = RetrofitInstance.dementiaAPI.getChatHistory("Bearer $token", page, size)
        if (response.isSuccessful) {
            response.body()?.let { chatResponse ->
                val newMessages = chatResponse.content.map { msg ->
                    ChatMessage(
                        text = if (msg.type == "ASSISTANT") msg.message.dropLast(4) else msg.message,
                        isUser = msg.type == "USER",
                        timestamp = msg.createdAt
                    )
                }
                val hasMore = page < chatResponse.page.totalPages - 1
                onComplete(newMessages, hasMore)
            }
        } else {
            onComplete(emptyList(), false)
        }
    } catch (e: Exception) {
        println("Error loading messages: ${e.message}")
        onComplete(emptyList(), false)
    }
}
