package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AttendanceRecord
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(viewModel: ErpViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val batchesList by viewModel.batchesList.collectAsState()

    var selectedBatch by remember { mutableStateOf<String?>(null) }
    
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    var selectedDateStr by remember { mutableStateOf(sdf.format(Date())) }

    // State for Attendance List
    var attendanceList by remember { mutableStateOf<List<AttendanceRecord>>(emptyList()) }

    LaunchedEffect(currentUser, selectedBatch, selectedDateStr) {
        if (currentUser?.role == "Student") {
            viewModel.getAttendanceByStudent(currentUser!!.userId).collect {
                attendanceList = it
            }
        } else {
            if (selectedBatch != null) {
                viewModel.getAttendanceByBatchAndDate(selectedBatch!!, selectedDateStr).collect {
                    attendanceList = it
                }
            }
        }
    }

    if (currentUser?.role == "Student") {
        // Student View
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "My Attendance",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = JalaramTextMain)
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (attendanceList.isEmpty()) {
                Text("No attendance records found.", color = JalaramTextSub)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(attendanceList) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(record.dateString, fontWeight = FontWeight.Bold)
                                    Text("Batch: ${record.batch}", style = MaterialTheme.typography.bodySmall)
                                }
                                val color = if (record.status == "Present") JalaramSuccess else JalaramDanger
                                Text(record.status, color = color, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Teacher/Admin View
        val teacherBatches = if (currentUser?.role == "Teacher") {
            val teacherBatchNames = currentUser?.batch?.split(",")?.map { it.trim() } ?: emptyList()
            batchesList.filter { it.name in teacherBatchNames || it.teacherId == currentUser?.userId }
        } else {
            batchesList
        }

        var expandedBatchMenu by remember { mutableStateOf(false) }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Attendance Register",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = JalaramTextMain)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                ExposedDropdownMenuBox(
                    expanded = expandedBatchMenu,
                    onExpandedChange = { expandedBatchMenu = !expandedBatchMenu },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedBatch ?: "Select Batch",
                        onValueChange = {},
                        label = { Text("Batch") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedBatchMenu) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedBatchMenu,
                        onDismissRequest = { expandedBatchMenu = false }
                    ) {
                        teacherBatches.forEach { b ->
                            DropdownMenuItem(
                                text = { Text(b.name) },
                                onClick = {
                                    selectedBatch = b.name
                                    expandedBatchMenu = false
                                }
                            )
                        }
                    }
                }
                
                // Simple Date display for now
                OutlinedTextField(
                    readOnly = true,
                    value = selectedDateStr,
                    onValueChange = {},
                    label = { Text("Date") },
                    leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedBatch == null) {
                Text("Please select a batch to take attendance.", color = JalaramTextSub)
            } else {
                val batchStudents = usersList.filter { it.role == "Student" && it.batch == selectedBatch }
                if (batchStudents.isEmpty()) {
                    Text("No students in this batch.", color = JalaramTextSub)
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Mark Attendance", fontWeight = FontWeight.Bold, color = JalaramTextMain)
                        TextButton(onClick = {
                            batchStudents.forEach { st ->
                                viewModel.markAttendance(st.userId, selectedBatch!!, selectedDateStr, "Present")
                            }
                        }) {
                            Text("Mark All Present")
                        }
                    }

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(batchStudents) { student ->
                            val record = attendanceList.find { it.studentId == student.userId }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(student.name, fontWeight = FontWeight.Bold)
                                        Text("ID: ${student.userId}", style = MaterialTheme.typography.bodySmall, color = JalaramTextSub)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        val isPresent = record?.status == "Present"
                                        val isAbsent = record?.status == "Absent"

                                        FilterChip(
                                            selected = isPresent,
                                            onClick = { viewModel.markAttendance(student.userId, selectedBatch!!, selectedDateStr, "Present") },
                                            label = { Text("Present") },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JalaramSuccess.copy(alpha=0.2f), selectedLabelColor = JalaramSuccess)
                                        )
                                        
                                        FilterChip(
                                            selected = isAbsent,
                                            onClick = { viewModel.markAttendance(student.userId, selectedBatch!!, selectedDateStr, "Absent") },
                                            label = { Text("Absent") },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = JalaramDanger.copy(alpha=0.2f), selectedLabelColor = JalaramDanger)
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
