package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.models.RequestChat
import com.example.frontend.screens.components.ChatBubble
import com.example.frontend.screens.models.ChatMessage
import kotlinx.coroutines.launch
import java.time.Instant

private const val TAG = "ScreenChat"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit = {}
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pageSize = 20

    // Load user info from cache
    LaunchedEffect(Unit) {
        userInfo = SelfUserInfoCache.getUserInfo()
        Log.d(TAG, "Loaded user info from cache: ${userInfo?.name}")
    }


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
        }    }

    Scaffold(
        containerColor = colorResource(R.color.dark_background),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = userInfo?.name ?: "Chat",
                        color = colorResource(R.color.dark_on_surface),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = colorResource(R.color.dark_primary),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                actions = {
                    // Profile picture on the right
                    if (userInfo?.profilePicture != null) {
                        AsyncImage(
                            model = userInfo!!.profilePicture,
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(getChatProfilePictureSize())
                                .clip(CircleShape)
                                .padding(end = 8.dp),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.ic_launcher_foreground),
                            error = painterResource(R.drawable.ic_launcher_foreground)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Default Profile",
                            tint = colorResource(R.color.dark_primary),
                            modifier = Modifier
                                .size(getChatProfilePictureSize())
                                .padding(end = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.dark_surface),
                    titleContentColor = colorResource(R.color.dark_on_surface),
                    navigationIconContentColor = colorResource(R.color.dark_primary),
                    actionIconContentColor = colorResource(R.color.dark_primary)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            colorResource(R.color.dark_surface),
                            colorResource(R.color.dark_background),
                            colorResource(R.color.dark_background)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {            ) {
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
                                CircularProgressIndicator(
                                    color = colorResource(R.color.dark_primary)
                                )
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
                        placeholder = { 
                            Text(
                                "Type your message...",
                                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.6f)
                            ) 
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorResource(R.color.dark_surface_variant),
                            unfocusedContainerColor = colorResource(R.color.dark_surface_variant),
                            focusedTextColor = colorResource(R.color.dark_on_surface),
                            unfocusedTextColor = colorResource(R.color.dark_on_surface),
                            cursorColor = colorResource(R.color.dark_primary),
                            focusedIndicatorColor = colorResource(R.color.dark_primary),
                            unfocusedIndicatorColor = colorResource(R.color.dark_on_surface).copy(alpha = 0.3f)
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
                },                        enabled = !isLoading && inputText.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = colorResource(R.color.dark_primary)
                        )
                    }
                }
            }
        }
    }
}

// Helper function for responsive profile picture sizing in chat
@Composable
private fun getChatProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth >= 400.dp -> 40.dp
        screenWidth >= 360.dp -> 36.dp
        else -> 32.dp
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
