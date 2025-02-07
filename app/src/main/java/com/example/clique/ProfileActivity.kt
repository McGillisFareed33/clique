package com.example.clique

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {

    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var postEditText: EditText
    private lateinit var addPostButton: Button
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnChangePassword: Button
    private lateinit var goToMainPageButton: Button

    private val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        userNameTextView = findViewById(R.id.userNameTextView)
        userEmailTextView = findViewById(R.id.userEmailTextView)
        postEditText = findViewById(R.id.postEditText)

        addPostButton = findViewById(R.id.addPostButton)
        goToMainPageButton =findViewById(R.id.goToMainPageButton)

        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        userNameTextView.text = UserSession.namex ?: "There is no name information"
        userEmailTextView.text = UserSession.emailx ?: "There is no mail information"

        addPostButton.setOnClickListener {
            addPost()
        }
        goToMainPageButton.setOnClickListener {
            navigateToMainPage()
        }
        btnChangePassword.setOnClickListener {
            changePassword()
        }
    }
    private fun changePassword() {
        val newPassword = etNewPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val currentUserEmail = UserSession.emailx ?: return

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "New passwords do not match.", Toast.LENGTH_SHORT).show()
            return
        }

        database.child("User").child(currentUserEmail).child("password").setValue(newPassword)
            .addOnSuccessListener {
                Toast.makeText(this, "Password changed successfully.", Toast.LENGTH_SHORT).show()
                etNewPassword.text.clear()
                etConfirmPassword.text.clear()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to change password.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun addPost() {
        val postText = postEditText.text.toString()
        val userEmail = UserSession.emailx ?: return
        val userName = UserSession.namex ?: return

        if (postText.isNotEmpty()) {
            val userPostsRef = database.child("Post")

            userPostsRef.get().addOnSuccessListener { snapshot ->
                val postCount = snapshot.childrenCount.toInt() + 1
                val postId = "$postCount"

                val postMap = mapOf(
                    "user"  to userName,
                    "email" to userEmail,
                    "text" to postText
                )

                userPostsRef.child(postId).setValue(postMap).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Post added!", Toast.LENGTH_SHORT).show()
                        postEditText.text.clear()
                    } else {
                        Toast.makeText(this, "Post did not add.", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Error: Post information could not be obtained.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Please write something", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
    }
}
