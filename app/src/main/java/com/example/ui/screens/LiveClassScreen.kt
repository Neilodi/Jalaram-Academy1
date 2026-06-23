package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.data.User
import com.example.data.LiveClassSession
import com.example.viewmodel.ErpViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun LiveClassScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val liveClassActive by viewModel.liveClassActive.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    AnimatedContent(
        targetState = liveClassActive,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "live_classroom_layer"
    ) { isActive ->
        if (isActive) {
            // Live Synchronous Broadcaster Window
            ActiveClassroomSession(
                currentUserRole = currentUser?.role ?: "",
                viewModel = viewModel,
                onEnd = {
                    viewModel.stopLiveClass()
                    Toast.makeText(context, "Left live classroom session.", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            // Live Class Lobby Directory
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(JalaramPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Videocam stream icon",
                        tint = JalaramPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Live Sync Classrooms",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = JalaramTextMain
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Interact synchronously with high-fidelity, limitless, completely free video lectures powered by open Jitsi meeting protocols.",
                    color = JalaramTextSub,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ADMIN PANEL TO START A NEW LIVE CLASS
                if (currentUser?.role == "Admin") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                        border = BorderStroke(1.dp, JalaramPrimary.copy(alpha = 0.3f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCall,
                                    contentDescription = null,
                                    tint = JalaramPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Start Limitless Live Class",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = JalaramTextMain
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "On starting, automatic push notifications will immediately alert targeted class students.",
                                color = JalaramTextSub,
                                fontSize = 11.sp
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))

                            // 1. Subject Select dropdown
                            val subjectsList = listOf("Physics", "Maths", "Chemistry", "Biology", "English", "Informatics")
                            var selectedSubject by remember { mutableStateOf(subjectsList[0]) }
                            var subjectDropdownExpanded by remember { mutableStateOf(false) }

                            Text("Subject Name", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JalaramTextMain)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { subjectDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramTextMain)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedSubject)
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                DropdownMenu(
                                    expanded = subjectDropdownExpanded,
                                    onDismissRequest = { subjectDropdownExpanded = false },
                                    modifier = Modifier.background(JalaramSurface)
                                ) {
                                    subjectsList.forEach { sub ->
                                        DropdownMenuItem(
                                            text = { Text(sub) },
                                            onClick = {
                                                selectedSubject = sub
                                                subjectDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 2. Batch (Class Class) dropdown
                            val batchesList by viewModel.batchesList.collectAsState()
                            var selectedBatch by remember { mutableStateOf("") }
                            var batchDropdownExpanded by remember { mutableStateOf(false) }

                            val targetBatchString = if (selectedBatch.isEmpty()) {
                                if (batchesList.isNotEmpty()) {
                                    selectedBatch = batchesList[0].name
                                    batchesList[0].name
                                } else {
                                    "No Batch Available"
                                }
                            } else {
                                selectedBatch
                            }

                            Text("Target Student Class (Batch)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JalaramTextMain)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { batchDropdownExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramTextMain),
                                    enabled = batchesList.isNotEmpty()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(targetBatchString)
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                                    }
                                }
                                if (batchesList.isNotEmpty()) {
                                    DropdownMenu(
                                        expanded = batchDropdownExpanded,
                                        onDismissRequest = { batchDropdownExpanded = false },
                                        modifier = Modifier.background(JalaramSurface)
                                    ) {
                                        batchesList.forEach { batch ->
                                            DropdownMenuItem(
                                                text = { Text(batch.name) },
                                                onClick = {
                                                    selectedBatch = batch.name
                                                    batchDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // 3. Topic name input field
                            var topicText by remember { mutableStateOf("") }
                            Text("Lecture Topic / Title", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JalaramTextMain)
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = topicText,
                                onValueChange = { topicText = it },
                                placeholder = { Text("e.g. Electromagnetism Introduction") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JalaramPrimary,
                                    unfocusedBorderColor = JalaramBorder
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    val finalTopic = if (topicText.trim().isEmpty()) "Interactive Discussion" else topicText.trim()
                                    viewModel.createLiveClass(
                                        subject = selectedSubject,
                                        batch = targetBatchString,
                                        topic = finalTopic,
                                        startedBy = currentUser?.name ?: "Academy Host"
                                    )
                                    topicText = ""
                                    Toast.makeText(context, "Live class stream started!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CastConnected, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Broadcast Live Class Stream", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // ONGOING LIVE CLASSROOMS DIRECTORY LISTING
                Text(
                    text = "Ongoing Live Classrooms",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain
                    ),
                    modifier = Modifier.align(Alignment.Start).padding(vertical = 12.dp)
                )

                val activeLiveClasses by viewModel.activeLiveClasses.collectAsState()

                if (activeLiveClasses.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                        border = BorderStroke(1.dp, JalaramBorder)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NoMeetingRoom,
                                contentDescription = null,
                                tint = JalaramTextSub.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Active Live Streams",
                                fontWeight = FontWeight.Bold,
                                color = JalaramTextMain,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Admins have not launched any live lectures right now. Check back shortly or view notifications panel for live alerts.",
                                color = JalaramTextSub,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    activeLiveClasses.forEach { liveSession ->
                        val studentBatch = currentUser?.batch
                        val isForMyBatch = studentBatch != null && studentBatch.lowercase() == liveSession.batch.lowercase()

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 480.dp)
                                .padding(vertical = 6.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isForMyBatch) JalaramPrimaryLight.copy(alpha = 0.2f) else JalaramSurface
                            ),
                            border = BorderStroke(
                                1.5.dp, 
                                if (isForMyBatch) JalaramPrimary else JalaramBorder
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            color = JalaramDangerContainer,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .clip(CircleShape)
                                                        .background(JalaramDanger)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "LIVE",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = JalaramDanger
                                                )
                                            }
                                        }

                                        if (isForMyBatch) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = JalaramPrimary,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "MY CLASS",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (currentUser?.role == "Admin") {
                                        IconButton(
                                            onClick = {
                                                viewModel.endLiveClassSession(liveSession.id)
                                                Toast.makeText(context, "Live session terminated.", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cancel,
                                                contentDescription = "Terminate Session",
                                                tint = JalaramDanger
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = liveSession.topic,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = JalaramTextMain
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = JalaramPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = liveSession.subject,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = JalaramTextMain
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Groups,
                                            contentDescription = null,
                                            tint = JalaramTextSub,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = liveSession.batch,
                                            fontSize = 12.sp,
                                            color = JalaramTextSub
                                        )
                                    }
                                }

                                Text(
                                    text = "Host Instructor: ${liveSession.startedBy}",
                                    fontSize = 11.sp,
                                    color = JalaramTextSub,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        viewModel.joinLiveClass(liveSession)
                                        Toast.makeText(context, "Connecting to live classroom...", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MeetingRoom,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Connect to Classroom", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveClassroomSession(
    currentUserRole: String,
    viewModel: ErpViewModel,
    onEnd: () -> Unit
) {
    var micMuted by remember { mutableStateOf(false) }
    var videoDisabled by remember { mutableStateOf(false) }
    val usersList by viewModel.usersList.collectAsState()
    val currentSession by viewModel.currentLiveSession.collectAsState()

    val subjectName = currentSession?.subject ?: "Physics"
    val batchClass = currentSession?.batch ?: "Grade 10-A"
    val topicTitle = currentSession?.topic ?: "Interactive Session"
    val jitsiMeetUrl = currentSession?.jitsiLink ?: "https://meet.jit.si/JalaramAcademySync"
    val instructorName = currentSession?.startedBy ?: "Dr. Shalini Vyas"

    val context = LocalContext.current

    // Dynamic wave progression ticker for visual feed simulation
    var tick by remember { mutableStateOf(0f) }
    LaunchedEffect(key1 = true) {
        while (true) {
            delay(50)
            tick += 0.15f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Night Space theme
    ) {
        // Stream Heading Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(JalaramDanger)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$subjectName Sync ($batchClass) - LIVE",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Surface(
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SERVER: MUMBAI-1",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Simulating Lecture Stream Broadcast via Canvas wave drawing
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            if (!videoDisabled) {
                // Sine-oscillating Canvas drawings representing live interactive camera feedback streams
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val midY = size.height / 2f
                    val waveSpacing = 50f
                    val amplitude = 40f

                    // Radial center background glows
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(JalaramPrimary.copy(alpha = 0.3f), Color.Transparent),
                            center = Offset(size.width / 2f, size.height / 2f)
                        ),
                        radius = size.width / 2f
                    )

                    // Oscillating wave loops
                    for (i in 0..5) {
                        val pathOffset = i * waveSpacing
                        val pathPoints = mutableListOf<Offset>()
                        for (x in 0..size.width.toInt() step 5) {
                            val y = midY + amplitude * sin((x * 0.008f) + tick + (i * 0.5f)) + pathOffset - 120f
                            pathPoints.add(Offset(x.toFloat(), y))
                        }
                        for (j in 0 until pathPoints.size - 1) {
                            drawLine(
                                color = JalaramAccent.copy(alpha = 0.4f - (i * 0.05f)),
                                start = pathPoints[j],
                                end = pathPoints[j + 1],
                                strokeWidth = 3f
                            )
                        }
                    }
                }

                // JITSI LINK QUICK CONTROL PANEL OVERLAY (limitless free live class)
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🚀 Limitless Jitsi Meeting Room",
                        color = JalaramAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topicTitle,
                        color = Color.White,
                        fontSize = 11.sp,
                        maxLines = 1,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Jitsi Meeting Link", jitsiMeetUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied Jitsi link!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy Link", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(jitsiMeetUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to launch Jitsi video room", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Join Room", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Faculty label box
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "📽️ Instructor: $instructorName",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            } else {
                Text(
                    text = "Instructor Video feed disabled",
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Collaborative Group Chat and participant listings
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Classroom Collaborative Feed",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )

            ChatBubbleSim(sender = instructorName, msg = "Welcome class! Today we are discussing '$topicTitle'. Feel free to click 'Join Room' to join the video directly.", isTeacher = true, currentUserRole = currentUserRole, usersList = usersList)
            ChatBubbleSim(sender = "Suresh Kumar", msg = "Sir/Ma'am, the audio & video feed resolution via Jitsi is incredibly clear and fast!", isTeacher = false, currentUserRole = currentUserRole, usersList = usersList)
            ChatBubbleSim(sender = instructorName, msg = "Excellent, Jitsi provides us a completely free and limitless whiteboard and sharing platform.", isTeacher = true, currentUserRole = currentUserRole, usersList = usersList)
            ChatBubbleSim(sender = "Aditi Sharma", msg = "Just joined the Jitsi room, it works flawlessly!", isTeacher = false, currentUserRole = currentUserRole, usersList = usersList)
        }

        // Live Controls Dock Panel
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic toggle
                IconButton(
                    onClick = { micMuted = !micMuted },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (micMuted) JalaramDanger else Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Mute audio",
                        tint = Color.White
                    )
                }

                // Hang Up
                Button(
                    onClick = onEnd,
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramDanger),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Session", fontWeight = FontWeight.Black)
                }

                // Video toggle
                IconButton(
                    onClick = { videoDisabled = !videoDisabled },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (videoDisabled) JalaramDanger else Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = if (videoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Toggle video camera",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubbleSim(
    sender: String,
    msg: String,
    isTeacher: Boolean,
    currentUserRole: String,
    usersList: List<User>
) {
    val studentUser = remember(usersList, sender) {
        usersList.firstOrNull { it.name == sender } ?: if (sender == "Suresh Kumar") {
            User("STU-102", "Suresh Kumar", "Student", "9876543210", null, "Grade 10-A", null, "1234", "Active")
        } else if (sender == "Aditi Sharma") {
            User("STU-103", "Aditi Sharma", "Student", "", "9988776655", "Grade 10-A", null, "1234", "Active")
        } else {
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTeacher) JalaramPrimary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isTeacher && studentUser != null && (currentUserRole == "Admin" || currentUserRole == "Teacher")) {
                    UserPhonePSIndicator(studentUser)
                }
                Text(
                    text = if (isTeacher) "⭐ $sender (Faculty)" else sender,
                    fontWeight = FontWeight.Bold,
                    color = if (isTeacher) JalaramAccent else Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = msg,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 16.sp
            )
        }
    }
}
