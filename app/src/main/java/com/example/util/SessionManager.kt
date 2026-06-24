package com.example.util

import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.data.User
import com.google.gson.Gson

class SessionManager(context: Context) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_session_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val deviceId: String = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    private val gson = Gson()

    fun saveUserSession(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit().apply {
            putString(KEY_USER_DATA, userJson)
            putString(KEY_DEVICE_ID, deviceId)
            putBoolean(KEY_IS_LOGGED_IN, true)
            putLong(KEY_LAST_LOGIN, System.currentTimeMillis())
            apply()
        }
    }

    fun getUserSession(): User? {
        val userJson = prefs.getString(KEY_USER_DATA, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getDeviceId(): String = deviceId

    fun logout() {
        prefs.edit().clear().apply()
    }

    fun isDeviceBound(): Boolean {
        val storedDeviceId = prefs.getString(KEY_DEVICE_ID, null)
        return storedDeviceId == deviceId
    }

    companion object {
        private const val KEY_USER_DATA = "user_data"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LAST_LOGIN = "last_login"
    }
}
