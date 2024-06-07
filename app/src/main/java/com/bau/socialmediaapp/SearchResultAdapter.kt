package com.bau.socialmediaapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class SearchResultAdapter(private val userList: List<User>) : RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {
    private var onItemClickListener: ((User) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_search, parent, false)
        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)

        holder.itemView.setOnClickListener {
            onItemClickListener?.invoke(user)
            val intent = Intent(holder.itemView.context, SearchProfilePage::class.java)
            println(user.userId)
            intent.putExtra("IDD", user.userId) // veya diğer kullanıcı bilgileri
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class SearchResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val nameSurnameTextView: TextView = itemView.findViewById(R.id.nameSurnameTextView)

        fun bind(user: User) {
            usernameTextView.text = user.username
            nameSurnameTextView.text = user.nameSurname

        }

    }

}
