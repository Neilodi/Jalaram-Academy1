package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Course
import com.example.data.LectureMaterial
import com.example.data.User
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import kotlinx.coroutines.delay
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import kotlinx.coroutines.launch

@Composable
fun CoursesScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    
    // Tabs state
    var selectedTabIdx by remember { mutableStateOf(0) }
    val tabs = listOf("Lecture Materials & Notes", "Premium Syllabus")

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Aesthetic Tab Header with M3 indicator
        TabRow(
            selectedTabIndex = selectedTabIdx,
            containerColor = JalaramSurface,
            contentColor = JalaramPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIdx]),
                    color = JalaramPrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIdx == index,
                    onClick = { selectedTabIdx = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIdx == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        AnimatedContent(
            targetState = selectedTabIdx,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "courses_tab_transition",
            modifier = Modifier.weight(1f)
        ) { targetIdx ->
            when (targetIdx) {
                0 -> LectureMaterialsTab(viewModel = viewModel)
                1 -> PremiumSyllabusTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun LectureMaterialsTab(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val lectureMaterialsList by viewModel.lectureMaterialsList.collectAsState()
    val usersList by viewModel.usersList.collectAsState()
    val batchesList by viewModel.batchesList.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var selectedTypeFilter by remember { mutableStateOf("All") }
    
    var activeViewMaterial by remember { mutableStateOf<LectureMaterial?>(null) }
    var showUploadMaterialDialog by remember { mutableStateOf(false) }

    // Parse student's specific mapped subjects
    val mySubjectsList = remember(currentUser) {
        currentUser?.subjects
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    // Default to true for students who have registered mapped subjects
    var filterToMySubjectsOnly by remember {
        mutableStateOf(currentUser?.role == "Student" && mySubjectsList.isNotEmpty())
    }

    // Determine assigned teachers for student roles
    val assignedTeachers = remember(currentUser, usersList, batchesList) {
        if (currentUser?.role != "Student") emptyList<User>()
        else {
            val studentBatch = currentUser?.batch ?: ""
            if (studentBatch.isBlank()) emptyList()
            else {
                usersList.filter { user ->
                    user.role == "Teacher" && (
                        (user.batch ?: "").split(",").map { it.trim().lowercase() }.contains(studentBatch.lowercase()) ||
                        batchesList.any { b -> b.name.equals(studentBatch, ignoreCase = true) && b.teacherId == user.userId }
                    )
                }
            }
        }
    }

    val assignedTeacherNamesAndIds = remember(assignedTeachers) {
        assignedTeachers.flatMap { listOf(it.name.lowercase(), it.userId.lowercase()) }.toSet()
    }

    // Filter Logic
    val filteredMaterials = remember(
        lectureMaterialsList, searchQuery, selectedSubjectFilter, selectedTypeFilter, filterToMySubjectsOnly, mySubjectsList, assignedTeacherNamesAndIds, currentUser
    ) {
        lectureMaterialsList.filter { material ->
            // Search query match
            val matchesSearch = material.title.contains(searchQuery, ignoreCase = true) ||
                    material.topic.contains(searchQuery, ignoreCase = true) ||
                    material.description.contains(searchQuery, ignoreCase = true)
            
            // Subject filter chip match
            val matchesSubject = selectedSubjectFilter == "All" ||
                    material.subject.equals(selectedSubjectFilter, ignoreCase = true)

            // Material Type filter chip match
            val matchesType = selectedTypeFilter == "All" ||
                    material.materialType.equals(selectedTypeFilter, ignoreCase = true)

            // Map to specific student subjects constraint
            val matchesEnrolledSubjects = !filterToMySubjectsOnly ||
                    mySubjectsList.any { enrolled -> enrolled.equals(material.subject, ignoreCase = true) }

            // Map to specific student assigned teacher constraint
            val matchesTeacherConstraint = if (currentUser?.role == "Student") {
                material.uploadedBy.lowercase() in assignedTeacherNamesAndIds
            } else {
                true
            }

            matchesSearch && matchesSubject && matchesType && matchesEnrolledSubjects && matchesTeacherConstraint
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Search Bar & Publish Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search notes, topics, formulas...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JalaramPrimary,
                        unfocusedBorderColor = JalaramBorder
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (currentUser?.role == "Admin" || currentUser?.role == "Teacher") {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { showUploadMaterialDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload Notes")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Student Curriculum Subject Mapping Toggle Card
            if (currentUser?.role == "Student" && mySubjectsList.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (filterToMySubjectsOnly) JalaramPrimaryLight.copy(alpha = 0.15f) else JalaramSurface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (filterToMySubjectsOnly) JalaramPrimary.copy(alpha = 0.4f) else JalaramBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (filterToMySubjectsOnly) JalaramPrimary else JalaramTextSub.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (filterToMySubjectsOnly) Color.White else JalaramTextSub,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Map to My Specific Subjects",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = JalaramTextMain
                                )
                                Text(
                                    text = "Subjects: ${mySubjectsList.joinToString(", ")}",
                                    fontSize = 11.sp,
                                    color = JalaramTextSub,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        
                        Switch(
                            checked = filterToMySubjectsOnly,
                            onCheckedChange = { filterToMySubjectsOnly = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = JalaramPrimary
                            )
                        )
                    }
                }
            }

            // Subject Filters Carousel
            Text(
                text = "Subject Categories",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = JalaramTextMain,
                modifier = Modifier.padding(top = 2.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Physics", "Chemistry", "Maths", "Biology", "English").forEach { subject ->
                    val isSelected = selectedSubjectFilter == subject
                    val isMyMappedSubject = mySubjectsList.any { it.equals(subject, ignoreCase = true) }
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedSubjectFilter = subject },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(subject, fontWeight = FontWeight.Bold)
                                if (currentUser?.role == "Student" && isMyMappedSubject) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color.White else JalaramPrimary)
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JalaramPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Type Filters Carousel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "PDF Notes", "Formula Sheet", "Slides", "Assignment").forEach { mType ->
                    val isSelected = selectedTypeFilter == mType
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedTypeFilter = mType },
                        label = { Text(mType, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JalaramAccent,
                            selectedLabelColor = JalaramPrimary
                        )
                    )
                }
            }

            // Materials Grid List
            if (filteredMaterials.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = JalaramTextSub.copy(alpha = 0.3f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No lecture materials found",
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain
                        )
                        Text(
                            text = "Try clearing search keywords, toggling mapping filters, or selecting a different subject.",
                            color = JalaramTextSub,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(filteredMaterials) { material ->
                        LectureMaterialGridCard(
                            material = material,
                            currentUserRole = currentUser?.role ?: "",
                            onDelete = {
                                viewModel.deleteLectureMaterial(material.id)
                                Toast.makeText(context, "Material deleted permanently.", Toast.LENGTH_SHORT).show()
                            },
                            onAccess = { activeViewMaterial = material }
                        )
                    }
                }
            }
        }

        // Upload dialog overlay
        if (showUploadMaterialDialog) {
            UploadMaterialDialog(
                onDismiss = { showUploadMaterialDialog = false },
                onUpload = { title, subject, topic, mType, url, size, desc ->
                    viewModel.uploadLectureMaterial(
                        title = title,
                        subject = subject,
                        topic = topic,
                        materialType = mType,
                        fileUrl = url,
                        uploadedBy = currentUser?.name ?: "Faculty",
                        fileSize = size,
                        description = desc
                    )
                    showUploadMaterialDialog = false
                    Toast.makeText(context, "Notes & materials published successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // Material full-immersive Viewer Dialog
        activeViewMaterial?.let { material ->
            MaterialViewerDialog(
                material = material,
                currentUserRole = currentUser?.role ?: "",
                onDismiss = { activeViewMaterial = null }
            )
        }
    }
}

@Composable
fun LectureMaterialGridCard(
    material: LectureMaterial,
    currentUserRole: String,
    onDelete: () -> Unit,
    onAccess: () -> Unit
) {
    // Select visual icon based on type
    val (icon, colorBg, colorFg) = remember(material.materialType) {
        when (material.materialType) {
            "PDF Notes" -> Triple(Icons.Default.Description, Color(0xFFFEE2E2), Color(0xFFEF4444))
            "Formula Sheet" -> Triple(Icons.Default.AutoStories, Color(0xFFE0F2FE), Color(0xFF0284C7))
            "Slides" -> Triple(Icons.Default.Tv, Color(0xFFFEF3C7), Color(0xFFD97706))
            "Assignment" -> Triple(Icons.Default.Assignment, Color(0xFFECEFDF), Color(0xFF65A30D))
            else -> Triple(Icons.Default.Description, Color(0xFFF3F4F6), JalaramTextSub)
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Type icon & Subject tags
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(colorBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colorFg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = JalaramPrimaryLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = material.subject.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = JalaramPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (currentUserRole == "Admin" || currentUserRole == "Teacher") {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete material",
                            tint = JalaramDanger,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes Title
            Text(
                text = material.title,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = JalaramTextMain,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Topic Name
            Text(
                text = "Topic: ${material.topic}",
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = JalaramPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Short Description
            Text(
                text = material.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = JalaramTextSub,
                    lineHeight = 16.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = JalaramBorder.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Meta Info and Access Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = JalaramTextSub.copy(alpha = 0.6f),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${material.fileSize} • ${material.uploadDate}",
                            fontSize = 10.sp,
                            color = JalaramTextSub
                        )
                    }
                    Text(
                        text = "By: ${material.uploadedBy}",
                        fontSize = 9.sp,
                        color = JalaramTextSub.copy(alpha = 0.8f)
                    )
                }

                Button(
                    onClick = onAccess,
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Access", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@Composable
fun MaterialViewerDialog(
    material: LectureMaterial,
    currentUserRole: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    if (currentUserRole == "Student") {
        val activity = context.findActivity()
        DisposableEffect(Unit) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            onDispose {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    var isDownloading by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    // Generate educational mock text matching subject and topic
    val noteContent = remember(material.subject, material.topic) {
        val topicLower = material.topic.lowercase()
        val subjectLower = material.subject.lowercase()
        
        when {
            topicLower.contains("kinematics") -> {
                """
                ✨ KINEMATICS & PROJECTILE MOTION SUMMARY NOTES
                
                1. BASIC EQUATIONS OF UNIFORM ACCELERATION
                   • v = u + at     (Velocity-Time Relation)
                   • s = ut + ½at²  (Displacement-Time Relation)
                   • v² = u² + 2as  (Velocity-Displacement Relation)
                   • s_n = u + ½a(2n - 1)  (Displacement in nth second)
                
                2. PROJECTILE MOTION DERIVATIONS
                   Let a particle be projected from the origin with initial velocity 'u' at an angle 'θ' to the horizontal axis:
                   • Horizontal component of velocity (u_x) = u·cos(θ)
                   • Vertical component of velocity (u_y) = u·sin(θ)
                   
                   • Time of Flight (T):
                     Total time taken by projectile to return to the horizontal plane.
                     T = (2 · u · sin(θ)) / g
                     
                   • Maximum Height Attained (H):
                     The maximum vertical displacement achieved during flight.
                     H = (u² · sin²(θ)) / (2g)
                     
                   • Horizontal Range (R):
                     The horizontal displacement from starting point to landing point.
                     R = (u² · sin(2θ)) / g
                     
                   • Equation of Trajectory (Parabolic Path):
                     y = x·tan(θ) - (g · x²) / (2 · u² · cos²(θ))
                     
                3. CONCEPTUAL REVISION CHECKLIST
                   [✔] Trajectory is always symmetric in a vacuum.
                   [✔] Acceleration is constant and directed downwards (g = 9.8 m/s²).
                   [✔] At maximum height, vertical velocity is zero, only horizontal velocity remains.
                   [✔] Maximum range is achieved at a projection angle of 45°.
                   
                4. HOT SOLVED PRACTICE PROBLEM
                   Problem: A physics student launches a projectile at an angle of 30° with an initial speed of 40 m/s from flat ground. Find the maximum height and the flight time. (Take g = 10 m/s²)
                   
                   Solution:
                   • Step 1: Calculate Flight Time (T)
                     T = (2 * u * sin(θ)) / g = (2 * 40 * sin(30°)) / 10
                     T = (80 * 0.5) / 10 = 4 seconds
                   • Step 2: Calculate Max Height (H)
                     H = (u² * sin²(θ)) / (2g) = (40² * sin²(30°)) / (2 * 10)
                     H = (1600 * 0.25) / 20 = 400 / 20 = 20 meters.
                   
                   Results: Flight time is 4.0s. Maximum height achieved is 20.0 meters.
                """.trimIndent()
            }
            subjectLower.contains("maths") || topicLower.contains("calculus") || topicLower.contains("integrals") -> {
                """
                ✨ CALCULUS ESSENTIALS - INTEGRALS CHEAT SHEET
                
                1. THE FUNDAMENTAL THEOREM OF CALCULUS
                   If f(x) is continuous on [a, b] and F'(x) = f(x):
                   • ∫[a to b] f(x) dx = F(b) - F(a)
                
                2. BASIC INDEFINITE INTEGRAL LIST
                   • ∫ x^n dx = (x^(n+1)) / (n+1) + C  (for n ≠ -1)
                   • ∫ (1/x) dx = ln|x| + C
                   • ∫ e^x dx = e^x + C
                   • ∫ a^x dx = (a^x) / ln(a) + C
                   • ∫ sin(x) dx = -cos(x) + C
                   • ∫ cos(x) dx = sin(x) + C
                   • ∫ sec²(x) dx = tan(x) + C
                   • ∫ csc²(x) dx = -cot(x) + C
                   • ∫ sec(x)tan(x) dx = sec(x) + C
                
                3. INTEGRATION BY SUBSTITUTION (u-substitution)
                   Useful for converting complex composite functions into standard integrals:
                   ∫ f(g(x)) · g'(x) dx = ∫ f(u) du, where u = g(x) and du = g'(x)dx
                   
                4. INTEGRATION BY PARTS
                   Derived from the product rule of differentiation:
                   ∫ u dv = u·v - ∫ v du
                   Tip for selecting 'u': Use the LIATE rule:
                   L - Logarithmic functions
                   I - Inverse trigonometric functions
                   A - Algebraic functions
                   T - Trigonometric functions
                   E - Exponential functions
                   
                5. STEP-BY-STEP SOLVED EXAMPLE
                   Integrate: ∫ x·cos(x) dx
                   • Step 1: Let u = x (algebraic) and dv = cos(x)dx (trig)
                   • Step 2: Then du = dx, and v = sin(x)
                   • Step 3: Apply rule: uv - ∫ v du
                     = x·sin(x) - ∫ sin(x) dx
                     = x·sin(x) - (-cos(x)) + C
                     = x·sin(x) + cos(x) + C
                """.trimIndent()
            }
            subjectLower.contains("chemistry") || topicLower.contains("bonding") || topicLower.contains("trends") -> {
                """
                ✨ PERIODIC TABLE TRENDS & ATOMIC STRUCTURE SLIDES
                
                1. COVALENT VS IONIC BONDING CORE CONCEPTS
                   • Ionic Bonds: Complete transfer of valence electrons from metals to non-metals. Highly electrostatic. (Typically ΔEN > 1.7)
                   • Covalent Bonds: Mutual sharing of electron pairs between non-metallic atoms to achieve stability. (Typically ΔEN < 1.7)
                   
                2. MAJOR MOLECULAR PERIODIC PROPERTIES & TRENDS
                   As we traverse across a Period (Left to Right):
                   • Atomic Radius: Decreases. Nuclear charge increases, pulling outer electrons tighter.
                   • Ionization Energy: Increases. Harder to remove electrons because they are held closer.
                   • Electronegativity: Increases. Atoms have a stronger pull on bonding electron pairs. (Fluorine is highest at 4.0 on Pauling scale)
                   • Non-Metallic Character: Increases.
                   
                   As we traverse down a Group (Top to Bottom):
                   • Atomic Radius: Increases. New electron shells are added.
                   • Ionization Energy: Decreases. Shielding effect increases, outer electrons are far.
                   • Electronegativity: Decreases.
                   • Metallic Character: Increases.
                   
                3. CHEMICAL STRUCTURAL SHAPES (VSEPR THEORY)
                   • Linear: 2 bonding pairs, 0 lone pairs. Angle = 180° (e.g. CO₂)
                   • Trigonal Planar: 3 bonding pairs, 0 lone pairs. Angle = 120° (e.g. BF₃)
                   • Tetrahedral: 4 bonding pairs, 0 lone pairs. Angle = 109.5° (e.g. CH₄)
                   • Trigonal Pyramidal: 3 bonding pairs, 1 lone pair. Angle = 107° (e.g. NH₃)
                   • Bent / V-Shape: 2 bonding pairs, 2 lone pairs. Angle = 104.5° (e.g. H₂O)
                """.trimIndent()
            }
            subjectLower.contains("biology") || topicLower.contains("cell") || topicLower.contains("cytology") -> {
                """
                ✨ CELL STRUCTURE & ORGANELLES REVISION MANUAL
                
                1. CORE DEFINITIONS
                   • Cell: The structural, functional, and biological unit of all living organisms.
                   • Cytology: The branch of biology concerned with the study of cells.
                
                2. CELL TYPES COMPARED
                   • Prototypic Prokaryotes: No true nucleus or membrane-bound organelles (e.g. Bacteria).
                   • Complex Eukaryotes: Contain a distinct nucleus and specialised organelles (e.g. Plants, Animals).
                   
                   Key Differences:
                   [Plants] Have rigid Cellulose Cell Wall, contain Chloroplasts, possess a large central vacuole.
                   [Animals] No Cell Wall, contain Centrioles for cell division, have small multiple vacuoles.
                   
                3. MAJOR ORGANELLES & FUNCTIONS
                   • Nucleus: The brain of the cell. Houses DNA Chromatin and directs cellular activities.
                   • Mitochondria: "Powerhouse of the cell". Synthesizes energy in the form of ATP molecules.
                   • Chloroplast: Contains green pigment Chlorophyll. Performs photosynthesis in plants.
                   • Ribosomes: Cellular machines that synthesize proteins from genetic RNA templates.
                   • Endoplasmic Reticulum (ER): Rough ER has ribosomes for protein folding. Smooth ER makes lipids.
                   • Lysosomes: Contains digestive enzymes. Cleans up cellular wastes.
                """.trimIndent()
            }
            else -> {
                """
                ✨ GENERAL LECTURE MATERIAL & REVISION NOTES
                
                Subject: ${material.subject}
                Topic: ${material.topic}
                Document Type: ${material.materialType}
                Published By: ${material.uploadedBy}
                
                GENERAL REVISION METHODOLOGY:
                • Read through the conceptual derivations outlined below.
                • Re-solve class illustrations step-by-step before attempting home assignments.
                • Draw neat labeled diagrams for all multi-step derivations in your worksheets.
                • Connect to live video rooms during open student interactive hours if you find any formulas difficult to memorize.
                
                Key study points for exam preparation:
                1. Review fundamental concepts and operational parameters.
                2. Practice at least 10 MCQs from previous diagnostic exams in the portal.
                3. Summarize complex formulas in your own personal pocket cheat sheets.
                4. Maintain consistency in your review schedules for maximum recall speed.
                """.trimIndent()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = material.title,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = JalaramTextMain
                    )
                    Text(
                        text = "${material.subject} • Topic: ${material.topic}",
                        fontSize = 12.sp,
                        color = JalaramPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close viewer")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp)
            ) {
                // Simulating download indicator
                if (isDownloading) {
                    Surface(
                        color = JalaramPrimaryLight,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Downloading ${material.fileSize}...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JalaramPrimary
                                )
                                Text(
                                    text = "${(downloadProgress * 100).toInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = JalaramPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = downloadProgress,
                                color = JalaramPrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Immersive scrollable viewport
                Surface(
                    color = Color(0xFFFAF9F6), // Warm paper-colored sheet
                    border = BorderStroke(1.dp, Color(0xFFE2E2D0)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = noteContent,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF1E293B),
                                lineHeight = 18.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (currentUserRole == "Student") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = JalaramDanger,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Saving, sharing & screen capture are restricted",
                        color = JalaramDanger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Material File Link", material.fileUrl)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Material download link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramAccent),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = JalaramPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Link", color = JalaramPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (isDownloading) return@Button
                            isDownloading = true
                            downloadProgress = 0f
                            coroutineScope.launch {
                                for (i in 1..20) {
                                    delay(100)
                                    downloadProgress = i / 20f
                                }
                                isDownloading = false
                                Toast.makeText(context, "Downloaded '${material.title}.pdf' to device Storage!", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Download PDF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMaterialDialog(
    onDismiss: () -> Unit,
    onUpload: (String, String, String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var fileUrl by remember { mutableStateOf("") }
    var fileSize by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var selectedSubject by remember { mutableStateOf("Physics") }
    var subjectDropdownExpanded by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf("PDF Notes") }
    var typeDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Lecture Material", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Material / Lecture Title") },
                    placeholder = { Text("e.g. Kinematics Advanced Sheet") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown for Subject
                ExposedDropdownMenuBox(
                    expanded = subjectDropdownExpanded,
                    onExpandedChange = { subjectDropdownExpanded = !subjectDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedSubject,
                        onValueChange = {},
                        label = { Text("Mapped Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = subjectDropdownExpanded,
                        onDismissRequest = { subjectDropdownExpanded = false },
                        modifier = Modifier.background(JalaramSurface)
                    ) {
                        listOf("Physics", "Chemistry", "Maths", "Biology", "English", "Informatics").forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    selectedSubject = sub
                                    subjectDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = topic,
                    onValueChange = { topic = it },
                    label = { Text("Topic/Chapter Name") },
                    placeholder = { Text("e.g. Projectile Motion") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown for Material Type
                ExposedDropdownMenuBox(
                    expanded = typeDropdownExpanded,
                    onExpandedChange = { typeDropdownExpanded = !typeDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedType,
                        onValueChange = {},
                        label = { Text("Material Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeDropdownExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeDropdownExpanded,
                        onDismissRequest = { typeDropdownExpanded = false },
                        modifier = Modifier.background(JalaramSurface)
                    ) {
                        listOf("PDF Notes", "Formula Sheet", "Slides", "Assignment").forEach { mType ->
                            DropdownMenuItem(
                                text = { Text(mType) },
                                onClick = {
                                    selectedType = mType
                                    typeDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fileSize,
                        onValueChange = { fileSize = it },
                        label = { Text("File Size") },
                        placeholder = { Text("e.g. 1.8 MB") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fileUrl,
                        onValueChange = { fileUrl = it },
                        label = { Text("File Link") },
                        placeholder = { Text("https://...") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.weight(1.2f)
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Brief Summary") },
                    placeholder = { Text("Provide notes bullet points or revision hints...") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val finalUrl = fileUrl.ifBlank { "https://www.w3schools.com/html/mov_bbb.mp4" }
                        val finalSize = fileSize.ifBlank { "1.5 MB" }
                        val finalDesc = description.ifBlank { "Lecture notes and revision material uploaded by faculty." }
                        onUpload(title, selectedSubject, topic, selectedType, finalUrl, finalSize, finalDesc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Upload & Notify")
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
fun PremiumSyllabusTab(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val coursesList by viewModel.coursesList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var activeDemoVideoUrl by remember { mutableStateOf<String?>(null) }
    var showCreateCourseDialog by remember { mutableStateOf(false) }

    val filteredCourses = remember(coursesList, searchQuery, selectedCategory) {
        coursesList.filter { course ->
            val matchesSearch = course.title.contains(searchQuery, ignoreCase = true) ||
                    course.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || course.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Search & Add Course
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search syllabus or packages...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JalaramPrimary,
                        unfocusedBorderColor = JalaramBorder
                    ),
                    modifier = Modifier.weight(1f)
                )

                if (currentUser?.role == "Admin") {
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { showCreateCourseDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add course")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Category Topic Filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("All", "Physics", "Chemistry", "Maths", "Biology").forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = JalaramPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Courses Grid
            if (filteredCourses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = JalaramTextSub.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No compatible courses found",
                            fontWeight = FontWeight.Bold,
                            color = JalaramTextMain
                        )
                        Text(
                            text = "Try refining your search keyword or selected tag.",
                            color = JalaramTextSub,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 250.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredCourses) { course ->
                        CourseGridCard(
                            course = course,
                            currentUserRole = currentUser?.role ?: "",
                            onDelete = { viewModel.deleteCourse(course.id) },
                            onPlayDemo = { activeDemoVideoUrl = course.videoUrl }
                        )
                    }
                }
            }
        }

        // Active video overlay player dialog/mode
        activeDemoVideoUrl?.let { url ->
            SimulatedVideoPlayerOverlay(
                videoUrl = url,
                onDismiss = { activeDemoVideoUrl = null }
            )
        }

        // Publish course overlay dialog
        if (showCreateCourseDialog) {
            PublishCourseDialog(
                onDismiss = { showCreateCourseDialog = false },
                onPublish = { t, d, p, v, c ->
                    viewModel.publishCourse(t, d, p, v, c)
                    showCreateCourseDialog = false
                    Toast.makeText(context, "Syllabus details published!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun CourseGridCard(
    course: Course,
    currentUserRole: String,
    onDelete: () -> Unit,
    onPlayDemo: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Custom play placeholder cover art with overlay play button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(JalaramPrimaryLight)
                    .clickable { onPlayDemo() },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Play preview video",
                        tint = JalaramPrimary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "WATCH LECTURE DEMO",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = JalaramPrimary,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            // Text Description and Parameters
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = JalaramPrimaryLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = course.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = JalaramPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = "Reviews rating", tint = JalaramWarning, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${course.rating}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = JalaramTextMain
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = course.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = course.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = JalaramTextSub,
                        lineHeight = 16.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "₹${course.price}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = JalaramPrimary
                        )
                    )

                    Row {
                        Button(
                            onClick = onPlayDemo,
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Learn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        if (currentUserRole == "Admin") {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete course", tint = JalaramDanger)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SimulatedVideoPlayerOverlay(
    videoUrl: String,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Lecture Preview Player", fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close player")
                }
            }
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Simulated Video viewport screen frame
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        // Simulated running stream artwork (oscillating graphic or simple loading indication)
                        CircularProgressIndicator(color = JalaramPrimary)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Paused stream",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Streaming URL: $videoUrl",
                    color = JalaramTextSub,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Toggle state",
                            tint = JalaramPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (isPlaying) "Playing lecture stream..." else "Stream Paused",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)) {
                Text("Exit Player")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishCourseDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var videoUrl by remember { mutableStateOf("") }

    var selectedCategory by remember { mutableStateOf("Physics") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish Syllabus Course") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Course Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Course Description") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { if (it.all { char -> char.isDigit() }) priceStr = it },
                        label = { Text("Price (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                        modifier = Modifier.weight(1f)
                    )

                    // Dropdown for Category
                    ExposedDropdownMenuBox(
                        expanded = dropdownExpanded,
                        onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(focusedBorderColor = JalaramPrimary),
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false },
                            modifier = Modifier.background(JalaramSurface)
                        ) {
                            listOf("Physics", "Chemistry", "Maths", "Biology").forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("Lecture Video URL (mp4/yt)") },
                    placeholder = { Text("e.g. https://www.w3schools.com/html/mov_bbb.mp4") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = JalaramPrimary),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceStr.toIntOrNull() ?: 0
                    val v = if (videoUrl.isBlank()) "https://www.w3schools.com/html/mov_bbb.mp4" else videoUrl
                    if (title.isNotBlank() && description.isNotBlank()) {
                        onPublish(title, description, p, v, selectedCategory)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
            ) {
                Text("Publish Now")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
