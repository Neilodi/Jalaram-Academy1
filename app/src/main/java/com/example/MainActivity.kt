package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.platform.LocalContext
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.isAppInDarkMode
import com.example.viewmodel.ErpViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: ErpViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = isAppInDarkMode) {
                val currentUser by viewModel.currentUser.collectAsState()
                var showRegister by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize().preventHackClicks(viewModel)) {
                    if (currentUser == null) {
                        if (showRegister) {
                            RegisterScreen(
                                viewModel = viewModel,
                                onCancel = { showRegister = false }
                            )
                        } else {
                            LoginScreen(
                                viewModel = viewModel,
                                onRegisterClick = { showRegister = true }
                            )
                        }
                    } else {
                        MainShellScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun Modifier.preventHackClicks(viewModel: ErpViewModel): Modifier {
    val context = LocalContext.current
    val hackPreventionEnabled by viewModel.hackPreventionEnabled.collectAsState()

    if (!hackPreventionEnabled) return this

    return this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent()
                val isRightClick = event.buttons.isSecondaryPressed
                if (isRightClick) {
                    event.changes.forEach { it.consume() }
                    Toast.makeText(context, "⚠️ Security Alert: Right-Click actions are restricted!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

