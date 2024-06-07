package com.bau.socialmediaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// follow adapter sınıfı
class followAdapter (private val messageList: ArrayList<followUser>) : RecyclerView.Adapter<followAdapter.followViewHolder>() {

    class followViewHolder(val binding: View) : RecyclerView.ViewHolder(binding) {
        val nameText: TextView =  itemView.findViewById(R.id.followName)
        val userNameText: TextView = itemView.findViewById(R.id.followUserName)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): followViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.follow_user, parent, false)
        return followViewHolder(itemView)
    }

    // name ve username atama işlemi yapıyoruz
    override fun onBindViewHolder(holder: followViewHolder, position: Int) {
        val currentItem = messageList[position]
        holder.nameText.text = currentItem.nameSurname
        holder.userNameText.text = currentItem.username

    }

    override fun getItemCount() = messageList.size




}