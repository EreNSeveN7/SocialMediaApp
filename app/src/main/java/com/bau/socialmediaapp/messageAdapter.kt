package com.bau.socialmediaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth


class messageAdapter(private val messageList: ArrayList<Message>) : RecyclerView.Adapter<messageAdapter.MessageViewHolder>() {

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
        val timeText: TextView = itemView.findViewById(R.id.messageDate)

    }
    //  sender ve receiver olarak ikiye ayırdım eğer güncel kullanıcı id si ise sender oluyor
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == SENDER_VIEW_TYPE){
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sender_message,parent,false))
        }else {
            MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.receiver_message,parent,false))
        }
    }
    override fun getItemViewType(position: Int): Int {
        return if (messageList[position].senderUid == FirebaseAuth.getInstance().currentUser?.uid) SENDER_VIEW_TYPE else RECEIVER_VIEW_TYPE
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messageList[position]
        holder.messageText.text = message.messageContent
        holder.timeText.text = message.timestamp
    }

    override fun getItemCount() = messageList.size



    companion object {
        const val SENDER_VIEW_TYPE = 0
        const val RECEIVER_VIEW_TYPE = 1
    }
}
