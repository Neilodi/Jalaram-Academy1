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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.User
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val usersList by viewModel.usersList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var userToEdit by remember { mutableStateOf<User?>(null) }
    var userToApprove by remember { mutableStateOf<User?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }

    var showNewSubjectDialog by remember { mutableStateOf(false) }
    var pendingSaveAction by remember { mutableStateOf<((String) -> Unit)?>(null) }
    var detectedNewSubjects by remember { mutableStateOf<List<String>>(emptyList()) }
    var originalSubjectsToProcess by remember { mutableStateOf("") }

    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    val pendingUsers = remember(usersList) { usersList.filter { it.status == "Pending" } }
    val activeUsers = remember(usersList, searchQuery) {
        usersList.filter {
            it.status == "Active" && (it.name.contains(searchQuery, ignoreCase = true) || it.userId.contains(searchQuery, ignoreCase = true))
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 🛠️ System Administration & Simulation Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                border = BorderStroke(1.dp, JalaramBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Add user button
                    Button(
                        onClick = { showAddUserDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add New Academy Member / Admin", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Pending approvals header and cards
        if (pendingUsers.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JalaramWarningContainer.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, JalaramWarning.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = JalaramWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ Pending Registrations (${pendingUsers.size})",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JalaramWarning
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        pendingUsers.forEach { user ->
                            PendingUserRow(
                                user = user,
                                onApprove = { userToApprove = user },
                                onReject = { viewModel.rejectUser(user.userId) }
                            )
                        }
                    }
                }
            }
        }

        // Active Users directory header
        item {
            Text(
                text = "Active Members Directory",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = JalaramTextMain
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter database by Name or User ID...") },
                leadingIcon = { Icon(imageVector = Icons.Default.FilterList, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JalaramPrimary,
                    unfocusedBorderColor = JalaramBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Active listing rows
        if (activeUsers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No matching active academy users found.",
                        color = JalaramTextSub,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else {
            items(activeUsers) { user ->
                ActiveUserRow(
                    user = user,
                    onEditClick = { userToEdit = user }
                )
            }
        }
    }

    val checkNewSubjectsAndRun = { role: String, subjects: String?, onProceed: (String?) -> Unit ->
        if (role != "Teacher" || subjects.isNullOrBlank()) {
            onProceed(subjects)
        } else {
            val existingSubjects = viewModel.coursesList.value.map { it.category.trim().lowercase() }.distinct()
            val teacherSubjects = subjects.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            val newSubjects = teacherSubjects.filter { it.lowercase() !in existingSubjects }

            if (newSubjects.isNotEmpty()) {
                detectedNewSubjects = newSubjects
                originalSubjectsToProcess = subjects
                pendingSaveAction = { finalSubjects ->
                    onProceed(finalSubjects)
                }
                showNewSubjectDialog = true
            } else {
                onProceed(subjects)
            }
        }
    }

    // Edit user parameter dialog modal
    userToEdit?.let { user ->
        EditUserDialog(
            user = user,
            onDismiss = { userToEdit = null },
            onSave = { name, mobile, parentMobile, b, subjects ->
                checkNewSubjectsAndRun(user.role, subjects) { finalSubjects ->
                    viewModel.editUserByAdmin(user.userId, name, mobile, parentMobile, b, finalSubjects)
                    userToEdit = null
                    Toast.makeText(context, "Directory record updated!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // Approve & Edit user registration modal
    userToApprove?.let { user ->
        ApproveUserDialog(
            user = user,
            viewModel = viewModel,
            onDismiss = { userToApprove = null },
            onApprove = { newUserId, name, role, mobile, parentMobile, batch, subjects ->
                checkNewSubjectsAndRun(role, subjects) { finalSubjects ->
                    viewModel.approveUserWithEdits(
                        oldUserId = user.userId,
                        newUserId = newUserId,
                        name = name,
                        role = role,
                        mobile = mobile,
                        parentMobile = parentMobile,
                        batch = batch,
                        subjects = finalSubjects
                    )
                    userToApprove = null
                }
            }
        )
    }

    if (showAddUserDialog) {
        AddUserDialog(
            viewModel = viewModel,
            onDismiss = { showAddUserDialog = false },
            onSave = { name, role, mobile, parentMobile, batch, subjects, pin, status ->
                checkNewSubjectsAndRun(role, subjects) { finalSubjects ->
                    viewModel.addNewUserByAdmin(name, role, mobile, parentMobile, batch, finalSubjects, pin, status)
                    showAddUserDialog = false
                    Toast.makeText(context, "New member added!", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showNewSubjectDialog) {
        var step by remember { mutableStateOf(1) }
        var intendedSubjectInput by remember { mutableStateOf("") }

        if (step == 1) {
            AlertDialog(
                onDismissRequest = {
                    showNewSubjectDialog = false
                    pendingSaveAction = null
                },
                title = { Text("New Subject Detected", fontWeight = FontWeight.Bold, color = JalaramPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "The following new subject(s) were found:",
                            fontWeight = FontWeight.SemiBold,
                            color = JalaramTextMain
                        )
                        detectedNewSubjects.forEach { subj ->
                            Text("• $subj", color = JalaramPrimary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Do you want to add these new subject(s) to the system course directory?",
                            color = JalaramTextSub
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            detectedNewSubjects.forEach { subj ->
                                viewModel.addNewSubjectCourse(subj)
                            }
                            showNewSubjectDialog = false
                            pendingSaveAction?.let { it(originalSubjectsToProcess) }
                            pendingSaveAction = null
                            Toast.makeText(context, "New subject(s) added to directory!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramSuccess)
                    ) {
                        Text("Yes, Add Subject(s)")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            step = 2
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramDanger)
                    ) {
                        Text("No")
                    }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = {
                    showNewSubjectDialog = false
                    pendingSaveAction = null
                },
                title = { Text("Set Intended Subject", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Since you selected not to add the new subject(s) to the system, please enter the intended existing subject (e.g., Physics) to replace it, or tap Skip to omit them.",
                            fontSize = 13.sp,
                            color = JalaramTextSub
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = intendedSubjectInput,
                            onValueChange = { intendedSubjectInput = it },
                            label = { Text("Intended Existing Subject Name") },
                            placeholder = { Text("e.g. Physics") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val cleanedIntended = intendedSubjectInput.trim()
                            if (cleanedIntended.isNotEmpty()) {
                                showNewSubjectDialog = false
                                val existingSubjects = viewModel.coursesList.value.map { it.category.trim().lowercase() }.distinct()
                                val teacherSubjects = originalSubjectsToProcess.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val finalized = teacherSubjects.map { subj ->
                                    if (subj.lowercase() !in existingSubjects) cleanedIntended else subj
                                }.distinct().joinToString(", ")
                                
                                pendingSaveAction?.let { it(finalized) }
                                pendingSaveAction = null
                                Toast.makeText(context, "Saved with intended subject: $cleanedIntended", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Please enter a valid subject name, or click Skip", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                    ) {
                        Text("Apply Intended")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showNewSubjectDialog = false
                            val existingSubjects = viewModel.coursesList.value.map { it.category.trim().lowercase() }.distinct()
                            val teacherSubjects = originalSubjectsToProcess.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            val finalized = teacherSubjects.filter { subj ->
                                subj.lowercase() in existingSubjects
                            }.distinct().joinToString(", ")
                            
                            pendingSaveAction?.let { it(finalized) }
                            pendingSaveAction = null
                            Toast.makeText(context, "Saved by skipping new subject(s)", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Skip")
                    }
                }
            )
        }
    }
}

@Composable
fun UserPhonePSIndicator(user: User) {
    val hasStudentNumber = user.mobile.isNotBlank()
    val hasParentNumber = !user.parentMobile.isNullOrBlank()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(end = 4.dp)
    ) {
        if (hasParentNumber) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF2E7D32), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "P",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        if (hasStudentNumber) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF1565C0), shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "S",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun PendingUserRow(
    user: User,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    color = JalaramTextMain,
                    fontSize = 15.sp
                )
                Text(
                    text = "${user.role} • ID: ${user.userId} • Mobile: ${user.mobile}",
                    color = JalaramTextSub,
                    fontSize = 12.sp
                )
                if (user.role == "Student") {
                    Text(
                        text = "Batch: ${user.batch ?: "Not Assigned"} | Parent: ${user.parentMobile ?: "N/A"}",
                        color = JalaramTextSub,
                        fontSize = 11.sp
                    )
                } else if (user.role == "Teacher") {
                    Text(
                        text = "Subjects: ${user.subjects ?: "N/A"}",
                        color = JalaramTextSub,
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onApprove,
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramSuccess),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramDanger),
                    border = BorderStroke(1.dp, JalaramDanger.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text("Reject", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ActiveUserRow(
    user: User,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circle initials avatar graphic
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(JalaramPrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatar,
                        fontWeight = FontWeight.Bold,
                        color = JalaramPrimary,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (user.role == "Student") {
                            UserPhonePSIndicator(user)
                        }
                        Text(
                            text = user.name,
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain,
                            fontSize = 15.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Surface(
                            color = JalaramPrimaryLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = user.role.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = JalaramPrimary
                                ),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "ID: ${user.userId}",
                            color = JalaramTextSub,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (user.role == "Student") {
                        if (user.batch != null) {
                            Text(
                                text = "Batch: ${user.batch}",
                                color = JalaramTextSub,
                                fontSize = 11.sp
                            )
                        }
                        Text(
                            text = "S: ${if (user.mobile.isBlank()) "None" else user.mobile} | P: ${user.parentMobile ?: "None"}",
                            color = JalaramTextSub,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    } else if (user.role == "Teacher" && user.subjects != null) {
                        Text(
                            text = "Teaches: ${user.subjects}",
                            color = JalaramTextSub,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (user.role == "Student") {
                    IconButton(onClick = { /* TODO: Implement suspend */ }) {
                        Icon(imageVector = Icons.Default.Block, contentDescription = "Suspend student", tint = JalaramDanger)
                    }
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit directory user record",
                        tint = JalaramPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var mobile by remember { mutableStateOf(user.mobile) }
    var batch by remember { mutableStateOf(user.batch ?: "") }
    var subjects by remember { mutableStateOf(user.subjects ?: "") }
    
    // Parent numbers dynamic editing
    val parentNumbers = remember {
        val list = mutableStateListOf<String>()
        val existing = user.parentMobile?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        if (existing.isNullOrEmpty()) {
            list.add("")
        } else {
            list.addAll(existing)
        }
        list
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Member Record") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Student Personal Mobile (Optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    label = { Text("Assigned Subjects (comma-separated)") },
                    placeholder = { Text("e.g. Physics, Maths, Chemistry") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth().testTag("edit_subjects_input")
                )

                if (user.role == "Student" || user.role == "Teacher") {
                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text(if (user.role == "Student") "Classroom Batch Assignment" else "Assigned Batches (comma-separated)") },
                        placeholder = { Text(if (user.role == "Student") "e.g. Class 10-A" else "e.g. Class 10-A, JEE Mains") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (user.role == "Student") {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Parent Contact Mobile(s):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = JalaramTextMain
                    )
                    
                    parentNumbers.forEachIndexed { idx, pNum ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = pNum,
                                onValueChange = { newVal ->
                                    if (newVal.all { it.isDigit() } && newVal.length <= 10) {
                                        parentNumbers[idx] = newVal
                                    }
                                },
                                placeholder = { Text("Parent Phone ${idx + 1}") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary)
                            )
                            
                            if (parentNumbers.size > 1) {
                                IconButton(onClick = { parentNumbers.removeAt(idx) }) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Delete", tint = JalaramDanger)
                                }
                            }
                        }
                    }
                    
                    TextButton(
                        onClick = { parentNumbers.add("") },
                        colors = ButtonDefaults.textButtonColors(contentColor = JalaramPrimary)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add parent", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Another Parent Number", fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val pMobile = if (user.role == "Student") {
                        parentNumbers.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
                    } else {
                        null
                    }
                    onSave(
                        name,
                        mobile,
                        if (pMobile.isNullOrBlank()) null else pMobile,
                        if (user.role == "Student" || user.role == "Teacher") batch else null,
                        if (subjects.isBlank()) null else subjects
                    )
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

@Composable
fun ApproveUserDialog(
    user: User,
    viewModel: ErpViewModel,
    onDismiss: () -> Unit,
    onApprove: (String, String, String, String, String?, String?, String?) -> Unit
) {
    val headDeviceCount by viewModel.headDeviceCount.collectAsState()
    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    var userId by remember { mutableStateOf(user.userId) }
    var name by remember { mutableStateOf(user.name) }
    var role by remember { mutableStateOf(user.role) }
    var mobile by remember { mutableStateOf(user.mobile) }
    var batch by remember { mutableStateOf(user.batch ?: "") }
    var subjects by remember { mutableStateOf(user.subjects ?: "") }
    var parentMobile by remember { mutableStateOf(user.parentMobile ?: "") }

    var expandedRoleDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HowToReg,
                    contentDescription = null,
                    tint = JalaramPrimary
                )
                Text("Approve & Edit Registration", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Verify and customize all registry details before activating this account. Password details are hidden and kept secure.",
                    fontSize = 12.sp,
                    color = JalaramTextSub,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = userId,
                    onValueChange = { userId = it.trim() },
                    label = { Text("Custom User ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Dropdown Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assigned System Role") },
                        trailingIcon = {
                            IconButton(onClick = { expandedRoleDropdown = true }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Role")
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedRoleDropdown = true }
                    )
                    DropdownMenu(
                        expanded = expandedRoleDropdown,
                        onDismissRequest = { expandedRoleDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val rolesList = if (false) {
                            listOf("Student", "Teacher", "Admin")
                        } else {
                            listOf("Student", "Teacher", "Admin", "Head")
                        }
                        rolesList.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    expandedRoleDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it.trim() },
                    label = { Text("Contact Phone Mobile") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role dependent fields
                if (role == "Student") {
                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Classroom Batch Assignment") },
                        placeholder = { Text("e.g. Class 10-A, Class 12-B") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = parentMobile,
                        onValueChange = { parentMobile = it.trim() },
                        label = { Text("Parent Contact Number(s)") },
                        placeholder = { Text("e.g. 9876543210") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (role == "Teacher") {
                    OutlinedTextField(
                        value = subjects,
                        onValueChange = { subjects = it },
                        label = { Text("Assigned Subject(s) (comma-separated)") },
                        placeholder = { Text("e.g. Physics, Chemistry, Maths") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Assigned Batch(es) (comma-separated)") },
                        placeholder = { Text("e.g. Class 10-A, JEE Mains") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (userId.isBlank() || name.isBlank()) {
                        return@Button
                    }
                    onApprove(
                        userId,
                        name,
                        role,
                        mobile,
                        if (parentMobile.isBlank()) null else parentMobile,
                        if (batch.isBlank()) null else batch,
                        if (subjects.isBlank()) null else subjects
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramSuccess)
            ) {
                Text("Approve & Save", fontWeight = FontWeight.Bold)
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
fun AddUserDialog(
    viewModel: ErpViewModel,
    onDismiss: () -> Unit,
    onSave: (name: String, role: String, mobile: String, parentMobile: String?, batch: String?, subjects: String?, pin: String, status: String) -> Unit
) {
    val headDeviceCount by viewModel.headDeviceCount.collectAsState()
    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Student") }
    var mobile by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("1234") }
    var status by remember { mutableStateOf("Pending") }
    var parentMobile by remember { mutableStateOf("") }
    var batch by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf("") }

    var roleExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add New Academy Member",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    placeholder = { Text("Enter full name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Role Selector
                ExposedDropdownMenuBox(
                    expanded = roleExpanded,
                    onExpandedChange = { roleExpanded = !roleExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = role,
                        onValueChange = {},
                        label = { Text("Select Portal Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = roleExpanded,
                        onDismissRequest = { roleExpanded = false }
                    ) {
                        val rolesList = if (false) {
                            listOf("Student", "Teacher", "Admin")
                        } else {
                            listOf("Student", "Teacher", "Admin", "Head")
                        }
                        rolesList.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    role = r
                                    roleExpanded = false
                                }
                            )
                        }
                    }
                }

                // Mobile
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it.trim() },
                    label = { Text("Mobile Contact Number") },
                    placeholder = { Text("10-digit number") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // PIN / Password
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.trim() },
                    label = { Text("Secure PIN / Password") },
                    placeholder = { Text("e.g. 1234") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Selector
                ExposedDropdownMenuBox(
                    expanded = statusExpanded,
                    onExpandedChange = { statusExpanded = !statusExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = status,
                        onValueChange = {},
                        label = { Text("Account Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = statusExpanded,
                        onDismissRequest = { statusExpanded = false }
                    ) {
                        listOf("Pending", "Active").forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s) },
                                onClick = {
                                    status = s
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                // Conditional fields based on Role
                if (role == "Student") {
                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Assigned Classroom Batch") },
                        placeholder = { Text("e.g. Class 10-A, Class 12-B") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = parentMobile,
                        onValueChange = { parentMobile = it.trim() },
                        label = { Text("Parent Contact Number(s)") },
                        placeholder = { Text("e.g. 9876543210") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (role == "Teacher") {
                    OutlinedTextField(
                        value = subjects,
                        onValueChange = { subjects = it },
                        label = { Text("Assigned Subject(s) (comma-separated)") },
                        placeholder = { Text("e.g. Physics, Chemistry, Maths") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Assigned Batch(es) (comma-separated)") },
                        placeholder = { Text("e.g. Class 10-A, JEE Mains") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || mobile.length != 10) {
                        return@Button
                    }
                    onSave(
                        name,
                        role,
                        mobile,
                        if (parentMobile.isBlank()) null else parentMobile,
                        if (batch.isBlank()) null else batch,
                        if (subjects.isBlank()) null else subjects,
                        pin,
                        status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
