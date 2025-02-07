package com.example.clique

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val chatList: List<ChatItem>) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val chatIdTextView: TextView = itemView.findViewById(R.id.chatIdTextView)
        val chatButton: Button = itemView.findViewById(R.id.chatButton)
        val usersEmailTextView: TextView = itemView.findViewById(R.id.usersEmailTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.chat_item, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = chatList[position]
        holder.chatIdTextView.text = chatItem.chatId
        holder.usersEmailTextView.text = "Users: ${chatItem.userEmails.joinToString(", ")}"

        holder.chatButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ChatActivity::class.java)
            intent.putExtra("CHAT_ID", chatItem.chatId)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = chatList.size
}

data class ChatItem(
    val chatId: String,
    val userEmails: List<String>
)

