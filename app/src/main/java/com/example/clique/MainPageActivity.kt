package com.example.clique

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase



class MainPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mainpage)


        val database = Firebase.database("https://clique-6c335-default-rtdb.europe-west1.firebasedatabase.app").reference

        val btnMessages = findViewById<Button>(R.id.btnMessages)
        val btnProfile = findViewById<Button>(R.id.btnProfile)


        btnMessages.setOnClickListener {
            val intent = Intent(this, MessagesActivity::class.java)
            startActivity(intent)
        }

        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }



        val postsLayout = findViewById<LinearLayout>(R.id.postsLayout)

        database.child("Post").get().addOnSuccessListener { snapshot ->
            snapshot.children.forEach { postSnapshot ->
                val postView = layoutInflater.inflate(R.layout.post_item, postsLayout, false)

                val userNameTextView = postView.findViewById<TextView>(R.id.userNameTextView)
                val postTextView = postView.findViewById<TextView>(R.id.postTextView)
                val likeButton = postView.findViewById<Button>(R.id.likeButton)
                val likeCountTextView = postView.findViewById<TextView>(R.id.likeCountTextView)
                val commentCountTextView = postView.findViewById<TextView>(R.id.commentCountTextView)
                val commentButton = postView.findViewById<Button>(R.id.commentButton)

                val postId = postSnapshot.key.toString()
                val userName = postSnapshot.child("user").value.toString()
                val postText = postSnapshot.child("text").value.toString()
                var likesCount = postSnapshot.child("likesCount").value?.toString()?.toInt() ?: 0
                val currentUserEmail = UserSession.emailx ?: return@forEach
                val userHasLiked = postSnapshot.child("likes").child(currentUserEmail).exists()
                var commentCount = postSnapshot.child("CommentCount").value?.toString()?.toInt() ?: 0

                userNameTextView.text = userName
                postTextView.text = postText
                likeCountTextView.text = likesCount.toString()
                commentCountTextView.text = commentCount.toString()

                likeButton.text = if (userHasLiked) "Liked" else "Like"

                val likesRef = database.child("Post").child(postId).child("likes")
                val likesCountRef = database.child("Post").child(postSnapshot.key!!).child("likesCount")

                commentButton.setOnClickListener {
                    val intent = Intent(this, CommentsActivity::class.java)
                    intent.putExtra("POST_ID", postId)
                    startActivity(intent)
                }
                likeButton.setOnClickListener {
                    likesRef.get().addOnSuccessListener { snapshot ->
                        if (snapshot.hasChild(currentUserEmail)) {
                            likesRef.child(currentUserEmail).removeValue().addOnSuccessListener {
                                likesCount -= 1
                                likesCountRef.setValue(likesCount).addOnSuccessListener {
                                    likeCountTextView.text = likesCount.toString()
                                }
                                likeButton.text = "Like"
                                Toast.makeText(this, "You unliked the post.", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to unlike the post.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            likesRef.child(currentUserEmail).setValue(true).addOnSuccessListener {
                                likesCount += 1
                                likesCountRef.setValue(likesCount).addOnSuccessListener {
                                    likeCountTextView.text = likesCount.toString()
                                }
                                likeButton.text = "Liked"
                                Toast.makeText(this, "You liked the post.", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                Toast.makeText(this, "Failed to like the post.", Toast.LENGTH_SHORT).show()
                            }
                        }

                    }.addOnFailureListener {
                        Toast.makeText(this, "Failed to load likes data.", Toast.LENGTH_SHORT).show()
                    }
                }
                postsLayout.addView(postView)
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Posts could not be loaded.", Toast.LENGTH_SHORT).show()
        }



    }

}
