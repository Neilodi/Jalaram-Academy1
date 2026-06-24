package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.ErpViewModel
import com.example.data.ExamSchedule
import com.example.data.AssignmentDeadline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(viewModel: ErpViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val examSchedules by viewModel.examSchedulesList.collectAsState()
    val deadlines by viewModel.deadlinesList.collectAsState()

    var filterMode by remember { mutableStateOf("All") }

    val userSubjects = currentUser?.subjects?.split(",")?.map { it.trim() } ?: emptyList()
    val userBatch = currentUser?.batch ?: ""

    val filteredExams = remember(examSchedules, filterMode, userSubjects, userBatch) {
        if (filterMode == "All") examSchedules
        else examSchedules.filter { it.subject in userSubjects || it.batch == userBatch }
    }

    val filteredDeadlines = remember(deadlines, filterMode, userSubjects) {
        if (filterMode == "All") deadlines
        else deadlines.filter { it.subject in userSubjects }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Calendar & Deadlines") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterMode == "All",
                    onClick = { filterMode = "All" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterMode == "My Courses",
                    onClick = { filterMode = "My Courses" },
                    label = { Text("My Courses") }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("Exams", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                items(filteredExams) { exam ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(exam.subject, fontWeight = FontWeight.Bold)
                            Text("Date: ${exam.examDate}")
                            Text("Batch: ${exam.batch}")
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
                item { Text("Deadlines", fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                items(filteredDeadlines) { deadline ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(deadline.title, fontWeight = FontWeight.Bold)
                            Text("Subject: ${deadline.subject}")
                            Text("Deadline: ${deadline.deadlineDate}")
                        }
                    }
                }
            }
        }
    }
}
