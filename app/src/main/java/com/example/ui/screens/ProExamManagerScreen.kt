package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProExam
import com.example.data.ProExamDraft
import com.example.data.ProExamStatus
import com.example.data.getStatus
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProExamManagerScreen(viewModel: ErpViewModel) {
    val exams by viewModel.proExams.collectAsState()
    val drafts by viewModel.examDrafts.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Published", "Local Drafts")

    var showCreateDraftDialog by remember { mutableStateOf(false) }
    var draftToSchedule by remember { mutableStateOf<ProExamDraft?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProExams()
        viewModel.loadDrafts()
    }

    Column(modifier = Modifier.fillMaxSize().background(JalaramBgMain)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = JalaramPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = JalaramPrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (selectedTab == 0) {
                PublishedExamsList(exams)
            } else {
                DraftsList(
                    drafts = drafts,
                    onDelete = { viewModel.deleteDraft(it) },
                    onSchedule = { draftToSchedule = it }
                )
            }

            FloatingActionButton(
                onClick = { showCreateDraftDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
                containerColor = JalaramPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Draft")
            }
        }
    }

    if (showCreateDraftDialog) {
        CreateDraftDialog(
            onDismiss = { showCreateDraftDialog = false },
            onSaveDraft = { title, subject, questions ->
                viewModel.saveExamAsDraft(title, subject, questions)
                showCreateDraftDialog = false
            }
        )
    }

    if (draftToSchedule != null) {
        ScheduleExamDialog(
            draft = draftToSchedule!!,
            onDismiss = { draftToSchedule = null },
            onConfirm = { startTime, duration, batches ->
                viewModel.publishDraft(draftToSchedule!!.draftId, startTime, duration, batches)
                draftToSchedule = null
            }
        )
    }
}

@Composable
fun PublishedExamsList(exams: List<ProExam>) {
    if (exams.isEmpty()) {
        EmptyState("No published exams found")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(exams) { exam ->
                ManagerExamCard(exam)
            }
        }
    }
}

@Composable
fun DraftsList(
    drafts: List<ProExamDraft>,
    onDelete: (String) -> Unit,
    onSchedule: (ProExamDraft) -> Unit
) {
    if (drafts.isEmpty()) {
        EmptyState("No local drafts saved.\nTeachers can create tests for future use here.")
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(drafts) { draft ->
                DraftCard(draft, onDelete, onSchedule)
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
            Spacer(Modifier.height(16.dp))
            Text(message, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.padding(horizontal = 32.dp))
        }
    }
}

@Composable
fun ManagerExamCard(exam: ProExam) {
    val status = exam.getStatus()
    val statusColor = when (status) {
        ProExamStatus.Scheduled -> JalaramPrimary
        ProExamStatus.Live -> Color.Red
        ProExamStatus.Completed -> Color.Gray
        else -> Color.Black
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(statusColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val icon = when (status) {
                            ProExamStatus.Scheduled -> Icons.Default.Schedule
                            ProExamStatus.Live -> Icons.Default.FiberManualRecord
                            else -> Icons.Default.CheckCircle
                        }
                        Icon(icon, null, tint = statusColor, modifier = if (status == ProExamStatus.Live) Modifier.size(16.dp) else Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(exam.subject, color = JalaramPrimary, fontSize = 12.sp)
                    }
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status.name.uppercase(),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Marks: ${exam.totalMarks}", fontSize = 12.sp, color = Color.Gray)
                Text("Batches: ${exam.assignedBatches}", fontSize = 12.sp, color = Color.Gray)
            }
            
            if (status != ProExamStatus.Scheduled) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Administrative edits disabled for $status exams", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun DraftCard(draft: ProExamDraft, onDelete: (String) -> Unit, onSchedule: (ProExamDraft) -> Unit) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.EditNote, null, tint = Color.Gray)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(draft.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(draft.subject, color = JalaramPrimary, fontSize = 12.sp)
                    }
                }
                IconButton(onClick = { onSchedule(draft) }) {
                    Icon(Icons.Default.RocketLaunch, null, tint = JalaramPrimary)
                }
                IconButton(onClick = { onDelete(draft.draftId) }) {
                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Last modified: ${sdf.format(Date(draft.lastModified))}", fontSize = 11.sp, color = Color.Gray)
                Text("Local Draft", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = JalaramWarning)
            }
        }
    }
}

@Composable
fun ScheduleExamDialog(
    draft: ProExamDraft,
    onDismiss: () -> Unit,
    onConfirm: (Long, Int, String) -> Unit
) {
    var dateText by remember { mutableStateOf("") }
    var timeText by remember { mutableStateOf("") } // e.g. 10:30
    var duration by remember { mutableStateOf("60") }
    var batches by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule: ${draft.title}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Set the launch window and target students.", fontSize = 12.sp, color = Color.Gray)
                
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (e.g. 2024-06-25)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) }
                )

                OutlinedTextField(
                    value = timeText,
                    onValueChange = { timeText = it },
                    label = { Text("Start Time (e.g. 10:00)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Schedule, null) }
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = batches,
                    onValueChange = { batches = it },
                    label = { Text("Target Batches (e.g. Grade 10-A, JEE)") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Comma separated") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val date = sdf.parse("$dateText $timeText")
                        if (date != null) {
                            onConfirm(date.time, duration.toIntOrNull() ?: 60, batches)
                        }
                    } catch (e: Exception) {
                        // Error handling
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Schedule & Publish")
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
fun CreateDraftDialog(
    onDismiss: () -> Unit,
    onSaveDraft: (String, String, List<Pair<String, List<Pair<String, Boolean>>>>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    
    // Mock questions for the draft (teacher can add more later)
    val questions = listOf(
        "Question 1" to listOf("Option A" to true, "Option B" to false),
        "Question 2" to listOf("Option X" to false, "Option Y" to true)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Local Test Draft") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("This test will be saved locally on your device. You can publish it to students later.", fontSize = 12.sp, color = Color.Gray)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Test Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && subject.isNotBlank()) onSaveDraft(title, subject, questions) },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Save to Local Drafts")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
