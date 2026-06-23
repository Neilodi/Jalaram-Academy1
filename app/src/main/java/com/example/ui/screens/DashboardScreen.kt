package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SystemNotification
import com.example.data.User
import com.example.data.Exam
import androidx.compose.foundation.clickable
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun DashboardScreen(viewModel: ErpViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val coursesList by viewModel.coursesList.collectAsState()
    val notificationsList by viewModel.notificationsList.collectAsState()
    val examsList by viewModel.examsList.collectAsState()
    val resultsList by viewModel.resultsList.collectAsState()

    val pendingUsers = remember(usersList) { usersList.filter { it.status == "Pending" } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Core Banner Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, Color(0xFFBBC9B8)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFD7E8CD))
            ) {
                Box(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "STUDENT PORTAL",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F493E),
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "👋 Welcome, ${currentUser?.name ?: "Visitor"}!",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C18),
                                    fontSize = 22.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Academic Year 2026 • Grade 10-A • ${currentUser?.role ?: "Student"}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF444941),
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }

                        // Attendance Display
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFB5CCAF))
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (currentUser?.role == "Student") "87%" else "95%",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1C18),
                                    fontSize = 18.sp
                                )
                                Text(
                                    text = "ATTEND",
                                    fontSize = 9.sp,
                                    color = Color(0xFF3F493E),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Roles Dashboard Statistics Grid
        item {
            Text(
                text = "Academy Operational Stats",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = JalaramTextMain
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentUser?.role == "Admin") {
                    StatCard(
                        title = "Registered Users",
                        countValue = "${usersList.size}",
                        icon = Icons.Default.Group,
                        accentColor = JalaramPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Pending approvals",
                        countValue = "${pendingUsers.size}",
                        icon = Icons.Default.HourglassEmpty,
                        accentColor = JalaramWarning,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Active Classes",
                        countValue = "${coursesList.size}",
                        icon = Icons.Default.CastForEducation,
                        accentColor = JalaramSuccess,
                        modifier = Modifier.weight(1f)
                    )
                } else if (currentUser?.role == "Teacher") {
                    StatCard(
                        title = "My Subjects",
                        countValue = "${currentUser?.subjects?.split(",")?.size ?: 1}",
                        icon = Icons.Default.CastForEducation,
                        accentColor = JalaramPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Global Catalog",
                        countValue = "${coursesList.size}",
                        icon = Icons.Default.VideoLibrary,
                        accentColor = JalaramSuccess,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Student metrics
                    StatCard(
                        title = "My Courses",
                        countValue = "${coursesList.size}",
                        icon = Icons.Default.MenuBook,
                        accentColor = JalaramPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Academy Batches",
                        countValue = "4",
                        icon = Icons.Default.Layers,
                        accentColor = JalaramAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (currentUser?.role == "Student") {
            item {
                StudentWidgetsModule(
                    currentUser = currentUser!!,
                    examsList = examsList,
                    resultsList = resultsList
                )
            }
        }

        // Notice Board Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = JalaramPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "📢 Circular Notice Board",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain
                        )
                    )
                }

                if (currentUser?.role == "Admin") {
                    var showCreateNoticeDialog by remember { mutableStateOf(false) }
                    TextButton(onClick = { showCreateNoticeDialog = true }) {
                        Icon(imageVector = Icons.Default.AddCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Notice", fontWeight = FontWeight.Bold, color = JalaramPrimary)
                    }

                    if (showCreateNoticeDialog) {
                        CreateNoticeDialog(
                            onDismiss = { showCreateNoticeDialog = false },
                            onSubmit = { t, c, ty ->
                                viewModel.createNotification(t, c, ty)
                                showCreateNoticeDialog = false
                            }
                        )
                    }
                }
            }
        }

        // Notice Board list items
        if (notificationsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = JalaramSurface)
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No academy circulars at this moment.",
                            color = JalaramTextSub,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            items(notificationsList) { notice ->
                NoticeItemCard(notice = notice, currentUserRole = currentUser?.role ?: "", onDeleteNotice = {
                    viewModel.deleteNotification(notice.id)
                })
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    countValue: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = countValue,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = JalaramTextMain,
                    fontSize = 24.sp
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = JalaramTextSub,
                    fontSize = 11.sp
                )
            )
        }
    }
}

@Composable
fun NoticeItemCard(
    notice: SystemNotification,
    currentUserRole: String,
    onDeleteNotice: () -> Unit
) {
    val barColor = when (notice.type) {
        "Warning" -> JalaramWarning
        "Success" -> JalaramSuccess
        else -> JalaramPrimary
    }

    val bgColor = when (notice.type) {
        "Warning" -> JalaramWarningContainer.copy(alpha = 0.3f)
        "Success" -> JalaramSuccessContainer.copy(alpha = 0.3f)
        else -> JalaramPrimaryLight.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .background(bgColor)
                .drawWithContent {
                    drawContent()
                    // Draw a solid color bar on the left edge
                    drawRect(
                        color = barColor,
                        size = androidx.compose.ui.geometry.Size(4.dp.toPx(), size.height),
                        topLeft = Offset.Zero
                    )
                }
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notice.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain
                        )
                    )
                    Text(
                        text = notice.dateString,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = JalaramTextSub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = notice.content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = JalaramTextSub,
                        lineHeight = 18.sp
                    )
                )
            }

            if (currentUserRole == "Admin") {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onDeleteNotice, modifier = Modifier.size(36.dp).align(Alignment.CenterVertically)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete notice", tint = JalaramDanger, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoticeDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Info") } // Info, Warning, Success

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Circular Notice") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Notice Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Notice Body Content") },
                    minLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Notice Urgency Level", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = JalaramTextSub)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Info", "Warning", "Success").forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotBlank() && content.isNotBlank()) onSubmit(title, content, type) },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Publish Notice")
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
fun StudentWidgetsModule(
    currentUser: User,
    examsList: List<Exam>,
    resultsList: List<com.example.data.ResultRecord>
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Personalised Academic Almanac",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = JalaramTextMain
            ),
            modifier = Modifier.padding(bottom = 2.dp)
        )

        // 1. Mini-calendar Schedule view
        MiniCalendarWidget(batchName = currentUser.batch ?: "Grade 10-A")

        // 2 & 3 Side by Side or Vertical Cards for spacing
        UpcomingAssignmentsWidget(
            batchName = currentUser.batch ?: "Grade 10-A",
            examsList = examsList
        )

        RecentGradesPerformanceWidget(
            userId = currentUser.userId,
            resultsList = resultsList
        )
    }
}

@Composable
fun MiniCalendarWidget(batchName: String) {
    var selectedDay by remember { mutableStateOf(15) } // June 15 default
    val daysInMonth = (1..30).toList()
    val eventDays = listOf(3, 8, 15, 17, 22, 24, 29) // simulated dynamic schedule lectures for Monday and Wednesday

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar icon",
                        tint = JalaramPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "June 2026 Monthly Planner",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = JalaramTextMain
                    )
                }

                Surface(
                    color = JalaramPrimaryLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = batchName,
                        color = JalaramPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Grid Calendar drawing
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Days header
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextSub
                        )
                    }
                }

                // Chunk days into rows of 7 of a standard calendar month
                val chunks = daysInMonth.chunked(7)
                chunks.forEach { week ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Empty spacer cards for days padding if necessary
                        week.forEach { dayNum ->
                            val isEventDay = dayNum in eventDays
                            val isSelected = selectedDay == dayNum

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) JalaramPrimary
                                        else if (isEventDay) JalaramPrimaryLight
                                        else Color.Transparent
                                    )
                                    .clickable { selectedDay = dayNum },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontWeight = if (isSelected || isEventDay) FontWeight.ExtraBold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color.White else if (isEventDay) JalaramPrimary else JalaramTextMain
                                    )
                                    if (isEventDay && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(JalaramAccent)
                                        )
                                    }
                                }
                            }
                        }

                        // Fill remainder if week is less than 7 days
                        if (week.size < 7) {
                            for (i in 1..(7 - week.size)) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Divider(color = JalaramBorder)

            // Dynamic schedule card response
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JalaramPrimaryLight.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = JalaramPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (selectedDay in eventDays) {
                        "📅 Day $selectedDay Schedule: Live Synchronous lectures for $batchName are online from 10:00 AM."
                    } else {
                        "📅 Day $selectedDay Schedule: Self-guided study. Complete homework coursework modules."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = JalaramPrimary
                )
            }
        }
    }
}

@Composable
fun UpcomingAssignmentsWidget(
    batchName: String,
    examsList: List<Exam>
) {
    val studentExams = remember(examsList, batchName) {
        examsList.filter { it.batch == batchName }
    }

    val completedMap = remember { mutableStateMapOf<String, Boolean>() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = JalaramSuccess,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upcoming Class Assessments & Homework",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = JalaramTextMain
                )
            }

            if (studentExams.isEmpty()) {
                Text(
                    text = "No upcoming tests registered for classroom batch: $batchName",
                    fontSize = 12.sp,
                    color = JalaramTextSub,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (test in studentExams) {
                        val isChecked = completedMap[test.id] ?: false
                        val testId = test.id
                        val testQuestionsCount = test.questionsJson.split("###").filter { it.isNotEmpty() }.size

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { completedMap[testId] = it }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = test.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (isChecked) JalaramTextSub else JalaramTextMain,
                                        style = if (isChecked) MaterialTheme.typography.bodyMedium.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "Max weightage: $testQuestionsCount marks | Subject: Physics Core",
                                        fontSize = 10.sp,
                                        color = JalaramTextSub
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isChecked) JalaramSuccessContainer.copy(alpha = 0.3f) else JalaramPrimaryLight,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isChecked) "Prepared" else "Assigned",
                                    color = if (isChecked) JalaramSuccess else JalaramPrimary,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
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
fun RecentGradesPerformanceWidget(
    userId: String,
    resultsList: List<com.example.data.ResultRecord>
) {
    val myResults = remember(resultsList, userId) {
        resultsList.filter { it.studentId == userId }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = JalaramAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Term Performance Snapshot Ledger",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = JalaramTextMain
                )
            }

            if (myResults.isEmpty()) {
                Text(
                    text = "No assessment scores recorded yet in academic profile ledger.",
                    fontSize = 12.sp,
                    color = JalaramTextSub,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (result in myResults) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = result.examTitle,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = JalaramTextMain
                                    )
                                }
                                Text(
                                    text = "Date recorded: ${result.dateString}",
                                    fontSize = 10.sp,
                                    color = JalaramTextSub
                                )
                            }

                            if (result.isAbsent) {
                                Box(
                                    modifier = Modifier
                                        .background(JalaramDanger.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "ABSENT",
                                        color = JalaramDanger,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp
                                    )
                                }
                            } else {
                                val ratio = result.score.toFloat() / result.totalQuestions
                                val pct = (ratio * 100).toInt()
                                val letterGrade = when {
                                    pct >= 85 -> "Grade A - Distinction"
                                    pct >= 70 -> "Grade B - Credit"
                                    pct >= 50 -> "Grade C - Pass"
                                    else -> "Grade D - Fail"
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${result.score} / ${result.totalQuestions} ($pct%)",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 13.sp,
                                        color = if (pct >= 50) JalaramPrimary else JalaramDanger
                                    )
                                    Text(
                                        text = letterGrade,
                                        fontSize = 9.sp,
                                        color = JalaramTextSub,
                                        fontWeight = FontWeight.Bold
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
