package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

// Global high-contrast theme state
var isAppInDarkMode by mutableStateOf(true) // Default to true (make it dark!)

// Jalaram Academy Brand Theme Colors - Natural Tones Theme
val JalaramPrimary: Color
    get() = if (isAppInDarkMode) Color(0xFF48C99E) else Color(0xFF006B5D) // Forest/Teal Accent Green

val JalaramPrimaryDark: Color
    get() = if (isAppInDarkMode) Color(0xFF00382E) else Color(0xFF005046) // Deep Spruce Green

val JalaramPrimaryLight: Color
    get() = if (isAppInDarkMode) Color(0xFF183129) else Color(0xFFD7E8CD) // Light Sage Green Accent

val JalaramAccent: Color
    get() = if (isAppInDarkMode) Color(0xFFAAB2A6) else Color(0xFF3F493E) // Darker Olive Grey Text/Accent

// Functional Colors - Natural Tones
val JalaramSuccess: Color
    get() = if (isAppInDarkMode) Color(0xFF81C784) else Color(0xFF3F493E)

val JalaramSuccessContainer: Color
    get() = if (isAppInDarkMode) Color(0xFF1C3220) else Color(0xFFDDE5D6)

val JalaramDanger: Color
    get() = if (isAppInDarkMode) Color(0xFFE57373) else Color(0xFFBA1A1A) // Organic Terracotta / Red

val JalaramDangerContainer: Color
    get() = if (isAppInDarkMode) Color(0xFF421D1D) else Color(0xFFFFDAD6) // Peach Cream

val JalaramWarning: Color
    get() = if (isAppInDarkMode) Color(0xFFFFB74D) else Color(0xFF825500) // Warm Ochre

val JalaramWarningContainer: Color
    get() = if (isAppInDarkMode) Color(0xFF4E3200) else Color(0xFFFFE0B2) // Warm Gold Cream

// Neutral Colors - Natural Tones
val JalaramBgMain: Color
    get() = if (isAppInDarkMode) Color(0xFF0F1210) else Color(0xFFF3F4EE) // Pebble/Parchment Sand Warm Cream

val JalaramSurface: Color
    get() = if (isAppInDarkMode) Color(0xFF161A18) else Color(0xFFFFFFFF) // Pure White Paper Surface

val JalaramTextMain: Color
    get() = if (isAppInDarkMode) Color(0xFFFFFFFF) else Color(0xFF1A1C18) // Charcoal Olive Bark

val JalaramTextSub: Color
    get() = if (isAppInDarkMode) Color(0xFFCFD2C6) else Color(0xFF444941) // Mossy Olive Grey

val JalaramBorder: Color
    get() = if (isAppInDarkMode) Color(0xFF282F2A) else Color(0xFFE2E3D8) // Soft Sand Pebble Border


