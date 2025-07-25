package com.example.frontend.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.frontend.R
import com.example.frontend.api.RetrofitInstance
import com.example.frontend.api.SelfUserInfoCache
import com.example.frontend.api.UserInfo
import com.example.frontend.api.deleteReminder
import com.example.frontend.api.getIdToken
import com.example.frontend.api.getReminders
import com.example.frontend.api.models.Reminder
import com.example.frontend.api.models.ReminderSchedule
import com.example.frontend.api.models.ReminderType
import com.example.frontend.api.models.RepeatMode
import com.example.frontend.api.models.RequestStoreReminder
import com.example.frontend.api.models.TimePeriod
import com.example.frontend.api.storeReminder
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Month

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ScreenReminder(userId: String?, navigationBar: NavigationBarComponent, onBack: () -> Unit) {
    var showCreateForm by remember { mutableStateOf(false) }
    var reminders by remember { mutableStateOf<List<Reminder>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf<Reminder?>(null) }
    val scope = rememberCoroutineScope()
    var userInfo: UserInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(userId) {
        loadReminders(userId) { loadedReminders ->
            reminders = loadedReminders
        }

        if (userId == null) userInfo = SelfUserInfoCache.getUserInfo()
        else {
            val token = RetrofitInstance.dementiaAPI.getIdToken()
            if (token != null) RetrofitInstance.dementiaAPI.getUserInfo(token, userId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (userInfo != null) "${userInfo!!.name}'s Reminders" else "Reminders",
                            style = typography.titleLarge,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        if (userInfo != null && userInfo!!.profilePicture != null) {
                            AsyncImage(
                                model = userInfo!!.profilePicture,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.white), CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                                error = painterResource(R.drawable.ic_launcher_foreground)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, colorResource(R.color.white), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Default Profile",
                                    modifier = Modifier.size(28.dp),
                                    tint = colorResource(R.color.white)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(R.color.card_patient),
                    titleContentColor = colorResource(R.color.white),
                    navigationIconContentColor = colorResource(R.color.white),
                    actionIconContentColor = colorResource(R.color.white)
                )
            )
        },
        bottomBar = {
            if (userId == null) navigationBar.PatientNavigationBar(selectedScreen = Screen.Reminder(null))
            else navigationBar.CaregiverNavigationBar(selectedScreen = Screen.Reminder(null))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Reminders",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                FloatingActionButton(
                    onClick = { showCreateForm = true },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Reminder")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (reminders.isEmpty()) {
                Text(
                    text = "No reminders yet. Tap + to create one.",
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyColumn {
                    items(reminders) { reminder ->
                        ReminderCard(
                            reminder = reminder,
                            onDelete = {
                                showDeleteDialog = reminder
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateForm) {
        CreateReminderDialog(
            userId = userId,
            onDismiss = { showCreateForm = false },
            onReminderCreated = {
                showCreateForm = false
                scope.launch {
                    loadReminders(userId) { loadedReminders ->
                        reminders = loadedReminders
                    }
                }
            }
        )
    }

    showDeleteDialog?.let { reminderToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Reminder") },
            text = { 
                Text("Are you sure you want to delete \"${reminderToDelete.title}\"? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            if (RetrofitInstance.dementiaAPI.deleteReminder(reminderToDelete.id)) {
                                loadReminders(userId) { loadedReminders ->
                                    reminders = loadedReminders
                                }
                            }
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
suspend fun loadReminders(userId: String?, onResult: (List<Reminder>) -> Unit) {
    val response = RetrofitInstance.dementiaAPI.getReminders(userId, null, null, 0, 50)
    val loadedReminders = response?.content ?: emptyList()
    onResult(loadedReminders)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReminderCard(
    reminder: Reminder,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = reminder.description,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = reminder.schedule.toReadableString(),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Reminder",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateReminderDialog(
    userId: String?,
    onDismiss: () -> Unit,
    onReminderCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var period by remember { mutableStateOf(TimePeriod.AM) }
    var isRecurring by remember { mutableStateOf(false) }
    var repeatMode by remember { mutableStateOf<RepeatMode?>(null) }
    var selectedDaysOfWeek by remember { mutableStateOf<Set<DayOfWeek>>(emptySet()) }
    var selectedDaysOfMonth by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedMonth by remember { mutableStateOf<Int?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Reminder") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = title.isBlank()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = description.isBlank(),
                    maxLines = 3
                )

                Text(
                    text = "Schedule *",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )

                TimePickerSection(
                    hour = hour,
                    minute = minute,
                    period = period,
                    onHourChange = { hour = it },
                    onMinuteChange = { minute = it },
                    onPeriodChange = { period = it }
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRecurring) 
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else 
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Recurring reminder",
                                fontWeight = FontWeight.Medium,
                                fontSize = 16.sp,
                                color = if (isRecurring) 
                                    MaterialTheme.colorScheme.primary
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isRecurring) "Reminder will repeat" else "One-time reminder",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { 
                                isRecurring = it
                                if (!it) {
                                    repeatMode = null
                                    selectedDaysOfWeek = emptySet()
                                    selectedDaysOfMonth = emptySet()
                                    selectedMonth = null
                                }
                            }
                        )
                    }
                }

                if (isRecurring) {
                    RepetitionModeSection(
                        repeatMode = repeatMode,
                        onRepeatModeChange = { 
                            repeatMode = it
                            selectedDaysOfWeek = emptySet()
                            selectedDaysOfMonth = emptySet()
                            selectedMonth = null
                        }
                    )

                    repeatMode?.let { mode ->
                        when (mode) {
                            RepeatMode.DAY_OF_WEEK -> {
                                DaysOfWeekSelection(
                                    selectedDays = selectedDaysOfWeek,
                                    onSelectionChange = { selectedDaysOfWeek = it }
                                )
                            }
                            RepeatMode.DAY_OF_MONTH -> {
                                DaysOfMonthSelection(
                                    selectedDays = selectedDaysOfMonth,
                                    onSelectionChange = { selectedDaysOfMonth = it }
                                )
                            }
                        }

                        if (selectedDaysOfWeek.isNotEmpty() || selectedDaysOfMonth.isNotEmpty()) {
                            MonthSelection(
                                selectedMonth = selectedMonth,
                                onMonthChange = { selectedMonth = it }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {

                    if (repeatMode == null) {
                        repeatMode = RepeatMode.DAY_OF_WEEK
                    }

                    if (title.isNotBlank() && description.isNotBlank()) {
                        scope.launch {
                            isLoading = true
                            val schedule = ReminderSchedule(
                                hour = hour,
                                minute = minute,
                                period = period,
                                repeatMode = repeatMode!!,
                                daysOfWeek = if (repeatMode == RepeatMode.DAY_OF_WEEK) selectedDaysOfWeek.toList() else null,
                                daysOfMonth = if (repeatMode == RepeatMode.DAY_OF_MONTH) selectedDaysOfMonth.toList() else null,
                                month = selectedMonth,
                                isRecurring = isRecurring
                            )
                            
                            val request = RequestStoreReminder(
                                userId = userId,
                                title = title,
                                description = description,
                                reminderType = ReminderType.BASE,
                                schedule = schedule
                            )
                            
                            val success = RetrofitInstance.dementiaAPI.storeReminder(request)
                            Log.d("SCREEN_REMINDER", "store response $success")
                            isLoading = false
                            
                            if (success) {
                                onReminderCreated()
                            }
                        }
                    }
                },
                enabled = title.isNotBlank() && description.isNotBlank() && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Create")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimePickerSection(
    hour: Int,
    minute: Int,
    period: TimePeriod,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onPeriodChange: (TimePeriod) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Set Time",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hour Wheel
                TimeWheelPicker(
                    items = (1..12).toList(),
                    selectedItem = hour,
                    onItemSelected = onHourChange,
                    modifier = Modifier.weight(1f),
                    label = "Hour"
                )
                
                Text(
                    text = ":",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Minute Wheel
                TimeWheelPicker(
                    items = (0..59).toList(),
                    selectedItem = minute,
                    onItemSelected = onMinuteChange,
                    modifier = Modifier.weight(1f),
                    label = "Minute",
                    formatter = { "%02d".format(it) }
                )
                
                // AM/PM Selector
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Period",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TimePeriod.entries.forEach { p ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                                .clickable { onPeriodChange(p) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (period == p) 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (period == p) 4.dp else 1.dp
                            )
                        ) {
                            Text(
                                text = p.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                textAlign = TextAlign.Center,
                                color = if (period == p) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (period == p) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeWheelPicker(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    formatter: (Int) -> String = { it.toString() }
) {
    val itemHeight = 48.dp
    val visibleItemsCount = 3
    val wheelHeight = itemHeight * visibleItemsCount
    
    // Create infinite wrapping list
    val infiniteItems = remember(items) {
        // Repeat items multiple times for infinite scrolling effect
        val repeatCount = 1000
        List(items.size * repeatCount) { index ->
            items[index % items.size]
        }
    }
    
    val listState = rememberLazyListState()
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    
    // Calculate initial scroll position to center the selected item
    val initialIndex = remember(items, selectedItem) {
        val baseIndex = infiniteItems.size / 2
        val selectedIndex = items.indexOf(selectedItem)
        if (selectedIndex >= 0) {
            baseIndex - (baseIndex % items.size) + selectedIndex
        } else {
            baseIndex
        }
    }
    
    // Monitor scroll state to detect selection changes and update selection
    LaunchedEffect(Unit) {
        snapshotFlow { 
            listState.firstVisibleItemIndex to listState.isScrollInProgress 
        }.collect { (firstVisibleIndex, isScrolling) ->
            if (!isScrolling) {
                // With contentPadding of itemHeight (48dp) and 3 visible items (144dp total height)
                // The center item should be at firstVisibleItemIndex (since padding pushes first item to center)
                val centerIndex = firstVisibleIndex
                val centerItem = infiniteItems.getOrNull(centerIndex)
                centerItem?.let { item ->
                    if (item != selectedItem) {
                        onItemSelected(item)
                    }
                }
            }
        }
    }
    
    // Scroll to selected item when it changes externally
    LaunchedEffect(selectedItem) {
        val currentCenterIndex = listState.firstVisibleItemIndex
        val currentCenterItem = infiniteItems.getOrNull(currentCenterIndex)
        
        if (currentCenterItem != selectedItem) {
            // Find the closest index with the selected item
            val startSearch = maxOf(0, currentCenterIndex - items.size)
            val endSearch = minOf(infiniteItems.size - 1, currentCenterIndex + items.size)
            
            var targetIndex = -1
            var minDistance = Int.MAX_VALUE
            
            for (i in startSearch..endSearch) {
                if (infiniteItems[i] == selectedItem) {
                    val distance = kotlin.math.abs(i - currentCenterIndex)
                    if (distance < minDistance) {
                        minDistance = distance
                        targetIndex = i
                    }
                }
            }
            
            if (targetIndex >= 0) {
                listState.animateScrollToItem(targetIndex)
            }
        }
    }
    
    // Initialize scroll position
    LaunchedEffect(Unit) {
        listState.scrollToItem(initialIndex)
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .height(wheelHeight)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        )
                    )
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = itemHeight),
                flingBehavior = snapBehavior,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(infiniteItems.size) { index ->
                    val item = infiniteItems[index]
                    val isSelected = item == selectedItem
                    val centerIndex = listState.firstVisibleItemIndex
                    val distanceFromCenter = kotlin.math.abs(index - centerIndex)
                    
                    // Make the currently selected item prominent regardless of scroll position
                    val alpha = when {
                        isSelected -> 1f
                        distanceFromCenter <= 1 -> 0.7f
                        else -> 0.3f
                    }
                    
                    val coroutineScope = rememberCoroutineScope()
                    
                    Text(
                        text = formatter(item),
                        modifier = Modifier
                            .height(itemHeight)
                            .fillMaxWidth()
                            .clickable { 
                                onItemSelected(item)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                            .alpha(alpha)
                            .padding(vertical = 12.dp),
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
            
            // Selection indicator box in center
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(itemHeight)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        2.dp, 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
            )
        }
    }
}

@Composable
fun RepetitionModeSection(
    repeatMode: RepeatMode?,
    onRepeatModeChange: (RepeatMode?) -> Unit
) {
    Column {
        Text("Repetition Mode", fontWeight = FontWeight.Medium)
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.selectable(
                selected = repeatMode == null,
                onClick = { onRepeatModeChange(null) }
            )
        ) {
            RadioButton(
                selected = repeatMode == null,
                onClick = { onRepeatModeChange(null) }
            )
            Text("No specific repetition")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.selectable(
                selected = repeatMode == RepeatMode.DAY_OF_WEEK,
                onClick = { onRepeatModeChange(RepeatMode.DAY_OF_WEEK) }
            )
        ) {
            RadioButton(
                selected = repeatMode == RepeatMode.DAY_OF_WEEK,
                onClick = { onRepeatModeChange(RepeatMode.DAY_OF_WEEK) }
            )
            Text("Days of Week")
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.selectable(
                selected = repeatMode == RepeatMode.DAY_OF_MONTH,
                onClick = { onRepeatModeChange(RepeatMode.DAY_OF_MONTH) }
            )
        ) {
            RadioButton(
                selected = repeatMode == RepeatMode.DAY_OF_MONTH,
                onClick = { onRepeatModeChange(RepeatMode.DAY_OF_MONTH) }
            )
            Text("Days of Month")
        }
    }
}

@Composable
fun DaysOfWeekSelection(
    selectedDays: Set<DayOfWeek>,
    onSelectionChange: (Set<DayOfWeek>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Days",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar order starting with Sunday (more intuitive)
            val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            // Reorder DayOfWeek.entries to start with Sunday (Sunday is last in entries)
            val allDays = DayOfWeek.entries
            val daysOrder = listOf(allDays.last()) + allDays.dropLast(1)
            
            // Split into 2 rows: 4 days in first row, 3 in second
            val firstRowDays = daysOrder.take(4)
            val secondRowDays = daysOrder.drop(4)
            val firstRowLabels = dayLabels.take(4)
            val secondRowLabels = dayLabels.drop(4)
            
            // First row (Sun, Mon, Tue, Wed)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                firstRowDays.forEachIndexed { index, day ->
                    DayBubble(
                        day = day,
                        label = firstRowLabels[index],
                        isSelected = selectedDays.contains(day),
                        onToggle = {
                            val newSelection = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                            onSelectionChange(newSelection)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Second row (Thu, Fri, Sat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Add spacers to center the 3 items
                Spacer(modifier = Modifier.weight(0.5f))
                
                secondRowDays.forEachIndexed { index, day ->
                    DayBubble(
                        day = day,
                        label = secondRowLabels[index],
                        isSelected = selectedDays.contains(day),
                        onToggle = {
                            val newSelection = if (selectedDays.contains(day)) {
                                selectedDays - day
                            } else {
                                selectedDays + day
                            }
                            onSelectionChange(newSelection)
                        }
                    )
                    
                    if (index < secondRowDays.size - 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                
                Spacer(modifier = Modifier.weight(0.5f))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when (selectedDays.size) {
                    0 -> "Tap days to select"
                    7 -> "Every day selected"
                    1 -> "1 day selected"
                    else -> "${selectedDays.size} days selected"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DayBubble(
    day: DayOfWeek,
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) {
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                } else {
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                else 
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clickable { onToggle() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) 
                MaterialTheme.colorScheme.onPrimary 
            else 
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun DaysOfMonthSelection(
    selectedDays: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Days of Month",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Calendar header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { dayLabel ->
                    Text(
                        text = dayLabel,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Calendar grid
            (1..31).chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    week.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                            )
                                        )
                                    } else {
                                        Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.surface,
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    else 
                                        MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    val newSelection = if (isSelected) {
                                        selectedDays - day
                                    } else {
                                        selectedDays + day
                                    }
                                    onSelectionChange(newSelection)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) 
                                    MaterialTheme.colorScheme.onPrimary 
                                else 
                                    MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    repeat(7 - week.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = when (selectedDays.size) {
                    0 -> "Tap dates to select"
                    1 -> "1 date selected"
                    else -> "${selectedDays.size} dates selected"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthSelection(
    selectedMonth: Int?,
    onMonthChange: (Int?) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Select Month (Optional)",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // "Any month" option
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMonthChange(null) },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedMonth == null) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (selectedMonth == null) 6.dp else 2.dp
                )
            ) {
                Text(
                    text = "Any Month",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (selectedMonth == null) 
                        MaterialTheme.colorScheme.onPrimary 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Month grid
            (1..12).chunked(3).forEach { monthGroup ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    monthGroup.forEach { monthNum ->
                        val monthName = Month.of(monthNum).name.lowercase().replaceFirstChar { it.uppercase() }
                        val isSelected = selectedMonth == monthNum
                        
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onMonthChange(monthNum) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isSelected) 8.dp else 3.dp
                            )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surface,
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    )
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthName.take(3),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    repeat(3 - monthGroup.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (selectedMonth == null) 
                    "Will repeat in any month" 
                else 
                    "Will repeat only in ${Month.of(selectedMonth).name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}