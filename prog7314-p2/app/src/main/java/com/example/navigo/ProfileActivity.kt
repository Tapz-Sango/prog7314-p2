package com.example.navigo

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        val root = findViewById<android.view.View>(R.id.profileRoot)
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        etName = findViewById(R.id.etProfileName)
        etEmail = findViewById(R.id.etProfileEmail)
        btnSave = findViewById(R.id.btnSaveProfile)

        loadUserProfile()

        btnSave.setOnClickListener {
            saveUserProfile()
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Pre-fill email from Firebase Auth
        etEmail.setText(currentUser.email ?: "")

        // Fetch user document from Firestore
        db.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val name = document.getString("name") ?: document.getString("fullName") ?: ""
                    etName.setText(name)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load profile data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: User session not found. Please log in again.", Toast.LENGTH_LONG).show()
            return
        }

        val updatedName = etName.text.toString().trim()

        if (updatedName.isEmpty()) {
            etName.error = "Name cannot be empty"
            return
        }

        btnSave.isEnabled = false
        btnSave.text = "Saving..."

        val profileData = hashMapOf(
            "name" to updatedName,
            "email" to (currentUser.email ?: "")
        )

        // Save data to Firestore under the user's UID document
        db.collection("users").document(currentUser.uid)
            .set(profileData, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                btnSave.isEnabled = true
                btnSave.text = "Save Changes"
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving profile: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                btnSave.isEnabled = true
                btnSave.text = "Save Changes"
            }
    }
}