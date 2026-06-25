package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ErpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ErpViewModel,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val showOtpVerification by viewModel.showOtpVerification.collectAsState()
    val verificationId by viewModel.verificationId.collectAsState()
    val focusManager = LocalFocusManager.current

    var loginTab by remember { mutableStateOf(0) } // 0: Email, 1: Phone
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var otpInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JalaramPrimaryLight, JalaramBgMain)
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = JalaramPrimary.copy(alpha = 0.04f),
                radius = 400.dp.toPx(),
                center = Offset(size.width, 0f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = null,
                tint = JalaramPrimary,
                modifier = Modifier.size(80.dp)
            )

            Text(
                text = "Jalaram Academy",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = JalaramTextMain
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth().widthIn(max = 450.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    if (!showOtpVerification) {
                        TabRow(
                            selectedTabIndex = loginTab,
                            containerColor = Color.Transparent,
                            contentColor = JalaramPrimary,
                            divider = {}
                        ) {
                            Tab(selected = loginTab == 0, onClick = { loginTab = 0 }) {
                                Text("Email", modifier = Modifier.padding(16.dp))
                            }
                            Tab(selected = loginTab == 1, onClick = { loginTab = 1 }) {
                                Text("Phone", modifier = Modifier.padding(16.dp))
                            }
                            Tab(selected = loginTab == 2, onClick = { loginTab = 2 }) {
                                Text("ID & Role", modifier = Modifier.padding(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        if (loginTab == 0) {
                            // Email Login
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email Address") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Email, null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Lock, null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { 
                                    focusManager.clearFocus()
                                    viewModel.loginWithEmail(emailInput, passwordInput) 
                                })
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.loginWithEmail(emailInput, passwordInput) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Text("Sign In with Email")
                            }
                        } else if (loginTab == 1) {
                            // Phone Login
                            OutlinedTextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                label = { Text("Phone Number") },
                                placeholder = { Text("+91XXXXXXXXXX") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Phone, null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    var activity: android.app.Activity? = context as? android.app.Activity
                                    var currentContext = context
                                    while (activity == null && currentContext is android.content.ContextWrapper) {
                                        currentContext = currentContext.baseContext
                                        activity = currentContext as? android.app.Activity
                                    }
                                    if (activity != null) {
                                        viewModel.startPhoneAuth(phoneInput, activity)
                                    }
                                })
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    var activity: android.app.Activity? = context as? android.app.Activity
                                    var currentContext = context
                                    while (activity == null && currentContext is android.content.ContextWrapper) {
                                        currentContext = currentContext.baseContext
                                        activity = currentContext as? android.app.Activity
                                    }
                                    if (activity != null) {
                                        viewModel.startPhoneAuth(phoneInput, activity)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Text("Send Verification Code")
                            }
                        } else if (loginTab == 2) {
                            var idInput by remember { mutableStateOf("") }
                            var roleInput by remember { mutableStateOf("Student") }
                            var pinInput by remember { mutableStateOf("") }
                            var roleExpanded by remember { mutableStateOf(false) }

                            ExposedDropdownMenuBox(
                                expanded = roleExpanded,
                                onExpandedChange = { roleExpanded = !roleExpanded }
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = roleInput,
                                    onValueChange = {},
                                    label = { Text("Role") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = roleExpanded,
                                    onDismissRequest = { roleExpanded = false },
                                    modifier = Modifier.background(JalaramSurface)
                                ) {
                                    listOf("Student", "Teacher", "Admin", "Head").forEach { role ->
                                        DropdownMenuItem(
                                            text = { Text(role) },
                                            onClick = {
                                                roleInput = role
                                                roleExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = idInput,
                                onValueChange = { idInput = it },
                                label = { Text("User ID") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it },
                                label = { Text("PIN") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    focusManager.clearFocus()
                                    viewModel.login(idInput, roleInput, pinInput,
                                        onBiometricRequested = { user ->
                                            // Optional: Handle biometric if requested
                                        },
                                        onOtpRequested = { 
                                            // Handle OTP if needed
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }),
                                leadingIcon = { Icon(Icons.Default.Lock, null) }
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { 
                                    viewModel.login(idInput, roleInput, pinInput,
                                        onBiometricRequested = { user ->
                                            // Optional: Handle biometric if requested
                                        },
                                        onOtpRequested = { 
                                            // Handle OTP if needed
                                        },
                                        onError = { error ->
                                            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                            ) {
                                Text("Log In with ID & PIN")
                            }
                        }
                    } else {
                        // OTP Verification
                        Text("Verify OTP", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Enter the 6-digit code sent to $phoneInput", color = JalaramTextSub, fontSize = 14.sp)
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        OutlinedTextField(
                            value = otpInput,
                            onValueChange = { if (it.length <= 6) otpInput = it },
                            label = { Text("Verification Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { viewModel.verifyOtpFirebase(otpInput) },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary)
                        ) {
                            Text("Confirm & Log In")
                        }
                        
                        TextButton(
                            onClick = { viewModel.cancelOtpFlow() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel", color = JalaramTextSub)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(onClick = onRegisterClick) {
                Text("Don't have an account? Request access", color = JalaramPrimary)
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Secure Encrypted Communication • Server: Mumbai-1",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = JalaramTextSub.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}
