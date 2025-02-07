package com.example.clique

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class ChatActivity : AppCompatActivity() {

    private lateinit var chatId: String
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: EditText
    private lateinit var sendButton: Button
    private lateinit var goToMainPageButton: Button
    private val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference

    private lateinit var messageAdapter: MessageAdapter
    private val messageList = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        messagesRecyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        goToMainPageButton = findViewById(R.id.goToMainPageButton4)

        messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        messageAdapter = MessageAdapter(messageList)
        messagesRecyclerView.adapter = messageAdapter

        chatId = intent.getStringExtra("CHAT_ID") ?: return

        loadMessages()

        sendButton.setOnClickListener {
            sendMessage()
        }

        goToMainPageButton.setOnClickListener {
            navigateToMainPage()
        }
    }

    private fun loadMessages() {
        val messageRef = database.child("Chats").child(chatId).child("messages")

        messageRef.get().addOnSuccessListener { snapshot ->
            messageList.clear()

            if (snapshot.exists()) {
                snapshot.children.forEach { messageSnapshot ->
                    val senderEmail = messageSnapshot.child("senderEmail").value?.toString() ?: "Unknown sender"
                    val messageText = messageSnapshot.child("messageText").value?.toString() ?: "No message"

                    Toast.makeText(this, "Fetched message -> sender: $senderEmail, message: $messageText", Toast.LENGTH_SHORT).show()

                    messageList.add(ChatMessage(senderEmail, messageText))
                }

                messageAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this, "No messages found in this chat.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error occurred while loading messages.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun sendMessage() {
        val messageText = messageInput.text.toString().trim()
        val senderEmail = UserSession.emailx ?: return

        if (messageText.isEmpty()) {
            Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        val messageRef = database.child("Chats").child(chatId).child("messages")
        val messageId = messageRef.push().key

        val messageData = mapOf(
            "senderEmail" to senderEmail,
            "messageText" to messageText
        )

        messageId?.let {
            messageRef.child(it).setValue(messageData)
                .addOnSuccessListener {
                    loadMessages()
                    messageInput.text.clear()
                    Toast.makeText(this, "Message sent.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToMainPage() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
    }
}
