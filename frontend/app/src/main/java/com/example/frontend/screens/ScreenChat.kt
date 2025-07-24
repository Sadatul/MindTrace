package com.example.frontend.screens

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
import com.example.frontend.api.models.RequestChat
import com.example.frontend.screens.components.ChatBubble
import com.example.frontend.screens.components.LastChatDialog
import com.example.frontend.screens.models.ChatMessage
import kotlinx.coroutines.launch
import java.time.Instant

private const val TAG = "ScreenChat"

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onCancelDialog: () -> Unit = {},
    navigationBar: NavigationBarComponent
) {
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    var showLastChatDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // For sending messages
    var isLoadingMore by remember { mutableStateOf(false) } // For loading older messages
    var isInitiallyLoading by remember { mutableStateOf(true) } // For initial message load
    var currentPage by remember { mutableIntStateOf(0) }
    var hasMorePages by remember { mutableStateOf(true) }
    var userInfo: UserInfo? by remember { mutableStateOf(null) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val pageSize = 20    // Load user info from cache

    LaunchedEffect(Unit) {
        userInfo = SelfUserInfoCache.getUserInfo()
        Log.d(TAG, "Loaded user info from cache: ${userInfo?.name}")
    }

    // Show dialog on first entry
    LaunchedEffect(Unit) {
        showLastChatDialog = true
    }

    // Load initial messages
    fun loadInitialMessages() {
        scope.launch {
            isInitiallyLoading = true
            loadMessages(0, pageSize) { newMessages, hasMore ->
                messages = newMessages
                hasMorePages = hasMore
                currentPage = 0
                isInitiallyLoading = false
                // Scroll to bottom after initial load
                if (newMessages.isNotEmpty()) {
                    scope.launch {
                        // Ensure the list is populated before trying to scroll
                        listState.scrollToItem(0) // Since reverseLayout=true, 0 is the newest message
                    }
                }
            }
        }
    }

    // Handle scroll to load more
    LaunchedEffect(listState, messages.size, hasMorePages) { // Add messages.size and hasMorePages as keys
        snapshotFlow {
            // Check if the first visible item (oldest due to reverseLayout) is within threshold
            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
        }.collect { firstVisibleIndex ->
            // If reverseLayout is true, the "first" visible item is the oldest.
            // We want to load more when the user scrolls towards the "top" of the list,
            // which visually appears as scrolling up to see older messages.
            // The condition should be when the *last* visible item (which is actually the oldest loaded message)
            // is near the end of the current `messages` list.
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
            if (lastVisibleItemIndex != null &&
                lastVisibleItemIndex >= messages.size - 5 && // Load when 5 items from the end (oldest) are visible
                !isLoadingMore &&
                hasMorePages &&
                messages.isNotEmpty() &&
                !isInitiallyLoading // Don't trigger load more during initial load
            ) {
                Log.d(TAG, "Load more triggered. LastVisible: $lastVisibleItemIndex, Total: ${messages.size}")
                isLoadingMore = true
                val nextPage = currentPage + 1
                loadMessages(nextPage, pageSize) { newMessages, hasMore ->
                    if (newMessages.isNotEmpty()) {
                        // Prepend older messages to maintain chronological order with reverseLayout
                        messages = messages + newMessages // Older messages are added to the end
                        currentPage = nextPage
                        hasMorePages = hasMore
                        Log.d(TAG,"Loaded page $nextPage with ${newMessages.size} messages. Total: ${messages.size}")
                    } else {
                        Log.d(TAG,"Loaded page $nextPage, no new messages. HasMore: $hasMore")
                        hasMorePages = false // Explicitly set if no new messages
                    }
                    isLoadingMore = false
                }
            }
        }
    }

    Scaffold(
        containerColor = colorResource(R.color.dark_background),
        topBar = {
            var showTelegramDialog by remember { mutableStateOf(false) }
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
                    IconButton(
                        onClick = { showTelegramDialog = true },
                        modifier = Modifier.padding(start = 0.dp)
                    ) {
                        val boxSize = 200.dp
                        val borderWidth = 2.dp
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(boxSize)
                                .clip(CircleShape)
                                .background(colorResource(R.color.white))
                                .border(
                                    width = borderWidth,
                                    color = colorResource(R.color.gradient_caregiver_start),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_telegram),
                                contentDescription = "Telegram",
                                tint = Color(0xFF229ED9),
                                modifier = Modifier.size(boxSize - borderWidth * 2)
                            )
                        }
                    }
                    if (showTelegramDialog) {
                        androidx.compose.material3.AlertDialog(
                            onDismissRequest = { showTelegramDialog = false },
                            title = { Text("Confirmation") },
                            text = { Text("You want to chat with telegram?") },
                            confirmButton = {
                                androidx.compose.material3.TextButton(onClick = {
                                    showTelegramDialog = false
                                    // TODO: Implement Telegram chat action here
                                }) {
                                    Text("Yes")
                                }
                            },
                            dismissButton = {
                                androidx.compose.material3.TextButton(onClick = { showTelegramDialog = false }) {
                                    Text("No")
                                }
                            }
                        )
                    }
                },actions = {
                    Box(
                        modifier = Modifier.padding(end = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userInfo?.profilePicture != null) {
                            AsyncImage(
                                model = userInfo!!.profilePicture,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(getChatProfilePictureSize())
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                error = painterResource(R.drawable.ic_launcher_foreground)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Default Profile",
                                tint = colorResource(R.color.dark_primary),
                                modifier = Modifier.size(getChatProfilePictureSize())
                            )
                        }
                    }
                },   
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.gradient_caregiver_start), // Different color for the bar
                    titleContentColor = colorResource(R.color.white),
                    navigationIconContentColor = colorResource(R.color.white),
                    actionIconContentColor = colorResource(R.color.white)
                )
            )
        },
        bottomBar = {
            when (userInfo?.role) {
                "CAREGIVER" -> navigationBar.CaregiverNavigationBar(Screen.Chat)
                "PATIENT" -> navigationBar.PatientNavigationBar(Screen.Chat)
                else -> navigationBar.PatientNavigationBar(Screen.Chat) // fallback
            }
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
                    .padding(horizontal = 16.dp, vertical = 8.dp) // Adjusted padding
            ) { // Corrected: Removed extra parenthesis here
                if (isInitiallyLoading && messages.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colorResource(R.color.dark_primary))
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        reverseLayout = true
                    ) {
                        // AI Typing Indicator
                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = R.drawable.ic_mindtrace_logo,
                                        contentDescription = "AI Assistant",
                                        modifier = Modifier
                                            .size(getChatProfilePictureSize())
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Blinking 'AI is thinking' text, word by word
                                            val infiniteTransition = rememberInfiniteTransition(label = "ai_thinking_words")
                                            val wordIndex by infiniteTransition.animateFloat(
                                                initialValue = 0f,
                                                targetValue = 4f,
                                                animationSpec = infiniteRepeatable(
                                                    animation = tween(durationMillis = 1200, easing = LinearEasing),
                                                    repeatMode = RepeatMode.Restart
                                                ), label = "ai_thinking_words_anim"
                                            )
                                            val currentWord = when (wordIndex.toInt()) {
                                                0 -> "AI"
                                                1 -> "is"
                                                2 -> "thinking"
                                                else -> "AI is thinking"
                                            }
                                            Text(
                                                text = currentWord,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                color = colorResource(id = R.color.dark_on_surface)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            AnimatedDots()
                                        }
                                    }
                                }
                            }
                        }

                        // Messages are displayed from newest (index 0) to oldest
                        items(messages, key = { it.timestamp + it.text }) { message ->
                            ChatBubble(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Loading indicator for pagination at the "top" (visually bottom because of reverseLayout)
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = colorResource(R.color.dark_primary),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Reduced space before input

                // Input field and send button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (inputText.isNotBlank()) {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            colorResource(R.color.dark_surface_variant),
                                            colorResource(R.color.gradient_patient_start).copy(alpha = 0.1f)
                                        )
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            colorResource(R.color.dark_surface_variant),
                                            colorResource(R.color.dark_surface_variant)
                                        )
                                    )
                                }
                            ),
                        placeholder = {
                            Text(
                                "Type your message...",
                                color = colorResource(R.color.dark_on_surface).copy(alpha = 0.6f),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = colorResource(R.color.transparent),
                            unfocusedContainerColor = colorResource(R.color.transparent),
                            focusedTextColor = colorResource(R.color.dark_on_surface),
                            unfocusedTextColor = colorResource(R.color.dark_on_surface),
                            cursorColor = colorResource(R.color.gradient_caregiver_start),
                            focusedIndicatorColor = colorResource(R.color.gradient_caregiver_start),
                            unfocusedIndicatorColor = colorResource(R.color.dark_on_surface).copy(alpha = 0.3f)
                        ),
                        enabled = !isLoading && !isInitiallyLoading, // Disable while sending or initial loading
                        maxLines = 5 // Allow multiple lines but constrain height
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (isLoading) {
                        // Circular progress indicator in place of send button
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    } else {
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    val userMessageText = inputText
                                    inputText = "" // Clear input immediately

                                    // Add user message optimistically
                                    val userMessage = ChatMessage(
                                        text = userMessageText,
                                        isUser = true,
                                        timestamp = Instant.now().toString() // Ensure unique timestamp
                                    )
                                    messages = listOf(userMessage) + messages // Prepend for reverseLayout

                                    scope.launch {
                                        // Scroll to the new message (index 0)
                                        listState.animateScrollToItem(0)
                                        isLoading = true
                                        try {
                                            val token = RetrofitInstance.dementiaAPI.getIdToken()
                                            Log.d(TAG, "Token for sending: $token")

                                            val response = RetrofitInstance.dementiaAPI.sendChatMessage(
                                                "Bearer $token",
                                                RequestChat(userMessageText)
                                            )
                                            if (response.isSuccessful) {
                                                response.body()?.string()?.let { reply ->
                                                    val assistantMessage = ChatMessage(
                                                        text = reply,
                                                        isUser = false,
                                                        timestamp = Instant.now().toString() // Ensure unique timestamp
                                                    )
                                                    messages = listOf(assistantMessage) + messages
                                                    listState.animateScrollToItem(0)
                                                } ?: run {
                                                    // Handle empty successful response if necessary
                                                    val errorMessage = ChatMessage(
                                                        text = "Received empty response from server.",
                                                        isUser = false,
                                                        timestamp = Instant.now().toString()
                                                    )
                                                    messages = listOf(errorMessage) + messages
                                                    listState.animateScrollToItem(0)
                                                }
                                            } else {
                                                val errorBody = response.errorBody()?.string() ?: "Unknown error"
                                                Log.e(TAG, "Chat send error: "+response.code()+" - "+errorBody)
                                                val errorMessage = ChatMessage(
                                                    text = "Error: "+response.code(),
                                                    isUser = false,
                                                    timestamp = Instant.now().toString()
                                                )
                                                messages = listOf(errorMessage) + messages
                                                listState.animateScrollToItem(0)
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Chat send exception", e)
                                            val errorMessage = ChatMessage(
                                                text = "Error: "+(e.localizedMessage ?: "Network error"),
                                                isUser = false,
                                                timestamp = Instant.now().toString()
                                            )
                                            messages = listOf(errorMessage) + messages
                                            listState.animateScrollToItem(0)
                                        } finally {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            enabled = inputText.isNotBlank() && !isInitiallyLoading,
                            modifier = Modifier
                                .background(
                                    brush = if (!isLoading && inputText.isNotBlank() && !isInitiallyLoading) {
                                        Brush.radialGradient(
                                            colors = listOf(
                                                colorResource(R.color.gradient_caregiver_start),
                                                colorResource(R.color.gradient_caregiver_end)
                                            )
                                        )
                                    } else {
                                        Brush.radialGradient(
                                            colors = listOf(
                                                colorResource(R.color.dark_surface_variant),
                                                colorResource(R.color.dark_surface_variant)
                                            )
                                        )
                                    },
                                    shape = CircleShape
                                )
                                .size(48.dp) // Standard FAB size
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (!isLoading && inputText.isNotBlank() && !isInitiallyLoading)
                                    colorResource(R.color.white)
                                else
                                    colorResource(R.color.dark_on_surface).copy(alpha = 0.6f)
                            )
                        }
                    }
                }


            }
        }
    }
      // Show Last Chat Dialog
    if (showLastChatDialog) {
        LastChatDialog(
            onDismiss = {
                showLastChatDialog = false
                onCancelDialog() // Navigate back to dashboard
            },
            onViewLastChat = {
                showLastChatDialog = false
                loadInitialMessages()
            },
            onStartNewChat = {
                showLastChatDialog = false
                messages = emptyList()
                isInitiallyLoading = false
            }
        )
    }
}

@Composable
private fun getChatProfilePictureSize(): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth >= 400.dp -> 42.dp
        screenWidth >= 360.dp -> 38.dp
        else -> 34.dp
    }
}

@SuppressLint("NewApi")
suspend fun loadMessages(
    page: Int,
    size: Int,
    onComplete: (List<ChatMessage>, Boolean) -> Unit
) {
    Log.d(TAG, "Loading messages for page: $page, size: $size")
    try {
        val token = RetrofitInstance.dementiaAPI.getIdToken()
        // Log.d(TAG, "Token for loading messages: $token") // Be cautious logging tokens
        val response = RetrofitInstance.dementiaAPI.getChatHistory("Bearer $token", page, size)
        if (response.isSuccessful) {
            response.body()?.let { chatResponse ->
                Log.d(TAG, "Load successful. Page: ${chatResponse.page.number}, TotalPages: ${chatResponse.page.totalPages}, ContentSize: ${chatResponse.content.size}")
                val newMessages = chatResponse.content.mapNotNull { msg ->
                    if (msg.message.isNotBlank()) { // Filter out potentially empty messages
                        ChatMessage(
                            // Consider if ".dropLast(4)" is always correct for assistant messages
                            text = if (msg.type == "ASSISTANT" && msg.message.endsWith("<em>")) msg.message.dropLast(4) else msg.message,
                            isUser = msg.type == "USER",
                            timestamp = msg.createdAt
                        )
                    } else null
                }
                val hasMore = chatResponse.page.number < chatResponse.page.totalPages - 1
                onComplete(newMessages, hasMore)
            } ?: run {
                Log.w(TAG, "Load messages successful but response body was null. Page: $page")
                onComplete(emptyList(), false)
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown error"
            Log.e(TAG, "Error loading messages: ${response.code()} - $errorBody. Page: $page")
            onComplete(emptyList(), false)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Exception loading messages: ${e.message}. Page: $page", e)
        onComplete(emptyList(), false)
    }
}

// AnimatedDots composable for typing/thinking animation
@Composable
fun AnimatedDots(
    dotSize: Dp = 16.dp,
    color: Color = Color.White,
    dotSpacing: Dp = 8.dp,
    dotCount: Int = 3
) {
    // dotCount is now a parameter
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    val animatedDot = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = dotCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "dotAnim"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(dotSpacing)
    ) {
        for (i in 0 until dotCount) {
            val visible = animatedDot.value > i
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .padding(3.dp)
                    .background(
                        color = if (visible) color else colorResource(R.color.dark_surface_variant),
                        shape = CircleShape
                    )
            )
        }
    }
}