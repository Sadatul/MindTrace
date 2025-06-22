package com.example.frontend.screens

import android.annotation.SuppressLint
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
// import coil.compose.AsyncImage // Duplicate import
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.getIdToken
// import com.example.frontend.api.getIdToken // Already imported by RetrofitInstance.dementiaAPI.getIdToken()
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
    var isLoading by remember { mutableStateOf(false) } // For sending messages
    var isLoadingMore by remember { mutableStateOf(false) } // For loading older messages
    var isInitiallyLoading by remember { mutableStateOf(true) } // For initial message load
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
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = userInfo?.name ?: "Chat",
                        color = colorResource(R.color.dark_on_surface),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard",
                            tint = colorResource(R.color.white),
                            modifier = Modifier.size(28.dp)
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
                },                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.gradient_caregiver_start), // Different color for the bar
                    titleContentColor = colorResource(R.color.white),
                    navigationIconContentColor = colorResource(R.color.white),
                    actionIconContentColor = colorResource(R.color.white)
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
                        // Messages are displayed from newest (index 0) to oldest
                        items(messages, key = { it.timestamp + it.text }) { message -> // Added key for better performance
                            ChatBubble(message)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Loading indicator for pagination at the "top" (visually bottom because of reverseLayout)
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp), // Increased padding for visibility
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = colorResource(R.color.dark_primary),
                                        modifier = Modifier.size(32.dp) // Slightly larger
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
                        .padding(bottom = 8.dp), // Padding for keyboard spacing
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
                            focusedIndicatorColor = colorResource(R.color.dark_primary), // Or Color.Transparent
                            unfocusedIndicatorColor = colorResource(R.color.dark_on_surface).copy(alpha = 0.3f) // Or Color.Transparent
                        ),
                        enabled = !isLoading && !isInitiallyLoading, // Disable while sending or initial loading
                        maxLines = 5 // Allow multiple lines but constrain height
                    )

                    Spacer(modifier = Modifier.width(8.dp))

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
                                            Log.e(TAG, "Chat send error: ${response.code()} - $errorBody")
                                            val errorMessage = ChatMessage(
                                                text = "Error: ${response.code()}",
                                                isUser = false,
                                                timestamp = Instant.now().toString()
                                            )
                                            messages = listOf(errorMessage) + messages
                                            listState.animateScrollToItem(0)
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Chat send exception", e)
                                        val errorMessage = ChatMessage(
                                            text = "Error: ${e.localizedMessage ?: "Network error"}",
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
                        enabled = !isLoading && inputText.isNotBlank() && !isInitiallyLoading,
                        modifier = Modifier
                            .background(
                                color = if (!isLoading && inputText.isNotBlank() && !isInitiallyLoading)
                                    colorResource(R.color.dark_primary)
                                else
                                    colorResource(R.color.dark_surface_variant),
                                shape = CircleShape
                            )
                            .size(48.dp) // Standard FAB size
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = colorResource(R.color.dark_on_primary) // White or light color on primary
                        )
                    }
                }
            }
        }
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
                            timestamp = msg.createdAt.toString()
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