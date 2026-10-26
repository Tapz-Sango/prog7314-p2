package com.example.navigo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.content.pm.ApplicationInfo
import androidx.core.view.WindowCompat
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.navigo.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

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

    // Handle the Google sign in intent result and navigate to HomeActivity on success
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
                        Toast.makeText(this@LoginActivity, "Google Sign-In Successful!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        val msg = firebaseResult.exceptionOrNull()?.localizedMessage ?: "Authentication failed"
                        Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.statusBarColor = getColor(R.color.navigo_teal)
        window.navigationBarColor = getColor(R.color.navigo_background)

        setContentView(R.layout.activity_login)

        val tabRegister = findViewById<TextView>(R.id.tabRegister)
        val registerLink = findViewById<TextView>(R.id.tvRegisterLink)
        val loginSSO = findViewById<Button>(R.id.btnLoginSSO)
        val btnDevPreview = findViewById<Button>(R.id.btnDevPreview)

        if ((applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            btnDevPreview.visibility = View.VISIBLE
            btnDevPreview.setOnClickListener {
                startActivity(Intent(this, BiometricActivity::class.java))
            }
        }

        // Trigger the Google sign in flow immediately on click while avoiding main thread locks
        loginSSO.setOnClickListener {
            if (!isClickable) return@setOnClickListener
            isClickable = false

            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        tabRegister.setOnClickListener { openRegister() }
        registerLink.setOnClickListener { openRegister() }
    }

    private fun openRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}