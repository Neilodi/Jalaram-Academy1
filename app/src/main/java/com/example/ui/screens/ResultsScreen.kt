package com.example.ui.screens

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ResultRecord
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun ResultsScreen(viewModel: ErpViewModel) {
    val resultsList by viewModel.resultsList.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val filteredResults = remember(resultsList, currentUser) {
        if (currentUser?.role == "Student") {
            resultsList.filter { it.studentId == currentUser?.userId }
        } else {
            resultsList
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Performance Reports",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = JalaramTextMain
                )
            )
            Text(
                text = if (currentUser?.role == "Student") "Your historical assessment performance metrics." else "Database of all student grading reports.",
                color = JalaramTextSub,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = JalaramTextSub.copy(alpha = 0.4f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recorded results available",
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain
                    )
                    Text(
                        text = "Take scheduled quizzes or examinations to generate reports.",
                        color = JalaramTextSub,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredResults) { result ->
                    ResultRecordCard(
                        result = result,
                        currentUserRole = currentUser?.role ?: ""
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRecordCard(
    result: ResultRecord,
    currentUserRole: String
) {
    val percentage = remember(result.score, result.totalQuestions) {
        if (result.totalQuestions > 0) {
            (result.score.toDouble() / result.totalQuestions * 100).toInt()
        } else 0
    }

    val themeColor = if (percentage >= 50) JalaramSuccess else JalaramDanger
    val themeBgColor = if (percentage >= 50) JalaramSuccessContainer else JalaramDangerContainer

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = JalaramSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                // Circle Score percentage display
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(themeBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$percentage%",
                            fontWeight = FontWeight.Black,
                            color = themeColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "SCORE",
                            fontWeight = FontWeight.Bold,
                            color = themeColor.copy(alpha = 0.8f),
                            fontSize = 8.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = result.examTitle,
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextMain,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Score: ${result.score}/${result.totalQuestions} correct answering",
                        color = JalaramTextSub,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )

                    if (currentUserRole != "Student") {
                        Text(
                            text = "Student: ${result.studentName} (ID: ${result.studentId})",
                            color = JalaramPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "Completed: ${result.dateString}",
                        color = JalaramTextSub,
                        fontSize = 10.sp
                    )
                }
            }

            // Performance grading badge
            Surface(
                color = themeBgColor,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (percentage >= 80) "EXCELLENT" else if (percentage >= 50) "PASSED" else "FAIL",
                    color = themeColor,
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}
