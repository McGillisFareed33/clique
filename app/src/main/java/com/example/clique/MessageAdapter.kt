package com.example.clique

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val senderTextView: TextView = itemView.findViewById(R.id.senderTextView2)
        val messageTextView: TextView = itemView.findViewById(R.id.messageTextView2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.message_item, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val chatMessage = messages[position]
        holder.senderTextView.text = chatMessage.senderEmail
        holder.messageTextView.text = chatMessage.messageText
    }

    override fun getItemCount(): Int = messages.size
}

data class ChatMessage(
    val senderEmail: String = "",
    val messageText: String = ""
)
