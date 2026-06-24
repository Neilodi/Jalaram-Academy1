package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    viewModel: ErpViewModel,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val batchesList by viewModel.batchesList.collectAsState()

    val headDeviceCount by viewModel.headDeviceCount.collectAsState()
    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    var regRole by remember { mutableStateOf("Student") }
    var regName by remember { mutableStateOf("") }
    var regMobile by remember { mutableStateOf("") }
    var regParentMobile by remember { mutableStateOf("") }
    var regSubjects by remember { mutableStateOf("") }
    var regBatches by remember { mutableStateOf("") }
    var regPin by remember { mutableStateOf("") }

    // Dropdown state for Batch
    var selectedBatch by remember { mutableStateOf("JEE Mains") }
    var batchDropdownExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JalaramPrimaryLight, JalaramBgMain)
                )
            )
    ) {
        // Floating Theme Toggle Button
        IconButton(
            onClick = { isAppInDarkMode = !isAppInDarkMode },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = if (isAppInDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle Theme Mode",
                tint = JalaramTextSub,
                modifier = Modifier.size(28.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 500.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(JalaramPrimaryLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AppRegistration,
                                contentDescription = "Reg icon",
                                tint = JalaramPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Request Access PIN",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = JalaramTextMain
                                )
                            )
                            Text(
                                text = "Member Registration System",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = JalaramTextSub
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Role Select
                    var roleDropdownExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = roleDropdownExpanded,
                        onExpandedChange = { roleDropdownExpanded = !roleDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = regRole,
                            onValueChange = {},
                            label = { Text("Registration Portal Role") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleDropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                focusedBorderColor = JalaramPrimary,
                                unfocusedBorderColor = JalaramBorder
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false },
                            modifier = Modifier.background(JalaramSurface)
                        ) {
                            val rolesList = if (false) {
                                listOf("Student", "Teacher", "Admin")
                            } else {
                                listOf("Student", "Teacher", "Admin", "Head")
                            }
                            rolesList.forEach { role ->
                                DropdownMenuItem(
                                    text = { Text(role, fontWeight = FontWeight.SemiBold) },
                                    onClick = {
                                        regRole = role
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Name
                    OutlinedTextField(
                        value = regName,
                        onValueChange = { regName = it },
                        label = { Text("Full Name") },
                        placeholder = { Text("e.g. Anand Kumar") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = JalaramTextSub) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JalaramPrimary,
                            unfocusedBorderColor = JalaramBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic Contact Phone Label & Behavior based on Role
                    OutlinedTextField(
                        value = regMobile,
                        onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) regMobile = it },
                        label = { Text(if (regRole == "Student") "Student Personal Phone (Optional)" else "Contact Phone (10 digits)") },
                        placeholder = { Text(if (regRole == "Student") "e.g. 9845012345 (Leave blank if none)" else "e.g. 9845012345") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = JalaramTextSub) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JalaramPrimary,
                            unfocusedBorderColor = JalaramBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_phone_input")
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Parent phone list state initialization
                    val parentMobiles = remember { mutableStateListOf<String>("") }

                    // Student vs Teacher Specific parameters
                    AnimatedContent(targetState = regRole, label = "reg_role_field_animation") { role ->
                        if (role == "Student") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "Parent Contact Mobile(s):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = JalaramTextMain,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                parentMobiles.forEachIndexed { idx, pNum ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = pNum,
                                            onValueChange = { newVal ->
                                                if (newVal.all { it.isDigit() } && newVal.length <= 10) {
                                                    parentMobiles[idx] = newVal
                                                }
                                            },
                                            placeholder = { Text("Parent Mobile #${idx + 1}") },
                                            leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = JalaramTextSub) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = JalaramPrimary,
                                                unfocusedBorderColor = JalaramBorder
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        if (parentMobiles.size > 1) {
                                            IconButton(onClick = { parentMobiles.removeAt(idx) }) {
                                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Delete", tint = JalaramDanger)
                                            }
                                        }
                                    }
                                }

                                TextButton(
                                    onClick = { parentMobiles.add("") },
                                    colors = ButtonDefaults.textButtonColors(contentColor = JalaramPrimary)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add slot", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Another Parent Number", fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Batch select dropdown
                                ExposedDropdownMenuBox(
                                    expanded = batchDropdownExpanded,
                                    onExpandedChange = { batchDropdownExpanded = !batchDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        readOnly = true,
                                        value = selectedBatch,
                                        onValueChange = {},
                                        label = { Text("Select Classroom Batch") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownExpanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = JalaramPrimary,
                                            unfocusedBorderColor = JalaramBorder
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
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
                        } else if (role == "Teacher") {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = regSubjects,
                                    onValueChange = { regSubjects = it },
                                    label = { Text("Assigned Subject(s) (comma-separated)") },
                                    placeholder = { Text("e.g. Physics, Chemistry, Maths") },
                                    leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, tint = JalaramTextSub) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_subjects_input")
                                )

                                OutlinedTextField(
                                    value = regBatches,
                                    onValueChange = { regBatches = it },
                                    label = { Text("Assigned Batch(es) (comma-separated)") },
                                    placeholder = { Text("e.g. Class 10-A, JEE Mains") },
                                    leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null, tint = JalaramTextSub) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().testTag("reg_teacher_batches_input")
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login PIN input parameter
                    OutlinedTextField(
                        value = regPin,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) regPin = it },
                        label = { Text("Set 4-digit Account Access PIN") },
                        placeholder = { Text("xxxx") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = JalaramTextSub) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JalaramPrimary,
                            unfocusedBorderColor = JalaramBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_pin_input")
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = {
                            val joinedParentMobiles = parentMobiles.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(",")
                            viewModel.registerNewUser(
                                name = regName,
                                role = regRole,
                                mobile = regMobile,
                                parentMobile = if (regRole == "Student") joinedParentMobiles else null,
                                batch = if (regRole == "Student") selectedBatch else if (regRole == "Teacher") regBatches else null,
                                subjects = if (regRole == "Teacher") regSubjects else null,
                                pin = regPin,
                                onSuccess = { id ->
                                    Toast.makeText(
                                        context,
                                        "Registration submitted! Assigned User ID: $id.\nPlease wait for Admin approval to login.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onCancel()
                                },
                                onError = { error ->
                                    Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("reg_submit_button")
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Submit check mark")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Registration Request", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onCancel,
                        border = BorderStroke(1.dp, JalaramBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Go Back to Portal Login", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
