package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GradeEntryInput
import com.example.data.ResultRecord
import com.example.data.User
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradebookScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val batchesList by viewModel.batchesList.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val resultsList by viewModel.resultsList.collectAsState()

    var selectedBatchName by remember { mutableStateOf("") }
    var showGradeDialog by remember { mutableStateOf(false) }

    // Seed default selected batch if empty and list is not empty
    LaunchedEffect(batchesList) {
        if (selectedBatchName.isEmpty() && batchesList.isNotEmpty()) {
            selectedBatchName = batchesList.first().name
        }
    }

    // Filter students belonging to the selected batch
    val studentsInBatch = remember(usersList, selectedBatchName) {
        usersList.filter { it.role == "Student" && it.batch == selectedBatchName }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dashboard Title Header Block
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Gradebook Almanac",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = JalaramTextMain
                )
                Text(
                    text = "Track student scores, register assessments, and audit performance",
                    style = MaterialTheme.typography.bodySmall,
                    color = JalaramTextSub
                )
            }

            if (selectedBatchName.isNotEmpty()) {
                Button(
                    onClick = { showGradeDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add grade")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Test Entry", fontWeight = FontWeight.Black)
                }
            }
        }

        // Batch Selector Dropdown Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JalaramSurface),
            border = BorderStroke(1.dp, JalaramBorder)
        ) {
            var expandedDropdown by remember { mutableStateOf(false) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Class,
                        contentDescription = null,
                        tint = JalaramPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Class Selection:",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 14.sp
                    )
                }

                Box {
                    Surface(
                        color = JalaramPrimaryLight,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { expandedDropdown = !expandedDropdown }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = selectedBatchName.ifEmpty { "Select Batch..." },
                                color = JalaramPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = JalaramPrimary
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false },
                        modifier = Modifier.background(JalaramSurface)
                    ) {
                        batchesList.forEach { batch ->
                            DropdownMenuItem(
                                text = { Text(batch.name, fontWeight = FontWeight.Bold) },
                                onClick = {
                                    selectedBatchName = batch.name
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Student Assessment Listing
        if (selectedBatchName.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Please register academy classes/batches to view gradebook", color = JalaramTextSub)
            }
        } else if (studentsInBatch.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No active students assigned to batch: $selectedBatchName", color = JalaramTextSub, fontWeight = FontWeight.Bold)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(studentsInBatch) { student ->
                    val studentResults = remember(resultsList, student.userId) {
                        resultsList.filter { it.studentId == student.userId }
                    }

                    GradebookStudentRow(
                        student = student,
                        history = studentResults
                    )
                }
            }
        }
    }

    // Dynamic Test Entry Dialog
    if (showGradeDialog) {
        addGradeEntryDialog(
            batchName = selectedBatchName,
            students = studentsInBatch,
            onDismiss = { showGradeDialog = false },
            onSubmit = { title, maxScore, grades ->
                viewModel.saveGradebook(title, selectedBatchName, maxScore, grades)
                showGradeDialog = false
                Toast.makeText(context, "Grades successfully published to dynamic notices!", Toast.LENGTH_LONG).show()
            }
        )
    }
}

@Composable
fun GradebookStudentRow(
    student: User,
    history: List<ResultRecord>
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Initials indicator avatar circular badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(JalaramPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.avatar,
                            fontWeight = FontWeight.Bold,
                            color = JalaramPrimary,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserPhonePSIndicator(student)
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                color = JalaramTextMain,
                                fontSize = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "S: ${if (student.mobile.isBlank()) "None" else student.mobile} | P: ${student.parentMobile ?: "None"}",
                            color = JalaramTextSub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Performance Quick Stats
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val averageScore = remember(history) {
                        val activeMatches = history.filter { !it.isAbsent }
                        if (activeMatches.isEmpty()) 0f
                        else activeMatches.map { (it.score.toFloat() / it.totalQuestions) * 100 }.sum() / activeMatches.size
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${averageScore.toInt()}%",
                            fontWeight = FontWeight.Black,
                            color = if (averageScore >= 75) JalaramSuccess else if (averageScore >= 40) JalaramPrimary else JalaramDanger,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Avg Term Score",
                            color = JalaramTextSub,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = { isExpanded = !isExpanded }) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand academic transcript",
                            tint = JalaramPrimary
                        )
                    }
                }
            }

            // Expanded Historic Audit Records section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Divider(color = JalaramBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Historical Assessment Record Insights",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No assessments recorded yet for this student.", fontSize = 11.sp, color = JalaramTextSub)
                        }
                    } else {
                        // Drawing simple, clean canvas vertical progress bars for each assessment
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                                .height(90.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            history.take(4).forEach { record ->
                                val pct = if (record.isAbsent) 0f else (record.score.toFloat() / record.totalQuestions)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Visual score bar
                                    Box(
                                        modifier = Modifier
                                            .width(16.dp)
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(pct)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (record.isAbsent) JalaramDanger 
                                                    else if (pct >= 0.75f) JalaramSuccess 
                                                    else JalaramPrimary
                                                )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (record.isAbsent) "ABSENT" else "${record.score}/${record.totalQuestions}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (record.isAbsent) JalaramDanger else JalaramTextMain
                                    )
                                    Text(
                                        text = record.examTitle.take(12) + if (record.examTitle.length > 12) ".." else "",
                                        fontSize = 8.sp,
                                        lineHeight = 10.sp,
                                        color = JalaramTextSub,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        // Detailed list format
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            history.forEach { record ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = record.examTitle,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp,
                                            color = JalaramTextMain
                                        )
                                        Text(
                                            text = record.dateString,
                                            fontSize = 9.sp,
                                            color = JalaramTextSub
                                        )
                                    }

                                    if (record.isAbsent) {
                                        Box(
                                            modifier = Modifier
                                                .background(JalaramDanger.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "ABSENT",
                                                color = JalaramDanger,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            )
                                        }
                                    } else {
                                        val pctStr = "${((record.score.toFloat() / record.totalQuestions) * 100).toInt()}%"
                                        Text(
                                            text = "${record.score} / ${record.totalQuestions} ($pctStr)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = JalaramPrimary
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

@Composable
fun addGradeEntryDialog(
    batchName: String,
    students: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (String, Int, List<GradeEntryInput>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var maxScoreStr by remember { mutableStateOf("100") }

    // Dynamic map to retain inputs for scores and absences
    val inputGrades = remember(students) {
        mutableStateMapOf<String, String>().apply {
            students.forEach { put(it.userId, "") }
        }
    }
    val absentees = remember(students) {
        mutableStateMapOf<String, Boolean>().apply {
            students.forEach { put(it.userId, false) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Class Performance", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Assessment / Test Name") },
                            placeholder = { Text("e.g. Physics Core MCQ Quiz 1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary)
                        )

                        OutlinedTextField(
                            value = maxScoreStr,
                            onValueChange = { if (it.all { c -> c.isDigit() }) maxScoreStr = it },
                            label = { Text("Total Assessment Max Marks") },
                            placeholder = { Text("e.g. 100") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary)
                        )
                    }
                }

                item {
                    Text(
                        text = "Student Performance Worksheet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = JalaramTextMain,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(students) { student ->
                    val isAbsent = absentees[student.userId] ?: false
                    val marksValue = inputGrades[student.userId] ?: ""

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(
                                text = student.name,
                                fontWeight = FontWeight.Bold,
                                color = JalaramTextMain,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "ID: ${student.userId}",
                                color = JalaramTextSub,
                                fontSize = 10.sp
                            )
                        }

                        // Absent Checkbox Switch
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Checkbox(
                                checked = isAbsent,
                                onCheckedChange = { absentees[student.userId] = it }
                            )
                            Text(
                                text = "Absent",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAbsent) JalaramDanger else JalaramTextMain
                            )
                        }

                        // Score marks input field
                        OutlinedTextField(
                            value = if (isAbsent) "ABS" else marksValue,
                            onValueChange = { newVal ->
                                if (!isAbsent && newVal.all { it.isDigit() }) {
                                    inputGrades[student.userId] = newVal
                                }
                            },
                            enabled = !isAbsent,
                            placeholder = { Text("Score") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(70.dp)
                                .height(48.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JalaramPrimary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val maxScore = maxScoreStr.toIntOrNull() ?: 100
                    if (title.isBlank()) {
                        return@Button
                    }
                    val finalGrades = students.map { s ->
                        val isAbsent = absentees[s.userId] ?: false
                        val scoreVal = inputGrades[s.userId]?.toIntOrNull() ?: 0
                        GradeEntryInput(
                            studentId = s.userId,
                            studentName = s.name,
                            score = if (isAbsent) 0 else scoreVal,
                            isAbsent = isAbsent
                        )
                    }
                    onSubmit(title, maxScore, finalGrades)
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Publish Assessment Ledger")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
