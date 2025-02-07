package com.example.clique

import android.os.Bundle
import android.widget.Button
import android.content.Intent
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MessagesActivity : AppCompatActivity() {

    private lateinit var emailInput: EditText
    private lateinit var addEmailButton: Button
    private lateinit var createChatButton: Button
    private lateinit var emailRecyclerView: RecyclerView
    private lateinit var goToMainPageButton: Button
    private lateinit var chatRecyclerView: RecyclerView

    private val emailList = mutableListOf<String>()
    private lateinit var emailAdapter: EmailAdapter
    private val chatList = mutableListOf<ChatItem>()
    private lateinit var chatAdapter: ChatAdapter

    private val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messages)

        emailInput = findViewById(R.id.emailInput)
        addEmailButton = findViewById(R.id.addEmailButton)
        createChatButton = findViewById(R.id.createChatButton)
        emailRecyclerView = findViewById(R.id.emailRecyclerView)
        goToMainPageButton = findViewById(R.id.goToMainPageButton2)
        chatRecyclerView = findViewById(R.id.chatRecyclerView)

        emailRecyclerView.layoutManager = LinearLayoutManager(this)
        chatRecyclerView.layoutManager = LinearLayoutManager(this)

        emailAdapter = EmailAdapter(emailList)
        emailRecyclerView.adapter = emailAdapter

        chatAdapter = ChatAdapter(chatList)
        chatRecyclerView.adapter = chatAdapter

        addEmailButton.setOnClickListener {
            val email = emailInput.text.toString().trim().replace(".", "_")
            if (email.isNotEmpty()) {
                checkIfEmailExists(email)
            }
        }

        createChatButton.setOnClickListener {
            createChatInFirebase()
        }

        goToMainPageButton.setOnClickListener {
            navigateToMainPage()
        }

        fetchChats()


    }

    private fun checkIfEmailExists(email: String) {
        val emailRef = database.child("User").child(email)

        emailRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(this@MessagesActivity, "Email found, you can use it.", Toast.LENGTH_SHORT).show()

                    emailList.add(email)
                    emailAdapter.notifyDataSetChanged()
                    emailInput.text.clear()
                } else {
                    Toast.makeText(this@MessagesActivity, "Email not found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error checking email: ${error.message}")
            }
        })
    }

    private fun createChatInFirebase() {
        val chatCounterRef = database.child("chatCounter")

        chatCounterRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                var currentCounter = mutableData.getValue(Int::class.java) ?: 0
                mutableData.value = currentCounter + 1
                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (committed) {
                    val chatId = snapshot?.getValue(Int::class.java) ?: return
                    createChatWithId(chatId)
                } else {
                    Log.e("FirebaseError", "Error incrementing chat counter")
                }
            }
        })
    }

    private fun createChatWithId(chatId: Int) {
        val userEmail = UserSession.emailx?.replace(".", "_") ?: return
        if (userEmail.isNotEmpty() && !emailList.contains(userEmail)) {
            emailList.add(userEmail)
        }

        val chatUsers = emailList.associateWith { true }
        val chatData = mapOf("User" to chatUsers)

        database.child("Chats").child(chatId.toString()).setValue(chatData).addOnSuccessListener {
            emailList.forEach { email ->
                val emailWithUnderscore = email.replace(".", "_")
                database.child("User").child(emailWithUnderscore).child("ChatIDs").child(chatId.toString()).setValue(true)
            }

            fetchChats()
        }.addOnFailureListener { exception ->
            Log.e("FirebaseError", "Error creating chat: ${exception.message}")
        }
    }

    private fun fetchChats() {
        val userEmail = UserSession.emailx?.replace(".", "_") ?: return
        val chatsRef = database.child("Chats")

        chatsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatList.clear()

                for (chatSnapshot in snapshot.children) {
                    val chatId = chatSnapshot.key ?: continue
                    val chatUsers = chatSnapshot.child("User")

                    if (chatUsers.hasChild(userEmail)) {
                        val userEmails = mutableListOf<String>()
                        chatUsers.children.forEach { user -> userEmails.add(user.key ?: "") }

                        chatList.add(ChatItem(chatId, userEmails))
                    }
                }
                chatAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MessagesActivity, "Error fetching chats: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
    }
}
