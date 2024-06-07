package com.bau.socialmediaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class commentAdapter (private val messageList: ArrayList<comment>) : RecyclerView.Adapter<commentAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: View) : RecyclerView.ViewHolder(binding) {
        val nameText: TextView =  itemView.findViewById(R.id.commentUserName)
        val contentTextView: TextView = itemView.findViewById(R.id.commentText)
        val timestampTextView: TextView = itemView.findViewById(R.id.timestampText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.rcyc_comment_item, parent, false)
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messageList[position]
        holder.nameText.text = currentItem.name
        holder.contentTextView.text = currentItem.content
        holder.timestampTextView.text = currentItem.timestamp

    }

    override fun getItemCount() = messageList.size




}