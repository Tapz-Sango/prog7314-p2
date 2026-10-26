package com.example.navigo.repository

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository {
    // Lazy-load FirebaseAuth to prevent blocking the main thread on initialization
    private val firebaseAuth: Lazy<FirebaseAuth> = lazy { FirebaseAuth.getInstance() }

    val currentUser get() = firebaseAuth.value.currentUser

    suspend fun register(email: String, pass: String) = try {
        val result = firebaseAuth.value.createUserWithEmailAndPassword(email, pass).await()
        Result.success(result.user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun login(email: String, pass: String) = try {
        val result = firebaseAuth.value.signInWithEmailAndPassword(email, pass).await()
        Result.success(result.user)
    } catch (e: Exception) {
        Result.failure(e)
    }

    // Standard Firebase logout
    fun logout() {
        firebaseAuth.value.signOut()
    }

    // Google SSO logout clearing local cache
    fun logoutGoogle(context: Context, webClientId: String) {
        firebaseAuth.value.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
    }

    suspend fun signInWithGoogle(idToken: String) = try {
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.value.signInWithCredential(credential).await()
        Result.success(result.user)
    } catch (e: Exception) {
        Result.failure(e)
    }
}