package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

data class ErpRoute(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val allowedRoles: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen(viewModel: ErpViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeProExam by viewModel.activeProExam.collectAsState()

    if (activeProExam != null) {
        ProExamScreen(viewModel = viewModel)
        return
    }

    val routes = remember {
        listOf(
            ErpRoute("dashboard", "Dashboard", Icons.Default.Dashboard, listOf("Admin", "Teacher", "Student")),
            ErpRoute("head_panel", "Head Controls", Icons.Default.Security, listOf("Head")),
            ErpRoute("courses", "Course Library", Icons.Default.MenuBook, listOf("Admin", "Teacher", "Student")),
            ErpRoute("batches", "My Batches", Icons.Default.MeetingRoom, listOf("Admin", "Teacher")),
            ErpRoute("gradebook", "Gradebook", Icons.Default.AssignmentTurnedIn, listOf("Admin", "Teacher")),
            ErpRoute("users", "User Management", Icons.Default.Group, listOf("Admin")),
            ErpRoute("exams", "Examinations", Icons.Default.Assignment, listOf("Admin", "Teacher", "Student")),
            ErpRoute("results", "Performance", Icons.Default.BarChart, listOf("Admin", "Teacher", "Student")),
            ErpRoute("live", "Live Classroom", Icons.Default.Videocam, listOf("Admin", "Teacher", "Student")),
            ErpRoute("calendar", "Calendar", Icons.Default.CalendarMonth, listOf("Admin", "Teacher", "Student")),
            ErpRoute("chats", "Academy Chats", Icons.Default.Forum, listOf("Admin", "Teacher", "Student", "Head"))
        )
    }

    val visibleRoutes = remember(currentUser, routes) {
        routes.filter { route ->
            currentUser?.role in route.allowedRoles
        }
    }

    var showProfileDialog by remember { mutableStateOf(false) }

    if (showProfileDialog && currentUser != null) {
        var isBiometricEnabled by remember { mutableStateOf(currentUser!!.isBiometricEnabled) }
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = { Text("My Profile") },
            text = {
                Column {
                    Text("Name: ${currentUser!!.name}", fontWeight = FontWeight.Bold)
                    Text("Role: ${currentUser!!.role}")
                    Text("ID: ${currentUser!!.userId}")
                    Spacer(modifier = Modifier.height(16.dp))
                    if (currentUser!!.role == "Student") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enable Biometric Login")
                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { isBiometricEnabled = it }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (currentUser!!.role == "Student") {
                        viewModel.editUserByAdmin(
                            userId = currentUser!!.userId,
                            name = currentUser!!.name,
                            mobile = currentUser!!.mobile,
                            parentMobile = currentUser!!.parentMobile,
                            batch = currentUser!!.batch,
                            subjects = currentUser!!.subjects,
                            isBiometricEnabled = isBiometricEnabled
                        )
                    }
                    showProfileDialog = false
                }) {
                    Text("Save & Close")
                }
            }
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth > 700.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Desktop Sidebar
            if (isWideScreen) {
                SidebarContent(
                    visibleRoutes = visibleRoutes,
                    currentTab = currentTab,
                    currentUserRole = currentUser?.role ?: "",
                    onNavigate = { viewModel.setTab(it) },
                    onLogout = { viewModel.logout() }
                )
            }

            // Central Content Container
            Scaffold(
                topBar = {
                    TopBarHeader(
                        currentUser = currentUser?.name ?: "Visitor",
                        currentUserRole = currentUser?.role ?: "Visitor",
                        onProfileClick = { showProfileDialog = true },
                        onLogout = { viewModel.logout() },
                        onLiveClassClick = { viewModel.setTab("live") }
                    )
                },
                bottomBar = {
                    if (!isWideScreen) {
                        MobileBottomBar(
                            visibleRoutes = visibleRoutes,
                            currentTab = currentTab,
                            onNavigate = { viewModel.setTab(it) }
                        )
                    }
                },
                containerColor = JalaramBgMain,
                modifier = Modifier.weight(1f)
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        "dashboard" -> DashboardScreen(viewModel = viewModel)
                        "courses" -> CoursesScreen(viewModel = viewModel)
                        "batches" -> BatchesScreen(viewModel = viewModel)
                        "gradebook" -> GradebookScreen(viewModel = viewModel)
                        "users" -> UserManagementScreen(viewModel = viewModel)
                        "exams" -> {
                            if (currentUser?.role == "Student") {
                                ProExamListScreen(viewModel = viewModel)
                            } else {
                                ProExamManagerScreen(viewModel = viewModel)
                            }
                        }
                        "results" -> ResultsScreen(viewModel = viewModel)
                        "live" -> LiveClassScreen(viewModel = viewModel)
                        "calendar" -> CalendarScreen(viewModel = viewModel)
                        "chats" -> ChatsScreen(viewModel = viewModel)
                        "head_panel" -> HeadPanelScreen(viewModel = viewModel)
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Module Under Construction")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SidebarContent(
    visibleRoutes: List<ErpRoute>,
    currentTab: String,
    currentUserRole: String,
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(),
        color = JalaramSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, JalaramBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            // Academy Logo Branding Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JalaramPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Jalaram",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = JalaramPrimary,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "Academy ERP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = JalaramTextSub,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Navigation Route buttons list
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                visibleRoutes.forEach { route ->
                    val isSelected = currentTab == route.id
                    val itemBg = if (isSelected) JalaramPrimary else Color.Transparent
                    val itemContentColor = if (isSelected) Color.White else JalaramTextMain

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(itemBg)
                            .clickable { onNavigate(route.id) }
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = route.icon,
                            contentDescription = route.title,
                            tint = itemContentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = route.title,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = itemContentColor,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout button in drawer footer
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = JalaramDanger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = "Logout action"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Secure Logout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarHeader(
    currentUser: String,
    currentUserRole: String,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit,
    onLiveClassClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "Jalaram Academy",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Academic Session 2026",
                    style = MaterialTheme.typography.labelSmall.copy(color = JalaramTextSub)
                )
            }
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(end = 16.dp)
            ) {
                // Top-right Live Class Button
                Button(
                    onClick = onLiveClassClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JalaramPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("top_bar_live_class_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Live Class Stream",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Live Class",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Theme Toggle Button
                IconButton(onClick = { isAppInDarkMode = !isAppInDarkMode }) {
                    Icon(
                        imageVector = if (isAppInDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme Mode",
                        tint = JalaramTextSub
                    )
                }

                // Bell alert status symbol
                Box(modifier = Modifier.padding(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notice notifications",
                        tint = JalaramTextSub
                    )
                }

                // Profile card credentials label
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = currentUser,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = JalaramTextMain
                    )
                    Surface(
                        color = JalaramPrimaryLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = currentUserRole.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = JalaramPrimary,
                                fontSize = 9.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Circle identifier graphic avatar (opens profile dialog)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDDE5D6))
                        .border(1.5.dp, Color(0xFFBBC9B8), CircleShape)
                        .clickable { onProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.firstOrNull()?.toString()?.uppercase() ?: "U",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3F493E),
                        fontSize = 16.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = JalaramSurface)
    )
}

@Composable
fun MobileBottomBar(
    visibleRoutes: List<ErpRoute>,
    currentTab: String,
    onNavigate: (String) -> Unit
) {
    // Pick top 4 routes to fit mobile interface cleanly
    val mobilePriorityRoutes = remember(visibleRoutes) {
        visibleRoutes.take(4)
    }

    NavigationBar(
        containerColor = JalaramSurface
    ) {
        mobilePriorityRoutes.forEach { route ->
            val isSelected = currentTab == route.id
            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(route.id) },
                icon = {
                    Icon(
                        imageVector = route.icon,
                        contentDescription = route.title
                    )
                },
                label = { Text(route.title, fontWeight = FontWeight.SemiBold, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = JalaramPrimary,
                    indicatorColor = JalaramPrimary,
                    unselectedIconColor = JalaramTextSub,
                    unselectedTextColor = JalaramTextSub
                )
            )
        }
    }
}
