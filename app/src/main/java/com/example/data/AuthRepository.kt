package com.example.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit

class AuthRepository {
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    suspend fun signInWithEmail(email: String, password: String): User? {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return null
            fetchUserFromFirestore(firebaseUser.uid)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun fetchUserFromFirestore(uid: String): User? {
        return try {
            val doc = firestore.collection("users").document(uid).get().await()
            if (doc.exists()) {
                // Mapping Firestore document to User object
                User(
                    userId = doc.getString("userId") ?: uid,
                    name = doc.getString("name") ?: "Unknown",
                    role = doc.getString("role") ?: "Student",
                    mobile = doc.getString("mobile") ?: "",
                    pin = "", // PIN is usually local or handled separately
                    status = doc.getString("status") ?: "Active"
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun sendOtp(phoneNumber: String, activity: Activity, callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    suspend fun signInWithOtp(verificationId: String, otpCode: String): User? {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return null
            fetchUserFromFirestore(firebaseUser.uid)
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
