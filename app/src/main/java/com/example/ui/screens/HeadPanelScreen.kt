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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@Composable
fun HeadPanelScreen(viewModel: ErpViewModel) {
    val context = LocalContext.current
    val headDeviceCount by viewModel.headDeviceCount.collectAsState()
    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    var typedDeviceCount by remember(headDeviceCount) { mutableStateOf(headDeviceCount.toString()) }
    var typedDeviceLimit by remember(headDeviceLimit) { mutableStateOf(headDeviceLimit.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(JalaramBgMain)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome Header card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JalaramSurface),
            border = BorderStroke(1.dp, JalaramBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(JalaramPrimary, JalaramAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Executive Security",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column {
                    Text(
                        text = "Head Executive Console",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = JalaramPrimary
                    )
                    Text(
                        text = "Manage system directory availability, restrict login counts, and secure executive roles.",
                        fontSize = 12.sp,
                        color = JalaramTextSub
                    )
                }
            }
        }

        // Live status metrics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Logged-in devices Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramPrimaryLight.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, JalaramPrimary.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Devices,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Active Logged Devices",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextSub
                    )
                    Text(
                        text = "$headDeviceCount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JalaramPrimary
                    )
                }
            }

            // Max allowed limit Card
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramAccent.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, JalaramAccent.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = JalaramPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Maximum Security Limit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JalaramTextSub
                    )
                    Text(
                        text = "$headDeviceLimit",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = JalaramPrimary
                    )
                }
            }
        }

        // Status Indicator Warning Box
        val isLocked = headDeviceCount >= headDeviceLimit
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocked) JalaramDanger.copy(alpha = 0.1f) else JalaramSuccess.copy(alpha = 0.1f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isLocked) JalaramDanger.copy(alpha = 0.3f) else JalaramSuccess.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isLocked) JalaramDanger else JalaramSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isLocked) "Registration & Logins Locked" else "Registration & Logins Open",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isLocked) JalaramDanger else JalaramSuccess
                        )
                        Text(
                            text = if (isLocked) {
                                "The active devices ($headDeviceCount) have met or exceeded the limit ($headDeviceLimit). The Head role is hidden on login/registration forms."
                            } else {
                                "Under the current limit ($headDeviceLimit), the Head role is fully accessible for registration and verification logins."
                            },
                            fontSize = 11.sp,
                            color = JalaramTextSub
                        )
                    }
                }
            }
        }

        // Configuration Control Fields Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = JalaramSurface),
            border = BorderStroke(1.dp, JalaramBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configure Security Thresholds",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = JalaramTextMain
                )

                // 1. TYPING ACTIVE DEVICE COUNT
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Set Active Logged-In Devices:",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = JalaramTextMain
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedDeviceCount,
                            onValueChange = { typedDeviceCount = it },
                            placeholder = { Text("e.g. 3") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JalaramPrimary,
                                unfocusedBorderColor = JalaramBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("head_device_count_input")
                        )
                        Button(
                            onClick = {
                                val count = typedDeviceCount.toIntOrNull()
                                if (count != null && count >= 0) {
                                    viewModel.setHeadDeviceCount(count)
                                    Toast.makeText(context, "Active logged-in devices updated to $count", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid non-negative number", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 2. TYPING MAXIMUM DEVICE LIMIT (Defaults to 3, but fully scaleable!)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Set Maximum Device Limit (for now keep it 3):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = JalaramTextMain
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = typedDeviceLimit,
                            onValueChange = { typedDeviceLimit = it },
                            placeholder = { Text("e.g. 3") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JalaramPrimary,
                                unfocusedBorderColor = JalaramBorder
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("head_device_limit_input")
                        )
                        Button(
                            onClick = {
                                val limit = typedDeviceLimit.toIntOrNull()
                                if (limit != null && limit >= 1) {
                                    viewModel.setHeadDeviceLimit(limit)
                                    Toast.makeText(context, "Maximum login limit scaled to $limit", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid number (minimum 1)", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }
        }
    }
}
