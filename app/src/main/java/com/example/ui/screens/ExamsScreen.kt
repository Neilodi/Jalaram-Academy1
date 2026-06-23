package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Exam
import com.example.data.Question
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import androidx.compose.ui.draw.scale

@Composable
fun ExamsScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val examsList by viewModel.examsList.collectAsState()
    val activeExam by viewModel.activeExam.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val examSchedulesList by viewModel.examSchedulesList.collectAsState()

    var showCreateExamDialog by remember { mutableStateOf(false) }
    var showCreateScheduleDialog by remember { mutableStateOf(false) }

    var activeSubTab by remember { mutableStateOf(0) } // 0 = MCQ Tests Portal, 1 = Exam Timetable & Calendar
    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }
    var showOnlyMyBatch by remember { mutableStateOf(currentUser?.role == "Student") }

    AnimatedContent(
        targetState = activeExam,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "exam_portal_root"
    ) { currentActiveExam ->
        if (currentActiveExam != null) {
            // Live Examination Session Active Pane
            LiveExamSessionView(
                viewModel = viewModel,
                exam = currentActiveExam
            )
        } else {
            // Standard Exams Directory View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Examinations Portal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = JalaramTextMain
                            )
                        )
                        Text(
                            text = "Assigned board tests, entrance examinations & schedules.",
                            color = JalaramTextSub,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (currentUser?.role != "Student") {
                        if (activeSubTab == 0) {
                            Button(
                                onClick = { showCreateExamDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Schedule Exam")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create MCQ", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { showCreateScheduleDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Schedule Subject Exam")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Schedule Exam", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Sub tabs: MCQ Tests vs Schedule & Calendar
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = Color.Transparent,
                    contentColor = JalaramPrimary,
                    divider = { HorizontalDivider(color = JalaramBorder) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = { Text("MCQ Tests Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = { Text("Timetables & Calendars", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }

                if (activeSubTab == 0) {
                    if (examsList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentLate,
                                    contentDescription = null,
                                    tint = JalaramTextSub.copy(alpha = 0.4f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("No assessments currently scheduled", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(examsList) { exam ->
                                ExamCard(
                                    exam = exam,
                                    onStart = {
                                        viewModel.startExam(exam)
                                    },
                                    onDelete = { viewModel.deleteExam(exam.id) },
                                    currentUserRole = currentUser?.role ?: ""
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: Exam Timetable & Interactive Scheduling Dashboard
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Interactive Calendar
                        June2026Calendar(
                            schedules = examSchedulesList,
                            selectedDate = selectedCalendarDate,
                            onDateSelect = { selectedCalendarDate = it }
                        )

                        // Filters & Info Header block
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (selectedCalendarDate != null) "Schedules for $selectedCalendarDate" else "All Subject Exam Timetables",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = JalaramTextMain
                                )
                                val userLocal = currentUser
                                if (userLocal?.role == "Student" && userLocal.batch != null) {
                                    Text(
                                        text = "Your Batch: ${userLocal.batch}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = JalaramPrimaryDark
                                    )
                                }
                            }

                            // Show Batch Filter Switch
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "My Batch Only",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = JalaramTextSub
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = showOnlyMyBatch,
                                    onCheckedChange = { showOnlyMyBatch = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JalaramPrimary,
                                        checkedTrackColor = JalaramPrimaryLight
                                    ),
                                    modifier = Modifier.scale(0.8f)
                                )
                            }
                        }

                        // Filter the list
                        val studentBatch = currentUser?.batch
                        val filteredSchedules = remember(examSchedulesList, showOnlyMyBatch, studentBatch) {
                            if (showOnlyMyBatch && studentBatch != null) {
                                examSchedulesList.filter { it.batch.lowercase() == studentBatch.lowercase() }
                            } else {
                                examSchedulesList
                            }
                        }

                        val displayedSchedules = remember(filteredSchedules, selectedCalendarDate) {
                            if (selectedCalendarDate != null) {
                                filteredSchedules.filter { it.examDate == selectedCalendarDate }
                            } else {
                                filteredSchedules
                            }
                        }

                        if (displayedSchedules.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, JalaramBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventBusy,
                                        contentDescription = null,
                                        tint = JalaramTextSub.copy(alpha = 0.5f),
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No exams scheduled for the selected filter.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = JalaramTextSub,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            displayedSchedules.forEach { schedule ->
                                ExamScheduleCard(
                                    schedule = schedule,
                                    onToggleAlert = {
                                        viewModel.toggleScheduleAlert(schedule.id, schedule.isAlertEnabled)
                                        val newState = !schedule.isAlertEnabled
                                        if (newState) {
                                            Toast.makeText(
                                                context,
                                                "📅 Automated Notification Scheduled! Alert set for ${schedule.subject} on ${schedule.examDate}.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                "Alert removed from local calendar.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    onDelete = if (currentUser?.role != "Student") {
                                        { viewModel.deleteSchedule(schedule.id) }
                                    } else null,
                                    userRole = currentUser?.role ?: ""
                                )
                            }
                        }

                        // Render Reminders Feed/Summary
                        val alertEnabledSchedules = examSchedulesList.filter { it.isAlertEnabled }
                        if (alertEnabledSchedules.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = JalaramSuccessContainer.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, JalaramSuccess.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = JalaramSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "My Automated Calendar Alerts (${alertEnabledSchedules.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = JalaramSuccess
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "You will receive push reminders 24h & 1h prior to these schedules:",
                                        fontSize = 11.sp,
                                        color = JalaramTextMain,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    alertEnabledSchedules.forEach { s ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "• ${s.subject} Board (${s.batch})",
                                                fontSize = 11.sp,
                                                color = JalaramTextMain
                                            )
                                            Text(
                                                text = "${s.examDate} @ ${s.timeSlot.split(" - ").firstOrNull() ?: ""}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JalaramSuccess
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateExamDialog) {
        CreateExamDialog(
            viewModel = viewModel,
            onDismiss = { showCreateExamDialog = false },
            onSubmit = { title, batch, duration, questions ->
                viewModel.createExam(title, batch, duration, questions)
                showCreateExamDialog = false
                Toast.makeText(context, "New academic MCQ assessment posted!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showCreateScheduleDialog) {
        CreateScheduleDialog(
            viewModel = viewModel,
            onDismiss = { showCreateScheduleDialog = false },
            onSubmit = { subject, batch, examDate, timeSlot, examinerName, location, duration ->
                viewModel.scheduleExamSubject(subject, batch, examDate, timeSlot, examinerName, location, duration)
                showCreateScheduleDialog = false
                Toast.makeText(context, "Successfully scheduled $subject Board Exam & sent automated alerts!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun ExamCard(
    exam: Exam,
    onStart: () -> Unit,
    onDelete: () -> Unit,
    currentUserRole: String
) {
    val questionsCount = remember(exam.questionsJson) {
        Question.deserializeList(exam.questionsJson).size
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JalaramPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assignment,
                        contentDescription = null,
                        tint = JalaramPrimary
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = exam.title,
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 15.sp
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(color = JalaramPrimaryLight, shape = RoundedCornerShape(6.dp)) {
                            Text(
                                text = exam.batch,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = JalaramPrimary),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "$questionsCount Qs • ${exam.durationMinutes} mins",
                            color = JalaramTextSub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row {
                Button(
                    onClick = onStart,
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Start Exam", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                if (currentUserRole != "Student") {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Exam", tint = JalaramDanger)
                    }
                }
            }
        }
    }
}

@Composable
fun LiveExamSessionView(
    viewModel: ErpViewModel,
    exam: Exam
) {
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val examTimeRemaining by viewModel.examTimeRemaining.collectAsState()

    val questions = remember(exam.questionsJson) {
        Question.deserializeList(exam.questionsJson)
    }

    val currentQuestion = remember(questions, currentQuestionIndex) {
        if (questions.isNotEmpty() && currentQuestionIndex < questions.size) {
            questions[currentQuestionIndex]
        } else null
    }

    // Parse remaining duration as mm:ss
    val formattedTime = remember(examTimeRemaining) {
        val minutes = examTimeRemaining / 60
        val seconds = examTimeRemaining % 60
        String.format("%02d:%02d", minutes, seconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JalaramBgMain)
    ) {
        // High visibility Top Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Live Assessment Mode",
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Do not close app or return",
                            color = JalaramTextSub,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontWeight = FontWeight.Black,
                        color = if (examTimeRemaining < 60) JalaramDanger else JalaramPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp
                    )

                    Button(
                        onClick = { viewModel.submitExam() },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramSuccess),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Finish", fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // Assessment sheet body
        currentQuestion?.let { q ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                        fontWeight = FontWeight.Bold,
                        color = JalaramPrimary,
                        fontSize = 15.sp
                    )

                    Surface(
                        color = if (selectedAnswers.containsKey(currentQuestionIndex)) JalaramSuccessContainer else JalaramWarningContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (selectedAnswers.containsKey(currentQuestionIndex)) "Answer Saved" else "Unanswered",
                            color = if (selectedAnswers.containsKey(currentQuestionIndex)) JalaramSuccess else JalaramWarning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JalaramSurface)
                ) {
                    Text(
                        text = q.questionText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = JalaramTextMain,
                            lineHeight = 22.sp
                        ),
                        modifier = Modifier.padding(20.dp)
                    )
                }

                // Options list list cards
                q.options.forEachIndexed { optIndex, optionText ->
                    val isSelected = selectedAnswers[currentQuestionIndex] == optIndex
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectOptionForCurrentQuestion(optIndex) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) JalaramPrimaryLight else JalaramSurface
                        ),
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) JalaramPrimary else JalaramBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) JalaramPrimary else JalaramBgMain),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = ('A'.code + optIndex).toChar().toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else JalaramTextSub,
                                    fontSize = 12.sp
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optionText,
                                fontWeight = FontWeight.SemiBold,
                                color = JalaramTextMain,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Navigation Footer bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 2.dp,
            color = Color.White
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.prevQuestion() },
                    enabled = currentQuestionIndex > 0,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Previous")
                }

                OutlinedButton(
                    onClick = { viewModel.nextQuestion() },
                    enabled = currentQuestionIndex < questions.size - 1,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Next Question")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExamDialog(
    viewModel: ErpViewModel,
    onDismiss: () -> Unit,
    onSubmit: (String, String, Int, List<Question>) -> Unit
) {
    val batchesList by viewModel.batchesList.collectAsState()

    var title by remember { mutableStateOf("") }
    var durationStr by remember { mutableStateOf("30") }

    var selectedBatch by remember { mutableStateOf("JEE Mains") }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    // Hardcode a default list of mock questions to avoid typing tedious MCQs in simulator
    val preCalculatedQuestions = listOf(
        Question(1, "Which of the following describes the first law of thermodynamics?", listOf("Conservation of energy", "Entropy constant", "Absolute zero temperature", "Relativity"), 0),
        Question(2, "What occurs during oxidation?", listOf("Gain of electrons", "Gain of neutrons", "Loss of protons", "Loss of electrons"), 3),
        Question(3, "Calculate limit as x approaches 0 of sin(x)/x.", listOf("0", "1", "Infinity", "Undefined"), 1)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Academic Assessment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Exam Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) durationStr = it },
                        label = { Text("Duration (mins)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.weight(1f)
                    )

                    ExposedDropdownMenuBox(
                        expanded = batchDropdownExpanded,
                        onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded },
                        modifier = Modifier.weight(1.2f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedBatch,
                            onValueChange = {},
                            label = { Text("Target Batch") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
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

                Card(colors = CardDefaults.cardColors(containerColor = JalaramPrimaryLight)) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = JalaramPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "To keep rapid simulator testing fast, this scheduled assessment auto-adds 3 preloaded science and calculus questions.",
                            fontSize = 11.sp,
                            color = JalaramPrimary,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d = durationStr.toIntOrNull() ?: 30
                    if (title.isNotBlank()) onSubmit(title, selectedBatch, d, preCalculatedQuestions)
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Schedule Exam")
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
fun June2026Calendar(
    schedules: List<com.example.data.ExamSchedule>,
    selectedDate: String?,
    onDateSelect: (String?) -> Unit
) {
    val daysInMonth = 30
    val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "June 2026",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = JalaramTextMain
                    )
                }

                if (selectedDate != null) {
                    TextButton(
                        onClick = { onDateSelect(null) },
                        colors = ButtonDefaults.textButtonColors(contentColor = JalaramPrimary)
                    ) {
                        Text("Show All", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text(
                        text = "Select date to filter",
                        fontSize = 11.sp,
                        color = JalaramTextSub,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekdays Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = JalaramTextSub
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Days Grid (June 1, 2026 is Monday)
            val rowsCount = (daysInMonth + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (rowIndex in 0 until rowsCount) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (colIndex in 0 until 7) {
                            val dayNum = rowIndex * 7 + colIndex + 1
                            if (dayNum <= daysInMonth) {
                                val dateStr = String.format("2026-06-%02d", dayNum)
                                val isSelected = selectedDate == dateStr
                                val daySchedules = schedules.filter { it.examDate == dateStr }
                                val hasExam = daySchedules.isNotEmpty()

                                Box(
                                    modifier = Modifier
                                        .size(height = 42.dp, width = 36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                isSelected -> JalaramPrimary
                                                hasExam -> JalaramPrimaryLight.copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .clickable {
                                            if (isSelected) onDateSelect(null) else onDateSelect(dateStr)
                                        }
                                        .testTag("calendar_day_$dayNum"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = dayNum.toString(),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = when {
                                                isSelected -> Color.White
                                                hasExam -> JalaramPrimaryDark
                                                else -> JalaramTextMain
                                            }
                                        )
                                        if (hasExam && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(JalaramPrimary)
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.width(36.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExamScheduleCard(
    schedule: com.example.data.ExamSchedule,
    onToggleAlert: () -> Unit,
    onDelete: (() -> Unit)?,
    userRole: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("schedule_card_${schedule.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JalaramPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (schedule.subject.lowercase()) {
                                "physics" -> Icons.Default.Science
                                "maths", "mathematics" -> Icons.Default.Calculate
                                "chemistry" -> Icons.Default.Biotech
                                "biology" -> Icons.Default.Eco
                                else -> Icons.Default.MenuBook
                            },
                            contentDescription = null,
                            tint = JalaramPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "${schedule.subject} Board Exam",
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain,
                            fontSize = 15.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                color = JalaramPrimaryLight,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = schedule.batch,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JalaramPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = "${schedule.durationMinutes} mins",
                                fontSize = 11.sp,
                                color = JalaramTextSub,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (onDelete != null) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Schedule",
                            tint = JalaramDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = JalaramBorder
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = JalaramPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Date: ${schedule.examDate}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = JalaramTextMain
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = JalaramPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Time: ${schedule.timeSlot}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = JalaramTextMain
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = JalaramTextSub,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Room: ${schedule.location}",
                            fontSize = 12.sp,
                            color = JalaramTextSub
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = JalaramTextSub,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Invigilator: ${schedule.examinerName}",
                            fontSize = 11.sp,
                            color = JalaramTextSub,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Button(
                    onClick = onToggleAlert,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (schedule.isAlertEnabled) JalaramSuccess else JalaramPrimaryLight
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (schedule.isAlertEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = if (schedule.isAlertEnabled) Color.White else JalaramPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (schedule.isAlertEnabled) "Alert Active" else "Add Reminder",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (schedule.isAlertEnabled) Color.White else JalaramPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateScheduleDialog(
    viewModel: ErpViewModel,
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, String, String, String, Int) -> Unit
) {
    val batchesList by viewModel.batchesList.collectAsState()

    var subject by remember { mutableStateOf("Physics") }
    val subjectsList = listOf("Physics", "Maths", "Chemistry", "Biology", "English")
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    var selectedBatch by remember { mutableStateOf(batchesList.firstOrNull()?.name ?: "Grade 10-A") }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    var examDate by remember { mutableStateOf("2026-06-25") }
    var timeSlot by remember { mutableStateOf("09:00 AM - 12:00 PM") }
    val timeSlotsList = listOf("09:00 AM - 12:00 PM", "02:00 PM - 05:00 PM", "10:00 AM - 01:00 PM")
    var timeSlotDropdownExpanded by remember { mutableStateOf(false) }

    var examinerName by remember { mutableStateOf("Dr. Shalini Vyas") }
    var location by remember { mutableStateOf("Main Examination Hall (A)") }
    var durationStr by remember { mutableStateOf("180") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Subject Examination") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = subjectDropdownExpanded,
                    onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = subject,
                        onValueChange = {},
                        label = { Text("Subject Name") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false },
                        modifier = Modifier.background(JalaramSurface)
                    ) {
                        subjectsList.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    subject = sub
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = batchDropdownExpanded,
                    onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedBatch,
                        onValueChange = {},
                        label = { Text("Target Batch Class") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
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

                OutlinedTextField(
                    value = examDate,
                    onValueChange = { examDate = it },
                    label = { Text("Exam Date (YYYY-MM-DD)") },
                    placeholder = { Text("e.g. 2026-06-25") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = timeSlotDropdownExpanded,
                    onExpandedChange = { timeSlotDropdownExpanded = !timeSlotDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = timeSlot,
                        onValueChange = {},
                        label = { Text("Time Slot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = timeSlotDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = timeSlotDropdownExpanded,
                        onDismissRequest = { timeSlotDropdownExpanded = false },
                        modifier = Modifier.background(JalaramSurface)
                    ) {
                        timeSlotsList.forEach { slot ->
                            DropdownMenuItem(
                                text = { Text(slot) },
                                onClick = {
                                    timeSlot = slot
                                    timeSlotDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = examinerName,
                    onValueChange = { examinerName = it },
                    label = { Text("Invigilator / Examiner") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Exam Room / Venue") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) durationStr = it },
                    label = { Text("Duration (minutes)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationStr.toIntOrNull() ?: 180
                    if (examDate.isNotBlank()) {
                        onSubmit(subject, selectedBatch, examDate, timeSlot, examinerName, location, duration)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Schedule Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
