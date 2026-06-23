package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.User
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Data representation of a Chat Group/Channel
data class ChatRoom(
    val name: String,
    val type: String, // "Announcement" or "Subject"
    val subject: String, // "All" or Physics, Maths etc
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun ChatsScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val chatMessagesList by viewModel.chatMessagesList.collectAsState()

    val allRooms = remember {
        listOf(
            ChatRoom("Main Announcements", "Announcement", "All", "Campus updates & central notices", Icons.Default.Campaign),
            ChatRoom("Physics Chatroom", "Subject", "Physics", "Physics discussion and assignments", Icons.Default.Science),
            ChatRoom("Maths Chatroom", "Subject", "Maths", "Mathematics exercises and formulas", Icons.Default.Calculate),
            ChatRoom("Chemistry Chatroom", "Subject", "Chemistry", "Chemistry lab protocols and theory", Icons.Default.Biotech),
            ChatRoom("Biology Chatroom", "Subject", "Biology", "Biology theory and class updates", Icons.Default.Eco),
            ChatRoom("English Chatroom", "Subject", "English", "Grammar drills and literature updates", Icons.Default.MenuBook)
        )
    }

    // Filter allowed rooms based on user assignments
    val allowedRooms = remember(currentUser, allRooms) {
        val user = currentUser
        if (user == null) emptyList()
        else {
            allRooms.filter { room ->
                if (room.type == "Announcement") {
                    true // announcements visible to all
                } else {
                    // Subject-respective security checks
                    if (user.role == "Admin") {
                        true // Admins see all subject groups
                    } else {
                        val userSubjects = user.subjects?.split(",")
                            ?.map { it.trim().lowercase() }
                            ?.filter { it.isNotEmpty() } ?: emptyList()
                        userSubjects.contains(room.subject.lowercase())
                    }
                }
            }
        }
    }

    var selectedRoom by remember(allowedRooms) {
        mutableStateOf<ChatRoom?>(allowedRooms.firstOrNull())
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(JalaramBgMain)
    ) {
        val isWideScreen = maxWidth > 700.dp

        if (isWideScreen) {
            // Tablet/Desktop Split View
            Row(modifier = Modifier.fillMaxSize()) {
                // Left Panel: Channel List
                Card(
                    modifier = Modifier
                        .width(320.dp)
                        .fillMaxHeight(),
                    colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(1.dp, JalaramBorder)
                ) {
                    ChannelsListPane(
                        allowedRooms = allowedRooms,
                        selectedRoom = selectedRoom,
                        currentUser = currentUser,
                        chatMessagesList = chatMessagesList,
                        onRoomSelect = { selectedRoom = it }
                    )
                }

                // Right Panel: Active Chat Room
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    if (selectedRoom != null) {
                        val activeRoom = selectedRoom!!
                        val filteredMessages = remember(chatMessagesList, activeRoom) {
                            chatMessagesList.filter {
                                it.channelType == activeRoom.type && it.channelSubject == activeRoom.subject
                            }
                        }

                        ActiveChatPane(
                            room = activeRoom,
                            currentUser = currentUser,
                            messages = filteredMessages,
                            onSendMessage = { text ->
                                currentUser?.let { user ->
                                    viewModel.sendChatMessage(
                                        senderId = user.userId,
                                        senderName = user.name,
                                        senderRole = user.role,
                                        channelType = activeRoom.type,
                                        channelSubject = activeRoom.subject,
                                        text = text
                                    )
                                }
                            }
                        )
                    } else {
                        ChatPlaceholderPane()
                    }
                }
            }
        } else {
            // Mobile: Alternating backstack navigation
            if (selectedRoom != null) {
                val activeRoom = selectedRoom!!
                val filteredMessages = remember(chatMessagesList, activeRoom) {
                    chatMessagesList.filter {
                        it.channelType == activeRoom.type && it.channelSubject == activeRoom.subject
                    }
                }

                ActiveChatPane(
                    room = activeRoom,
                    currentUser = currentUser,
                    messages = filteredMessages,
                    onSendMessage = { text ->
                        currentUser?.let { user ->
                            viewModel.sendChatMessage(
                                senderId = user.userId,
                                senderName = user.name,
                                senderRole = user.role,
                                channelType = activeRoom.type,
                                channelSubject = activeRoom.subject,
                                text = text
                            )
                        }
                    },
                    onBackClick = { selectedRoom = null }
                )
            } else {
                ChannelsListPane(
                    allowedRooms = allowedRooms,
                    selectedRoom = selectedRoom,
                    currentUser = currentUser,
                    chatMessagesList = chatMessagesList,
                    onRoomSelect = { selectedRoom = it }
                )
            }
        }
    }
}

@Composable
fun ChannelsListPane(
    allowedRooms: List<ChatRoom>,
    selectedRoom: ChatRoom?,
    currentUser: User?,
    chatMessagesList: List<ChatMessage>,
    onRoomSelect: (ChatRoom) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // User Meta Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JalaramPrimaryLight.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = currentUser?.name ?: "Academy Member",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = JalaramPrimaryDark
                )
                Text(
                    text = when (currentUser?.role) {
                        "Admin" -> "👑 Central Administrator"
                        "Teacher" -> "💼 Faculty: ${currentUser.subjects ?: "N/A"}"
                        else -> "🎓 Enrolled Tracker: ${currentUser?.subjects ?: "General Module"}"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = JalaramAccent
                )
            }
        }

        // Section Title
        Text(
            text = "My Conversations",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = JalaramTextMain,
            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )

        if (allowedRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No chats assigned. Please request an administrator to register your subjects under User Management.",
                    fontSize = 12.sp,
                    color = JalaramTextSub,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allowedRooms) { room ->
                    val isSelected = selectedRoom == room
                    val lastMessage = remember(chatMessagesList, room) {
                        chatMessagesList.filter {
                            it.channelType == room.type && it.channelSubject == room.subject
                        }.lastOrNull()
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRoomSelect(room) }
                            .testTag("chat_channel_${room.subject.lowercase()}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) JalaramPrimaryLight else JalaramSurface
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) JalaramPrimary else JalaramBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else JalaramPrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = room.icon,
                                    contentDescription = null,
                                    tint = JalaramPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = room.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = JalaramTextMain,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (room.type == "Announcement") {
                                        Surface(
                                            color = JalaramDangerContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "OFFICIAL",
                                                color = JalaramDanger,
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = if (lastMessage != null) {
                                        "${lastMessage.senderName}: ${lastMessage.messageText}"
                                    } else {
                                        room.subtitle
                                    },
                                    fontSize = 11.sp,
                                    color = JalaramTextSub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveChatPane(
    room: ChatRoom,
    currentUser: User?,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBackClick: (() -> Unit)? = null
) {
    var inputText by remember { mutableStateOf("") }
    var showSecurityAudits by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Automatically scroll to bottom when message list expands
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JalaramBgMain)
    ) {
        // Chat Window Header Bar
        Surface(
            color = JalaramSurface,
            shadowElevation = 2.dp,
            border = BorderStroke(1.dp, JalaramBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Return to chats menu",
                            tint = JalaramPrimary
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JalaramPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = room.icon,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = room.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = JalaramTextMain
                    )
                    Text(
                        text = if (room.type == "Announcement") "Public Notice board" else "Restrained subject-only discussion group",
                        fontSize = 10.sp,
                        color = JalaramTextSub,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(onClick = { showSecurityAudits = true }) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "View chat permissions check",
                        tint = JalaramPrimary
                    )
                }
            }
        }

        // Messages History Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            if (messages.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = JalaramPrimary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Clean Discussion Register!",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Send a welcoming query to initiate conversation with fellow members regarding ${room.subject}.",
                        fontSize = 12.sp,
                        color = JalaramTextSub,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
                ) {
                    items(messages) { msg ->
                        val isSelf = msg.senderId == currentUser?.userId
                        ChatMessageRow(message = msg, isSelf = isSelf)
                    }
                }
            }
        }

        // Security Permitted Input Control Bar
        val canWrite = remember(room, currentUser) {
            val user = currentUser
            if (user == null) false
            else {
                if (room.type == "Announcement") {
                    user.role == "Admin" || user.role == "Teacher"
                } else {
                    true // subject respective chats: any assigned user who can access can write!
                }
            }
        }

        Surface(
            color = JalaramSurface,
            border = BorderStroke(1.dp, JalaramBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (canWrite) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Write your course statement...") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedBorderColor = JalaramPrimary,
                            unfocusedBorderColor = JalaramBorder
                        ),
                        shape = RoundedCornerShape(24.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText.trim())
                                    inputText = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isBlank()) Color.LightGray else JalaramPrimary)
                            .clickable(enabled = inputText.isNotBlank()) {
                                onSendMessage(inputText.trim())
                                inputText = ""
                            }
                            .testTag("send_chat_msg_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Submit chat message",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                // Announcements Students warning message block
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    colors = CardDefaults.cardColors(containerColor = JalaramWarningContainer.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, JalaramWarning.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = JalaramWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Only Administrators and assigned Teachers are allowed to issue bulletin board Announcements.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JalaramWarning,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showSecurityAudits) {
        AlertDialog(
            onDismissRequest = { showSecurityAudits = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = JalaramPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Security Clearance Audit")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Current Room: ${room.name}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = JalaramTextMain
                    )
                    Text(
                        text = if (room.type == "Announcement") {
                            "• Board Announcements are visible to all registered student and employee divisions.\n• Only Admins and Teachers can write posts here."
                        } else {
                            "• Exclusive discussion room for subject: ${room.subject}.\n• ONLY accessible to faculty teaching ${room.subject}, students pursuing ${room.subject}, and administrators."
                        },
                        fontSize = 12.sp,
                        color = JalaramTextSub
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = JalaramPrimaryLight.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Access Policy: Enforced by Room Database filter.",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            color = JalaramPrimary,
                            modifier = Modifier.padding(8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSecurityAudits = false },
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                ) {
                    Text("Decline Inspect")
                }
            }
        )
    }
}

@Composable
fun ChatPlaceholderPane() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JalaramBgMain),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.QuestionAnswer,
                contentDescription = null,
                tint = JalaramPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(72.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to Jalaram Academy Chatroom",
                fontWeight = FontWeight.Bold,
                color = JalaramTextMain,
                fontSize = 16.sp
            )
            Text(
                text = "Select a channel/workspace from the left to start collaborating with teachers and administrators.",
                color = JalaramTextSub,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 32.dp, end = 32.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun ChatMessageRow(message: ChatMessage, isSelf: Boolean) {
    val formatter = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val timeStr = remember(message.timestamp) { formatter.format(Date(message.timestamp)) }

    val alignment = if (isSelf) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (isSelf) JalaramPrimary else JalaramSurface
    val textColor = if (isSelf) Color.White else JalaramTextMain
    val subTextColor = if (isSelf) Color.White.copy(alpha = 0.7f) else JalaramTextSub

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(0.82f),
            horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isSelf) {
                // Small Circle Avatar graphic
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(JalaramPrimaryLight)
                        .padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (message.senderName.isNotEmpty()) message.senderName.take(1).uppercase() else "U",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = JalaramPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Body Bubble Card
            Card(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 16.dp,
                    bottomEnd = if (isSelf) 0.dp else 16.dp
                ),
                colors = CardDefaults.cardColors(containerColor = bubbleColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (!isSelf) BorderStroke(1.dp, JalaramBorder) else null
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (!isSelf) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Text(
                                text = message.senderName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = JalaramPrimaryDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Badge indicating role
                            Surface(
                                color = when (message.senderRole) {
                                    "Admin" -> JalaramDangerContainer
                                    "Teacher" -> JalaramWarningContainer
                                    else -> JalaramPrimaryLight
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = message.senderRole,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (message.senderRole) {
                                        "Admin" -> JalaramDanger
                                        "Teacher" -> JalaramWarning
                                        else -> JalaramPrimary
                                    },
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = message.messageText,
                        fontSize = 13.sp,
                        color = textColor,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        color = subTextColor,
                        modifier = Modifier.align(Alignment.End),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
