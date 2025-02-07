package com.example.clique


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CommentsActivity : AppCompatActivity() {

    private lateinit var commentEditText: EditText
    private lateinit var postCommentButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var commentsAdapter: CommentsAdapter
    private val commentsList = mutableListOf<Comment>()
    private lateinit var goToMainPageButton: Button

    private val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference
    private var postId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        postId = intent.getStringExtra("POST_ID")

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        commentsAdapter = CommentsAdapter(commentsList)
        recyclerView.adapter = commentsAdapter

        commentEditText = findViewById(R.id.commentEditText)
        postCommentButton = findViewById(R.id.postCommentButton)
        goToMainPageButton = findViewById(R.id.goToMainPageButton3)

        postCommentButton.setOnClickListener {
            postComment()
        }

        loadComments()
        goToMainPageButton.setOnClickListener {
            navigateToMainPage()
        }

    }


    private fun loadComments() {
        postId?.let{
        database.child("Post").child(it).child("Comment").get().addOnSuccessListener { snapshot ->
            commentsList.clear()
            snapshot.children.forEach { commentSnapshot ->
                val sender = commentSnapshot.child("sender").value.toString()
                val commentText = commentSnapshot.child("text").value.toString()
                commentsList.add(Comment(sender, commentText))
            }
            commentsAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(this, "Error accured.", Toast.LENGTH_SHORT).show()
        }
      }
    }


    private fun postComment() {
        val commentText = commentEditText.text.toString().trim()
        val sender = UserSession.emailx ?: return

        if (commentText.isEmpty()) {
            Toast.makeText(this, "Comment cannot be empty.", Toast.LENGTH_SHORT).show()
            return
        }

        postId?.let { postId ->
            val commentsRef = database.child("Post").child(postId).child("Comment")
            val commentId = commentsRef.push().key

            val commentData = mapOf(
                "sender" to sender,
                "text" to commentText
            )

            commentId?.let {
                commentsRef.child(it).setValue(commentData)
                    .addOnSuccessListener {
                        updateCommentCount(postId)
                        Toast.makeText(this, "Comment posted.", Toast.LENGTH_SHORT).show()
                        commentEditText.text.clear()
                        loadComments()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to post comment.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun updateCommentCount(postId: String) {
        val postRef = database.child("Post").child(postId)

        postRef.child("CommentCount").get().addOnSuccessListener { snapshot ->
            val currentCount = snapshot.value?.toString()?.toInt() ?: 0
            val newCount = currentCount + 1

            postRef.child("CommentCount").setValue(newCount)
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update comment count.", Toast.LENGTH_SHORT).show()
        }
    }



    private fun navigateToMainPage() {
        val intent = Intent(this, MainPageActivity::class.java)
        startActivity(intent)
    }
}
