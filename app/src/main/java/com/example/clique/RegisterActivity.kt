package com.example.clique

import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        lateinit var database: DatabaseReference
        val etName = findViewById<EditText>(R.id.etName2)
        val etEmail = findViewById<EditText>(R.id.etEmail2)
        val etPassword = findViewById<EditText>(R.id.etPassword2)
        val btnRegister = findViewById<Button>(R.id.btnRegister2)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim().replace(".", "_")
            val password = etPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the blanks.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference

            database.child("User").child(email).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        Toast.makeText(this@RegisterActivity, "Email already exists. Please choose another one.", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = User(name, email, password)

                        database.child("User").child(email).setValue(user).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@RegisterActivity, "Register Successful!", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                            } else {
                                Toast.makeText(this@RegisterActivity, "Register unsuccessful: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@RegisterActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}
data class User(
    val name: String = "",
    val email: String = "",
    val password: String = ""
)
