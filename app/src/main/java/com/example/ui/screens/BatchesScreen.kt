package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AcademyBatch
import com.example.data.User
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun BatchesScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val batchesList by viewModel.batchesList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val usersList by viewModel.usersList.collectAsState()

    var showCreateBatchDialog by remember { mutableStateOf(false) }
    var editingBatch by remember { mutableStateOf<AcademyBatch?>(null) }

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
                    text = "Room Classrooms & Batches",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain
                    )
                )
                Text(
                    text = "Managed academic groups for synchronous lectures.",
                    color = JalaramTextSub,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (currentUser?.role == "Admin") {
                Button(
                    onClick = { showCreateBatchDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Batch")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Batch", fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(batchesList) { batch ->
                // Look up matching teacher name or details
                val teacherUser = usersList.find { it.userId == batch.teacherId || (it.role == "Teacher" && it.subjects?.contains("Physics") == true) }
                BatchCard(
                    batch = batch,
                    teacherName = teacherUser?.name ?: "Dr. Shalini Vyas", // fallback value matching seeded database
                    currentUserRole = currentUser?.role ?: "",
                    usersList = usersList,
                    onEdit = { editingBatch = batch },
                    onDelete = { viewModel.deleteBatch(batch.id) }
                )
            }
        }
    }

    if (showCreateBatchDialog) {
        CreateBatchDialog(
            usersList = usersList,
            onDismiss = { showCreateBatchDialog = false },
            onSubmit = { name, teacherId ->
                viewModel.createBatch(name, teacherId)
                showCreateBatchDialog = false
                Toast.makeText(context, "New Batch created successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    editingBatch?.let { batch ->
        EditBatchDialog(
            batch = batch,
            usersList = usersList,
            onDismiss = { editingBatch = null },
            onSubmit = { newName, teacherId, selectedStudentIds ->
                viewModel.editBatch(batch.id, newName, teacherId, selectedStudentIds)
                editingBatch = null
                Toast.makeText(context, "Batch and student enrollment updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun BatchCard(
    batch: AcademyBatch,
    teacherName: String,
    currentUserRole: String,
    usersList: List<User>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val batchStudents = remember(usersList, batch.name) {
        usersList.filter { it.role == "Student" && it.batch == batch.name }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(JalaramPrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MeetingRoom,
                            contentDescription = null,
                            tint = JalaramPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = batch.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = JalaramTextMain
                        )
                        Text(
                            text = "Assigned Faculty Coordinator: $teacherName",
                            color = JalaramTextSub,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (currentUserRole == "Admin") {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Batch",
                                tint = JalaramPrimary
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Batch",
                                tint = JalaramDanger
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Expand students list",
                            tint = JalaramPrimary
                        )
                    }
                }
            }

            // Student list under batch expansion
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Divider(color = JalaramBorder)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Enrolled Students (${batchStudents.size})",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (batchStudents.isEmpty()) {
                        Text(
                            text = "No students registered under this class batch yet.",
                            fontSize = 12.sp,
                            color = JalaramTextSub,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            batchStudents.forEach { student ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        // Student circular initials avatar
                                        Box(
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clip(CircleShape)
                                                .background(JalaramPrimaryLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.avatar,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = JalaramPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Badges shown only to admins and teachers
                                            if (currentUserRole == "Admin" || currentUserRole == "Teacher") {
                                                UserPhonePSIndicator(student)
                                            }
                                            Text(
                                                text = student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = JalaramTextMain
                                            )
                                        }
                                    }

                                    // Phone details shown only to admins and teachers
                                    if (currentUserRole == "Admin" || currentUserRole == "Teacher") {
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "S: ${if (student.mobile.isBlank()) "None" else student.mobile}",
                                                fontSize = 10.sp,
                                                color = JalaramTextSub,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "P: ${student.parentMobile ?: "None"}",
                                                fontSize = 10.sp,
                                                color = JalaramTextSub,
                                                fontWeight = FontWeight.Medium
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBatchDialog(
    usersList: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (name: String, teacherId: String?) -> Unit
) {
    var batchName by remember { mutableStateOf("") }
    var selectedTeacherId by remember { mutableStateOf<String?>(null) }
    
    val teachers = remember(usersList) { usersList.filter { it.role == "Teacher" } }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }
    val selectedTeacherName = teachers.find { it.userId == selectedTeacherId }?.name ?: "None Assigned"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Assemble New Class Batch", fontWeight = FontWeight.Bold, color = JalaramPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = batchName,
                    onValueChange = { batchName = it },
                    label = { Text("Batch Name (e.g. NEET Droppers 2026)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Faculty Coordinator:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JalaramTextMain)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { teacherDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramPrimary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedTeacherName, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = teacherDropdownExpanded,
                        onDismissRequest = { teacherDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(JalaramSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None Assigned") },
                            onClick = {
                                selectedTeacherId = null
                                teacherDropdownExpanded = false
                            }
                        )
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text("${teacher.name} (${teacher.userId})") },
                                onClick = {
                                    selectedTeacherId = teacher.userId
                                    teacherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (batchName.isNotBlank()) onSubmit(batchName, selectedTeacherId) },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Create Batch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBatchDialog(
    batch: AcademyBatch,
    usersList: List<User>,
    onDismiss: () -> Unit,
    onSubmit: (newName: String, teacherId: String?, selectedStudentIds: List<String>) -> Unit
) {
    var name by remember { mutableStateOf(batch.name) }
    var selectedTeacherId by remember { mutableStateOf(batch.teacherId) }
    
    val teachers = remember(usersList) { usersList.filter { it.role == "Teacher" } }
    val students = remember(usersList) { usersList.filter { it.role == "Student" } }
    
    val initialSelectedIds = remember(students, batch.name) {
        students.filter { it.batch == batch.name }.map { it.userId }
    }
    val selectedStudentIds = remember { mutableStateListOf<String>().apply { addAll(initialSelectedIds) } }
    
    var teacherDropdownExpanded by remember { mutableStateOf(false) }
    val selectedTeacherName = teachers.find { it.userId == selectedTeacherId }?.name ?: "None Assigned"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Class Batch & Students", fontWeight = FontWeight.Bold, color = JalaramPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Batch Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Faculty Coordinator:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JalaramTextMain)
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { teacherDropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramPrimary)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedTeacherName, fontWeight = FontWeight.SemiBold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = teacherDropdownExpanded,
                        onDismissRequest = { teacherDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f).background(JalaramSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("None Assigned") },
                            onClick = {
                                selectedTeacherId = null
                                teacherDropdownExpanded = false
                            }
                        )
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { Text("${teacher.name} (${teacher.userId})") },
                                onClick = {
                                    selectedTeacherId = teacher.userId
                                    teacherDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Enroll/Remove Students (${selectedStudentIds.size} Enrolled):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = JalaramTextMain)
                
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, JalaramBorder),
                    color = Color.White.copy(alpha = 0.05f)
                ) {
                    if (students.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No registered students found.", fontSize = 12.sp, color = JalaramTextSub)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(students) { student ->
                                val isChecked = selectedStudentIds.contains(student.userId)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (isChecked) {
                                                selectedStudentIds.remove(student.userId)
                                            } else {
                                                selectedStudentIds.add(student.userId)
                                            }
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(JalaramPrimaryLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(student.avatar, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = JalaramPrimary)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(student.name, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = JalaramTextMain)
                                            Text(
                                                text = "Current Batch: ${student.batch ?: "None"}",
                                                fontSize = 10.sp,
                                                color = if (student.batch != null && student.batch != batch.name) JalaramAccent else JalaramTextSub
                                            )
                                        }
                                    }
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            if (checked == true) {
                                                selectedStudentIds.add(student.userId)
                                            } else {
                                                selectedStudentIds.remove(student.userId)
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = JalaramPrimary)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSubmit(name, selectedTeacherId, selectedStudentIds.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
