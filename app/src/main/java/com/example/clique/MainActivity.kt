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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)


        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all the blanks", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference

            val userRef = database.child("User").child(email.replace(".", "_"))

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val dbPassword = snapshot.child("password").getValue(String::class.java)
                        if (dbPassword == password) {

                                val name = snapshot.child("name").value.toString()
                                val email = snapshot.child("email").value.toString()

                                UserSession.namex = name
                                UserSession.emailx = email

                            Toast.makeText(this@MainActivity, "Login Successful!", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@MainActivity, MainPageActivity::class.java)
                            startActivity(intent)

                        } else {
                            Toast.makeText(this@MainActivity, "Wrong password!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "User is not found!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })


        }
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

    }

}
object UserSession {
    var namex: String? = null
    var emailx: String? = null
}
