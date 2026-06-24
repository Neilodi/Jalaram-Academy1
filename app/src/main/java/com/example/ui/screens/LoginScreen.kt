package com.example.ui.screens

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
import androidx.compose.ui.platform.testTag
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
    val tempUser by viewModel.tempUser.collectAsState()
    val showOtpVerification by viewModel.showOtpVerification.collectAsState()
    val generatedOtp by viewModel.generatedOtp.collectAsState()
    val headDeviceCount by viewModel.headDeviceCount.collectAsState()
    val headDeviceLimit by viewModel.headDeviceLimit.collectAsState()

    var selectedRole by remember { mutableStateOf("Student") }
    var userIdInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
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
        // Aesthetic Background Canvas lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = JalaramPrimary.copy(alpha = 0.04f),
                radius = 400.dp.toPx(),
                center = Offset(size.width, 0f)
            )
            drawCircle(
                color = JalaramAccent.copy(alpha = 0.05f),
                radius = 300.dp.toPx(),
                center = Offset(0f, size.height)
            )
        }

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
            // Elegant School Crest logo in Compose Canvas / Icon combination
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(JalaramPrimary)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = "Academic School Crest",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Jalaram Academy",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = JalaramTextMain,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enterprise ERP v350 • Titan Build",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = JalaramTextSub
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 450.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = JalaramSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = showOtpVerification,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "auth_screen_transition"
                    ) { isOtpView ->
                        if (!isOtpView) {
                            // Primary Login View
                            Column {
                                Text(
                                    text = "Secure Portal Access",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = JalaramTextMain
                                    ),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                // Role selection spinner/dropdown
                                var expanded by remember { mutableStateOf(false) }
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        readOnly = true,
                                        value = selectedRole,
                                        onValueChange = {},
                                        label = { Text("Select Portal Role") },
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                                            focusedBorderColor = JalaramPrimary,
                                            unfocusedBorderColor = JalaramBorder
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .testTag("login_role_select")
                                    )
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
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
                                                    selectedRole = role
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = userIdInput,
                                    onValueChange = { userIdInput = it },
                                    label = { Text("User ID or Mobile") },
                                    placeholder = { Text("e.g. ADM-001 or 9900881122") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "User credentials icon",
                                            tint = JalaramTextSub
                                        )
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("username_input")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                var pinVisible by remember { mutableStateOf(false) }
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { pinInput = it },
                                    label = { Text("Account PIN / Password") },
                                    placeholder = { Text("Enter 4-digit PIN / password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Password,
                                            contentDescription = "PIN secure icon",
                                            tint = JalaramTextSub
                                        )
                                    },
                                    trailingIcon = {
                                        IconButton(onClick = { pinVisible = !pinVisible }) {
                                            Icon(
                                                imageVector = if (pinVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password visibility"
                                            )
                                        }
                                    },
                                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("password_input")
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        viewModel.login(
                                            userId = userIdInput,
                                            role = selectedRole,
                                            pin = pinInput,
                                            onBiometricRequested = { user ->
                                                val activity = context as? FragmentActivity
                                                if (activity != null) {
                                                    val executor = ContextCompat.getMainExecutor(activity)
                                                    val biometricPrompt = BiometricPrompt(activity, executor,
                                                        object : BiometricPrompt.AuthenticationCallback() {
                                                            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                                                                super.onAuthenticationError(errorCode, errString)
                                                                // Fall back to OTP
                                                                viewModel.proceedToOtp { otp ->
                                                                    Toast.makeText(context, "[SIMULATION MODE]\nSecurity OTP Sent to ALL registered devices! Verification Code: $otp", Toast.LENGTH_LONG).show()
                                                                }
                                                            }

                                                            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                                                super.onAuthenticationSucceeded(result)
                                                                viewModel.completeLoginWithoutOtp()
                                                            }

                                                            override fun onAuthenticationFailed() {
                                                                super.onAuthenticationFailed()
                                                                Toast.makeText(context, "Biometric Authentication failed", Toast.LENGTH_SHORT).show()
                                                            }
                                                        })

                                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                                        .setTitle("Biometric Verification")
                                                        .setSubtitle("Confirm your identity to securely log in")
                                                        .setNegativeButtonText("Use OTP Instead")
                                                        .build()

                                                    biometricPrompt.authenticate(promptInfo)
                                                } else {
                                                    viewModel.proceedToOtp { otp ->
                                                        Toast.makeText(context, "[SIMULATION MODE]\nSecurity OTP Sent to ALL registered devices! Verification Code: $otp", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            },
                                            onOtpRequested = { otp ->
                                                Toast.makeText(
                                                    context,
                                                    "[SIMULATION MODE]\nSecurity OTP Sent to ALL registered devices! Verification Code: $otp",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("login_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = "Secure login symbol"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Generate Security OTP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = onRegisterClick,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = JalaramPrimary),
                                    border = BorderStroke(1.dp, JalaramBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AppRegistration,
                                        contentDescription = "New registration icon"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Request Member Account",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            // OTP View
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    color = JalaramSuccessContainer,
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified status indicator",
                                            tint = JalaramSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "OTP Code Dispatched",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = JalaramSuccess
                                            )
                                        )
                                    }
                                }

                                Text(
                                    text = "Enter Security Code",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = JalaramTextMain
                                    ),
                                    textAlign = TextAlign.Center
                                )

                                Text(
                                    text = "Check your device for the 4-digit code. In simulation, check the toast message for the code.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = JalaramTextSub,
                                        lineHeight = 16.sp
                                    ),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )

                                generatedOtp?.let { code ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = JalaramWarningContainer),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "SIMULATOR CODE: $code",
                                            fontWeight = FontWeight.Bold,
                                            color = JalaramWarning,
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = otpInput,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                            otpInput = it
                                        }
                                    },
                                    label = { Text("Security Variable PIN") },
                                    placeholder = { Text("xxxx") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = "Security key badge",
                                            tint = JalaramTextSub
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JalaramPrimary,
                                        unfocusedBorderColor = JalaramBorder
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input")
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Button(
                                    onClick = {
                                        viewModel.verifyOtp(
                                            otpInput = otpInput,
                                            onSuccess = {
                                                Toast.makeText(context, "Identity verified successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                            }
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = JalaramPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("verify_otp_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm key input icon"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Confirm Identity",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                TextButton(
                                    onClick = { viewModel.cancelOtpFlow() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = JalaramTextSub)
                                ) {
                                    Text(
                                        text = "Return to login screen",
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
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
