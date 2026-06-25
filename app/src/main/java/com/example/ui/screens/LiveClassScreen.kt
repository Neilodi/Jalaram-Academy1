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
import androidx.compose.foundation.border
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.*
import com.example.data.User
import com.example.data.LiveClassSession
import com.example.viewmodel.ErpViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll

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
                    // Elevated Broadcaster Control States
                    val subjectsList = listOf("Physics", "Maths", "Chemistry", "Biology", "English", "Informatics")
                    val batchesList by viewModel.batchesList.collectAsState()

                    var selectedSubject by remember { mutableStateOf(subjectsList[0]) }
                    var subjectDropdownExpanded by remember { mutableStateOf(false) }

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

                    var topicText by remember { mutableStateOf("") }

                    // Access confirmation states
                    var showConfirmAccessDialog by remember { mutableStateOf(false) }
                    var notifyBatchCheckbox by remember { mutableStateOf(true) }
                    var notifySubjectCheckbox by remember { mutableStateOf(true) }

                    // THE BEAUTIFUL, ULTRA-CLEAR LIVE CLASS CARD
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 520.dp)
                            .padding(bottom = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                        border = BorderStroke(2.dp, JalaramPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Badge & Title Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LiveTv,
                                        contentDescription = null,
                                        tint = JalaramPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Broadcaster Studio Panel",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = JalaramPrimary
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(JalaramPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "ADMIN ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = JalaramPrimary
                                    )
                                }
                            }

                            Text(
                                text = "Operate and broadcast synchronous lectures. Follow the clear configuration steps below to stream limitlessly.",
                                color = JalaramTextSub,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )

                            Divider(color = JalaramBorder.copy(alpha = 0.5f))

                            // STEP 1: Select Subject
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "1️⃣ ALLOT SUBJECT DIRECTORY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JalaramPrimary
                                )
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
                                            Text(selectedSubject, fontWeight = FontWeight.Bold)
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
                                                text = { Text(sub, fontWeight = FontWeight.SemiBold) },
                                                onClick = {
                                                    selectedSubject = sub
                                                    subjectDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // STEP 2: Select Target Class (Batch)
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "2️⃣ ALLOT TARGET CLASS (BATCH)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JalaramPrimary
                                )
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
                                            Text(targetBatchString, fontWeight = FontWeight.Bold)
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
                                                    text = { Text(batch.name, fontWeight = FontWeight.SemiBold) },
                                                    onClick = {
                                                        selectedBatch = batch.name
                                                        batchDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // STEP 3: Define Lecture Topic
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "3️⃣ DEFINE LECTURE TOPIC / TOPIC TITLE",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = JalaramPrimary
                                )
                                OutlinedTextField(
                                    value = topicText,
                                    onValueChange = { topicText = it },
                                    placeholder = { Text("e.g. Circular Motion, Wave Interference...") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // CLEAR BROADCAST BUTTON
                            Button(
                                onClick = {
                                    showConfirmAccessDialog = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.CastConnected, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Launch & Configure Access", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                            }
                        }
                    }

                    // ASK BATCH & STUDENTS WITH SUBJECTS ALLOTTED THE ACCESS DIALOG
                    if (showConfirmAccessDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmAccessDialog = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = JalaramPrimary,
                                    modifier = Modifier.size(36.dp)
                                )
                            },
                            title = {
                                Text(
                                    text = "Configure Broadcast Access",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = JalaramPrimary
                                )
                            },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "You are starting a live stream for $selectedSubject. Confirm which batches and students should be allotted immediate access and informed:",
                                        fontSize = 13.sp,
                                        color = JalaramTextMain
                                    )

                                    // Batch checkbox option
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = JalaramBgMain),
                                        border = BorderStroke(1.dp, JalaramBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { notifyBatchCheckbox = !notifyBatchCheckbox }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = notifyBatchCheckbox,
                                                onCheckedChange = { notifyBatchCheckbox = it }
                                            )
                                            Column {
                                                Text(
                                                    text = "Inform & Grant Access to Batch:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = JalaramTextMain
                                                )
                                                Text(
                                                    text = "All active students in class \"$targetBatchString\"",
                                                    fontSize = 11.sp,
                                                    color = JalaramPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }

                                    // Subject checkbox option
                                    Card(
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = JalaramBgMain),
                                        border = BorderStroke(1.dp, JalaramBorder)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { notifySubjectCheckbox = !notifySubjectCheckbox }
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = notifySubjectCheckbox,
                                                onCheckedChange = { notifySubjectCheckbox = it }
                                            )
                                            Column {
                                                Text(
                                                    text = "Grant Subject-Allotted Access:",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = JalaramTextMain
                                                )
                                                Text(
                                                    text = "All students registered with directory \"$selectedSubject\"",
                                                    fontSize = 11.sp,
                                                    color = JalaramPrimary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        val finalTopic = if (topicText.trim().isEmpty()) "Interactive Discussion" else topicText.trim()
                                        
                                        // Trigger live class creation
                                        viewModel.createLiveClass(
                                            subject = selectedSubject,
                                            batch = targetBatchString,
                                            topic = finalTopic,
                                            startedBy = currentUser?.name ?: "Academy Host"
                                        )
                                        
                                        // Log confirmation details
                                        var toastMsg = "Stream broadcast started!"
                                        if (notifyBatchCheckbox) toastMsg += "\nBatch $targetBatchString informed & granted access."
                                        if (notifySubjectCheckbox) toastMsg += "\nAllotted subject students granted access."
                                        
                                        topicText = ""
                                        showConfirmAccessDialog = false
                                        Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Grant Access & Broadcast", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showConfirmAccessDialog = false }) {
                                    Text("Back", color = JalaramTextSub, fontWeight = FontWeight.Bold)
                                }
                            },
                            containerColor = JalaramSurface
                        )
                    }
                }

                // ONGOING LIVE CLASSROOMS DIRECTORY LISTING
                val activeLiveClasses by viewModel.activeLiveClasses.collectAsState()

                if (currentUser?.role == "Student") {
                    var manualLink by remember { mutableStateOf("") }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 480.dp)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Join via Meeting Link",
                                fontWeight = FontWeight.Bold,
                                color = JalaramTextMain,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = manualLink,
                                onValueChange = { manualLink = it },
                                label = { Text("Enter zoom/jitsi link") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JalaramPrimary,
                                    unfocusedBorderColor = JalaramBorder
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val session = activeLiveClasses.find { it.jitsiLink == manualLink.trim() }
                                    if (session != null) {
                                        val studentSubjects = currentUser?.subjects?.split(",")?.map { it.trim() } ?: emptyList()
                                        val studentBatch = currentUser?.batch ?: ""
                                        if (session.subject in studentSubjects || session.batch == studentBatch) {
                                            viewModel.joinLiveClass(session)
                                            Toast.makeText(context, "Connecting to live classroom...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "Access Denied: Subject or Batch does not match your enrollment", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Invalid or Expired Link", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Text("Join")
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
                                        if (currentUser?.role == "Student") {
                                            val studentSubjects = currentUser?.subjects?.split(",")?.map { it.trim() } ?: emptyList()
                                            val studentBatch = currentUser?.batch ?: ""
                                            if (liveSession.subject in studentSubjects || liveSession.batch == studentBatch) {
                                                viewModel.joinLiveClass(liveSession)
                                                Toast.makeText(context, "Connecting to live classroom...", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Access Denied: Subject or Batch does not match your enrollment", Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            viewModel.joinLiveClass(liveSession)
                                            Toast.makeText(context, "Connecting to live classroom...", Toast.LENGTH_SHORT).show()
                                        }
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
    val currentUser by viewModel.currentUser.collectAsState()
    val currentUserName = currentUser?.name ?: "Student"

    // Zoom Class Controls and View States
    var micMuted by remember { mutableStateOf(false) }
    var videoDisabled by remember { mutableStateOf(false) }
    var isWhiteboardActive by remember { mutableStateOf(false) }
    var isScreenShareActive by remember { mutableStateOf(false) }
    var isHandRaised by remember { mutableStateOf(false) }
    var isGalleryView by remember { mutableStateOf(true) }

    // Whiteboard Drawing States
    val currentPoints = remember { mutableStateListOf<Offset>() }
    val drawnPaths = remember { mutableStateListOf<Pair<List<Offset>, Color>>() }
    var selectedColor by remember { mutableStateOf(Color(0xFF48C99E)) } // Default Teal accent

    // Chat Conversation States
    val currentSession by viewModel.currentLiveSession.collectAsState()
    val subjectName = currentSession?.subject ?: "Physics"
    val batchClass = currentSession?.batch ?: "Grade 10-A"
    val topicTitle = currentSession?.topic ?: "Interactive Session"
    val instructorName = currentSession?.startedBy ?: "Dr. Shalini Vyas"

    // Screen Share slide simulation content
    var currentSlideIndex by remember { mutableIntStateOf(0) }
    val slides = listOf(
        "SLIDE 1: Introduction to $subjectName\n\n- Definition and core principles of $topicTitle\n- Real-world applications and historical contexts\n- Interactive formula breakdown in the next section.",
        "SLIDE 2: Core Mechanical Formulations\n\n- Essential calculations and vector diagrams\n- Force (F) = mass (m) x acceleration (a)\n- Drag on the interactive whiteboard to solve sample problems.",
        "SLIDE 3: Laboratory Demonstrations & Exercises\n\n- Group assignments and collaborative discussions\n- Solve Section A MCQ Tests in the portal before leaving\n- Summary & Homework assignments."
    )

    val context = LocalContext.current

    // Live Class Leave Tracking Logic
    val lifecycleOwner = LocalLifecycleOwner.current
    var leaveCount by remember { mutableIntStateOf(0) }
    var showAlert by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (currentUserRole == "Student" && event == Lifecycle.Event.ON_PAUSE) {
                leaveCount++
                if (leaveCount >= 3) {
                    showAlert = true
                    viewModel.createNotification(
                        title = "🚨 DISCIPLINARY ALERT: Student Distracted",
                        content = "Student '$currentUserName' left the active live class '$subjectName' ($batchClass) more than 3 times. Instructors and Admins have been officially notified.",
                        type = "Danger"
                    )
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // SCARY GIANT RED WORDS FOCUS ALERT FOR STU-LEAVE
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            containerColor = Color(0xFF1E1010), // Extremely dark scary red container
            modifier = Modifier.padding(16.dp),
            title = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚠️ ALERT ⚠️", 
                        color = Color(0xFFEF4444), 
                        fontWeight = FontWeight.Black, 
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "FOCUS OR YOU WILL BE REMOVED",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        lineHeight = 34.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Text(
                        text = "You have left the academic streaming screen $leaveCount times. This distraction has been logged. The administrator and conducting instructor $instructorName have been dispatched a secure notification about your attendance status.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAlert = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("I UNDERSTAND AND WILL FOCUS NOW", color = Color.White, fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        )
    }

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
            .background(Color(0xFF0F172A)) // Sleek dark charcoal/blue Zoom theme
    ) {
        // TOP HEADER BAR: Zoom details & Limitless Status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$subjectName Class - $topicTitle",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Instructor: $instructorName • Batch: $batchClass",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            }

            Surface(
                color = JalaramPrimary.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, JalaramPrimary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "LIMITLESS SESSION",
                        color = JalaramPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // VIEW STAGE: Large Presenter / Whiteboard / Slides OR Gallery Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.3f)
                .padding(12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF020617)), // Ink black
            contentAlignment = Alignment.Center
        ) {
            if (isWhiteboardActive) {
                // HOMEGROWN INTERACTIVE CHALKBOARD/WHITEBOARD
                Column(modifier = Modifier.fillMaxSize()) {
                    // Whiteboard Control Toolbar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1E293B))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "✍️ LIVE CLASSROOM WHITEBOARD",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 6.dp)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Color Selector swatches
                            listOf(
                                Color(0xFF48C99E), // Jalaram primary green
                                Color(0xFFE57373), // Red
                                Color(0xFFFFB74D), // Warning amber
                                Color(0xFF60A5FA), // Blue
                                Color.White
                            ).forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (selectedColor == color) 2.dp else 0.dp,
                                            color = Color.White,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColor = color }
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Reset button
                            Button(
                                onClick = {
                                    drawnPaths.clear()
                                    currentPoints.clear()
                                    Toast.makeText(context, "Whiteboard cleared!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Interactive drawing Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF1E293B).copy(alpha = 0.4f))
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints.add(offset)
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        currentPoints.add(change.position)
                                    },
                                    onDragEnd = {
                                        drawnPaths.add(currentPoints.toList() to selectedColor)
                                        currentPoints.clear()
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw Gridlines to look like a premium academic whiteboard
                            val spacing = 40f
                            for (x in 0..size.width.toInt() step spacing.toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.04f),
                                    start = Offset(x.toFloat(), 0f),
                                    end = Offset(x.toFloat(), size.height),
                                    strokeWidth = 1f
                                )
                            }
                            for (y in 0..size.height.toInt() step spacing.toInt()) {
                                drawLine(
                                    color = Color.White.copy(alpha = 0.04f),
                                    start = Offset(0f, y.toFloat()),
                                    end = Offset(size.width, y.toFloat()),
                                    strokeWidth = 1f
                                )
                            }

                            // Draw completed paths
                            drawnPaths.forEach { (pathPoints, color) ->
                                for (i in 0 until pathPoints.size - 1) {
                                    drawLine(
                                        color = color,
                                        start = pathPoints[i],
                                        end = pathPoints[i + 1],
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }

                            // Draw active drawing path
                            if (currentPoints.size > 1) {
                                for (i in 0 until currentPoints.size - 1) {
                                    drawLine(
                                        color = selectedColor,
                                        start = currentPoints[i],
                                        end = currentPoints[i + 1],
                                        strokeWidth = 6f,
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                        }

                        if (drawnPaths.isEmpty() && currentPoints.isEmpty()) {
                            Text(
                                text = "✍️ Use your finger or mouse to draw directly on this screen share board...",
                                color = Color.White.copy(alpha = 0.35f),
                                fontSize = 12.sp,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                }
            } else if (isScreenShareActive) {
                // SCREEN SHARE SIMULATOR
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🖥️ PRESENTING SCREEN: Dr. Shalini Vyas (Host)",
                            color = JalaramPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )

                        Surface(
                            color = Color.White.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SLIDE ${currentSlideIndex + 1} OF ${slides.size}",
                                color = Color.White,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Slide deck display
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = slides[currentSlideIndex],
                                color = Color.White,
                                fontSize = 14.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                modifier = Modifier.align(Alignment.TopStart),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    // Interactive controls for slide deck
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                currentSlideIndex = (currentSlideIndex - 1).coerceAtLeast(0)
                            },
                            enabled = currentSlideIndex > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Prev Slide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                currentSlideIndex = (currentSlideIndex + 1).coerceAtMost(slides.size - 1)
                            },
                            enabled = currentSlideIndex < slides.size - 1,
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Next Slide", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (!isGalleryView) {
                // SPEAKER VIEW: Big display of Teacher, thumbs of other students
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val midY = size.height / 2f
                        val amplitude = 50f
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(JalaramPrimary.copy(alpha = 0.25f), Color.Transparent),
                                center = Offset(size.width / 2f, size.height / 2f)
                            ),
                            radius = size.width / 1.8f
                        )
                        for (i in 0..4) {
                            val pathPoints = mutableListOf<Offset>()
                            for (x in 0..size.width.toInt() step 6) {
                                val y = midY + amplitude * sin((x * 0.006f) + tick + (i * 0.4f)) + (i * 40f) - 80f
                                pathPoints.add(Offset(x.toFloat(), y))
                            }
                            for (j in 0 until pathPoints.size - 1) {
                                drawLine(
                                    color = JalaramPrimary.copy(alpha = 0.35f - (i * 0.06f)),
                                    start = pathPoints[j],
                                    end = pathPoints[j + 1],
                                    strokeWidth = 4f
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📽️ Instructor: $instructorName (Active Broadcaster)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            } else {
                // ZOOM GALLERY VIEW: 2x2 Grid of Video Frames
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card 1: Instructor (Host)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(BorderStroke(2.dp, JalaramPrimary), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                // Animated teacher broadcast stream visual
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(JalaramPrimary.copy(alpha = 0.15f), Color.Transparent),
                                            center = Offset(size.width / 2f, size.height / 2f)
                                        ),
                                        radius = size.width / 1.5f
                                    )
                                    val midY = size.height / 2f
                                    val pathPoints = mutableListOf<Offset>()
                                    for (x in 0..size.width.toInt() step 10) {
                                        val y = midY + 15f * sin((x * 0.02f) + tick)
                                        pathPoints.add(Offset(x.toFloat(), y))
                                    }
                                    for (j in 0 until pathPoints.size - 1) {
                                        drawLine(
                                            color = JalaramPrimary.copy(alpha = 0.6f),
                                            start = pathPoints[j],
                                            end = pathPoints[j + 1],
                                            strokeWidth = 3f
                                        )
                                    }
                                }

                                Text(
                                    text = "👩‍🏫",
                                    fontSize = 36.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Dr. Shalini (Faculty) • Speaking",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Card 2: Suresh Kumar
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "👨‍🎓",
                                    fontSize = 32.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Suresh Kumar (Student)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card 3: Aditi Sharma
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "👩‍🎓",
                                    fontSize = 32.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "Aditi Sharma (Student)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Card 4: YOU (Current User)
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .border(
                                    width = if (isHandRaised) 2.dp else 0.dp,
                                    color = Color(0xFFFBBF24),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (!videoDisabled) {
                                    // Animated self video feed preview
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        drawCircle(
                                            brush = Brush.radialGradient(
                                                colors = listOf(Color(0xFF60A5FA).copy(alpha = 0.12f), Color.Transparent),
                                                center = Offset(size.width / 2f, size.height / 2f)
                                            ),
                                            radius = size.width / 1.5f
                                        )
                                        val midY = size.height / 2f
                                        val pathPoints = mutableListOf<Offset>()
                                        for (x in 0..size.width.toInt() step 12) {
                                            val y = midY + 8f * sin((x * 0.03f) - tick)
                                            pathPoints.add(Offset(x.toFloat(), y))
                                        }
                                        for (j in 0 until pathPoints.size - 1) {
                                            drawLine(
                                                color = Color(0xFF60A5FA).copy(alpha = 0.5f),
                                                start = pathPoints[j],
                                                end = pathPoints[j + 1],
                                                strokeWidth = 2.5f
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (videoDisabled) "🔇" else "⭐",
                                        fontSize = 32.sp
                                    )
                                    if (videoDisabled) {
                                        Text(
                                            text = "Camera Blocked",
                                            color = Color.White.copy(alpha = 0.5f),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (isHandRaised) {
                                    Surface(
                                        color = Color(0xFFFFB74D),
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "✋ HAND RAISED",
                                            color = Color.Black,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 8.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.Black.copy(alpha = 0.6f),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "$currentUserName (You)",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // LOWER SECTION: Participant head thumbs (when in active whiteboard or screenshare)
        if (isWhiteboardActive || isScreenShareActive || !isGalleryView) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Thumbs
                listOf(
                    Pair("Dr. Shalini (Host)", "👩‍🏫"),
                    Pair("Suresh Kumar", "👨‍🎓"),
                    Pair("Aditi Sharma", "👩‍🎓"),
                    Pair("$currentUserName (You)", if (videoDisabled) "🔇" else "⭐")
                ).forEach { (name, icon) ->
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                        modifier = Modifier.width(130.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = icon, fontSize = 16.sp)
                            Column {
                                Text(
                                    text = name,
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "Connected",
                                    color = Color.White.copy(alpha = 0.5f),
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // LIVE CONTROL BAR PANEL (Sleek slate dark control bar)
        Surface(
            color = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mic Control
                IconButton(
                    onClick = { micMuted = !micMuted },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (micMuted) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (micMuted) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Toggle Microphone",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Camera Video Control
                IconButton(
                    onClick = { videoDisabled = !videoDisabled },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (videoDisabled) Color(0xFFEF4444) else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (videoDisabled) Icons.Default.VideocamOff else Icons.Default.Videocam,
                        contentDescription = "Toggle Video",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Native Draw Whiteboard Control
                IconButton(
                    onClick = {
                        isWhiteboardActive = !isWhiteboardActive
                        if (isWhiteboardActive) {
                            isScreenShareActive = false
                            isGalleryView = false
                        } else {
                            isGalleryView = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isWhiteboardActive) JalaramPrimary else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Brush,
                        contentDescription = "Toggle Whiteboard",
                        tint = if (isWhiteboardActive) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Screen Share Control
                IconButton(
                    onClick = {
                        isScreenShareActive = !isScreenShareActive
                        if (isScreenShareActive) {
                            isWhiteboardActive = false
                            isGalleryView = false
                        } else {
                            isGalleryView = true
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isScreenShareActive) JalaramPrimary else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PresentToAll,
                        contentDescription = "Toggle Screen Share",
                        tint = if (isScreenShareActive) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Raise Hand Control
                IconButton(
                    onClick = {
                        isHandRaised = !isHandRaised
                        if (isHandRaised) {
                            Toast.makeText(context, "✋ Raised your hand. Instructor has been notified!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isHandRaised) Color(0xFFFFB74D) else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (isHandRaised) Icons.Default.BackHand else Icons.Default.PanTool,
                        contentDescription = "Raise Hand",
                        tint = if (isHandRaised) Color.Black else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // View Mode Grid/Speaker Switcher
                IconButton(
                    onClick = {
                        isGalleryView = !isGalleryView
                        isWhiteboardActive = false
                        isScreenShareActive = false
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isGalleryView) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.1f))
                ) {
                    Icon(
                        imageVector = if (isGalleryView) Icons.Default.GridView else Icons.Default.AccountBox,
                        contentDescription = "Switch View Mode",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Leave Meeting/End Session Button
                Button(
                    onClick = onEnd,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.height(38.dp)
                ) {
                    Icon(imageVector = Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Leave", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
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
