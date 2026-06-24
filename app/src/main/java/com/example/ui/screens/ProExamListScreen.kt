package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ProExam
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProExamListScreen(viewModel: ErpViewModel) {
    val exams by viewModel.proExams.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Live", "Upcoming", "Completed")

    LaunchedEffect(Unit) {
        viewModel.loadProExams()
    }

    val currentTime = System.currentTimeMillis()
    
    val filteredExams = when (selectedTab) {
        0 -> exams.filter { currentTime in it.startTimestamp..it.endTimestamp }
        1 -> exams.filter { currentTime < it.startTimestamp }
        else -> exams.filter { currentTime > it.endTimestamp }
    }

    Column(modifier = Modifier.fillMaxSize().background(JalaramBgMain)) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = JalaramPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = JalaramPrimary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                )
            }
        }

        if (filteredExams.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(16.dp))
                    Text("No exams found in this category", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExams) { exam ->
                    ExamCard(exam = exam, onStart = { viewModel.startProExam(exam) }, isLive = selectedTab == 0)
                }
            }
        }
    }
}

@Composable
fun ExamCard(exam: ProExam, onStart: () -> Unit, isLive: Boolean) {
    val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = JalaramTextMain)
                    Text(exam.subject, color = JalaramPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                if (exam.isStrictMode) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("STRICT", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(icon = Icons.Default.Timer, label = "${exam.durationMinutes} Min")
                InfoItem(icon = Icons.Default.Assignment, label = "${exam.totalMarks} Marks")
            }
            
            Spacer(Modifier.height(12.dp))
            
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Starts: ${sdf.format(Date(exam.startTimestamp))}", fontSize = 12.sp, color = Color.Gray)
                    Text("Ends: ${sdf.format(Date(exam.endTimestamp))}", fontSize = 12.sp, color = Color.Gray)
                }
                
                if (isLive) {
                    Button(
                        onClick = onStart,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                    ) {
                        Text("Start Exam")
                    }
                } else if (System.currentTimeMillis() < exam.startTimestamp) {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Coming Soon")
                    }
                } else {
                    Text("Completed", color = Color.Gray, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 14.sp, color = Color.DarkGray)
    }
}
