package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.ProExam
import com.example.data.ProQuestion
import com.example.data.ProQuestionOption
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProExamScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val activeExam by viewModel.activeProExam.collectAsState()
    val questions by viewModel.currentProQuestions.collectAsState()
    val optionsMap by viewModel.currentProOptions.collectAsState()
    val answers by viewModel.proAnswers.collectAsState()
    val timerSeconds by viewModel.proExamTimer.collectAsState()
    val violations by viewModel.proViolations.collectAsState()
    
    var currentQuestionIdx by remember { mutableStateOf(0) }
    var showSubmitConfirm by remember { mutableStateOf(false) }
    
    // 1. Anti-Screenshot Protection (FLAG_SECURE)
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    
    // 2. App-Leave Detection
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                // User left the app (Backgrounded)
                viewModel.logProViolation("App Leave")
                Toast.makeText(context, "WARNING: App leave detected! Violation logged.", Toast.LENGTH_LONG).show()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    // 3. Prevent Back Press
    BackHandler {
        Toast.makeText(context, "Back press is disabled during exam. Use Submit button.", Toast.LENGTH_SHORT).show()
    }

    if (activeExam == null || questions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = JalaramPrimary)
        }
        return
    }

    val currentQuestion = questions.getOrNull(currentQuestionIdx)
    val currentOptions = optionsMap[currentQuestion?.questionId] ?: emptyList()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(activeExam!!.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(activeExam!!.subject, fontSize = 12.sp, color = Color.Gray)
                    }
                },
                actions = {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(end = 16.dp)) {
                        Text(
                            text = formatTime(timerSeconds),
                            color = if (timerSeconds < 300) Color.Red else JalaramPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (activeExam!!.isStrictMode) {
                            Text("Violations: $violations / ${activeExam!!.maxLeavesAllowed}", fontSize = 10.sp, color = Color.Red)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { if (currentQuestionIdx > 0) currentQuestionIdx-- },
                        enabled = currentQuestionIdx > 0
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Previous")
                    }
                    
                    Button(
                        onClick = { showSubmitConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Final Submit")
                    }
                    
                    TextButton(
                        onClick = { if (currentQuestionIdx < questions.size - 1) currentQuestionIdx++ },
                        enabled = currentQuestionIdx < questions.size - 1
                    ) {
                        Text("Next")
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(JalaramBgMain)
        ) {
            // Question Palette (Miniature)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(40.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.heightIn(max = 120.dp)
            ) {
                items(questions.size) { idx ->
                    val isCurrent = idx == currentQuestionIdx
                    val isAnswered = answers[questions[idx].questionId] != null
                    
                    Box(
                        modifier = Modifier
                            .size(35.dp)
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = if (isCurrent) JalaramPrimary else Color.LightGray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = if (isAnswered) JalaramPrimary.copy(alpha = 0.1f) else Color.White,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { currentQuestionIdx = idx },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (idx + 1).toString(),
                            fontSize = 12.sp,
                            color = if (isCurrent) JalaramPrimary else Color.DarkGray,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            
            Divider()
            
            // Question Content
            if (currentQuestion != null) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    item {
                        Text(
                            text = "Question ${currentQuestionIdx + 1} of ${questions.size}",
                            fontSize = 14.sp,
                            color = JalaramPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 24.sp
                        )
                        Spacer(Modifier.height(24.dp))
                    }
                    
                    items(currentOptions) { option ->
                        val isSelected = answers[currentQuestion.questionId] == option.optionId
                        
                        Card(
                            onClick = { viewModel.saveProAnswer(currentQuestion.questionId, option.optionId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) JalaramPrimary.copy(alpha = 0.15f) else Color.White
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) JalaramPrimary else Color.Transparent
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .border(1.dp, if (isSelected) JalaramPrimary else Color.Gray, CircleShape)
                                        .background(if (isSelected) JalaramPrimary else Color.Transparent, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp), tint = Color.White)
                                    }
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(option.optionText, fontSize = 16.sp)
                            }
                        }
                    }
                    
                    item {
                        Spacer(Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.saveProAnswer(currentQuestion.questionId, null) }) {
                            Text("Clear Response", color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showSubmitConfirm) {
        AlertDialog(
            onDismissRequest = { showSubmitConfirm = false },
            title = { Text("Final Submission") },
            text = { Text("Are you sure you want to end the exam? You cannot change your answers after submission.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirm = false
                        viewModel.submitProExam()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                ) {
                    Text("Submit Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
