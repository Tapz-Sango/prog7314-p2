package com.example.navigo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.navigo.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private val authRepository by lazy { AuthRepository() }

    // Lazy initialization prevents Google API client builder from locking up the main thread during onCreate
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("918856184962-fvvi7400fenfsm12bifrb8kshuoj67b4.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }

    private var isClickable = true

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isClickable = true
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (idToken != null) {
                lifecycleScope.launch {
                    val firebaseResult = authRepository.signInWithGoogle(idToken)
                    if (firebaseResult.isSuccess) {
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please log in.", Toast.LENGTH_LONG).show()
                        startActivity(
                            Intent(this@RegisterActivity, LoginActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                        finish()
                    } else {
                        val msg = firebaseResult.exceptionOrNull()?.localizedMessage ?: "Registration failed"
                        Toast.makeText(this@RegisterActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_register)

        val tabLogin = findViewById<TextView>(R.id.tabLogin)
        val loginLink = findViewById<TextView>(R.id.tvLoginLink)
        val registerSSO = findViewById<Button>(R.id.btnRegisterSSO)

        tabLogin.setOnClickListener { openLogin() }
        loginLink.setOnClickListener { openLogin() }

        registerSSO.setOnClickListener {
            if (isClickable) {
                isClickable = false
                googleSignInClient.signOut().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                }
            }
        }
    }

    private fun openLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}