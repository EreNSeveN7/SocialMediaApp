package com.bau.socialmediaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

// takipe dilen kullanıcıların üstüne bastığımızda message screene geçiş yapıyor
class messageUserAdapter(private val messageList: ArrayList<userMessage>) : RecyclerView.Adapter<messageUserAdapter.MessageViewHolder>() {

    // Tıklama olayını dinlemek için arayüz
    interface OnItemClickListener {
        fun onItemClick(documentId: String)
    }

    private var listener: OnItemClickListener? = null

    // Tıklama olayı dinleyicisini ayarlayan yöntem
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    class MessageViewHolder(val binding: View) : RecyclerView.ViewHolder(binding) {
        val nameText: TextView =  itemView.findViewById(R.id.usernameMessage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.message_item_rcyc, parent, false)
        return MessageViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messageList[position]
        holder.nameText.text = currentItem.userName

        holder.itemView.setOnClickListener {
            listener?.onItemClick(currentItem.userId)
        }
    }

    override fun getItemCount() = messageList.size

}
