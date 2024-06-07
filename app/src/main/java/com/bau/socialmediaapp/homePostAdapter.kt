package com.bau.socialmediaapp

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.util.Calendar
import java.util.Locale

class homePostAdapter(private val context: Context, private val dataList: ArrayList<postModel>) :
    RecyclerView.Adapter<homePostAdapter.ViewHolder>() {
    private  var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private  var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.textView8)
        val createdAt: TextView = itemView.findViewById(R.id.textView9)
        val caption: TextView = itemView.findViewById(R.id.textCaption)
        val image: ImageView = itemView.findViewById(R.id.imageView15)
        val likeButton : ImageButton  = itemView.findViewById(R.id.imageButton4)
        val showCommentButton : ImageButton  = itemView.findViewById(R.id.imageButton5)
        var isLiked: Boolean = false // Store the like state for each item
        val textLike: TextView = itemView.findViewById(R.id.textLike)
        fun setImage(imageUrl: String) {
            image.post {
                val width = image.width
                val height = image.height
                if (width > 0 && height > 0) {
                    Picasso.get()
                        .load(imageUrl)
                        .resize(width, height)
                        .centerCrop()
                        .transform(RoundedTransformation(15, 0))
                        .into(image)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_item_rcyc, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = dataList[position]
        holder.userName.text = currentItem.userName
        holder.caption.text = currentItem.caption
        val postDateTime = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).parse(currentItem.createdAt)
        val postDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(postDateTime)
        val postTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(postDateTime)

        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val postYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(postDateTime).toInt()

        if (currentYear != postYear) {
            // Farklı yıl, tam tarih
            holder.createdAt.text = postDate
        } else {
            // Aynı yıl, kontrol et ve göster
            val postDay = SimpleDateFormat("dd", Locale.getDefault()).format(postDateTime).toInt()
            val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

            if (postDay == currentDay) {
                // Aynı gün, sadece saat
                holder.createdAt.text = postTime
            } else {
                // Farklı gün, gün ve ay
                holder.createdAt.text = SimpleDateFormat("dd/MM", Locale.getDefault()).format(postDateTime)
            }
        }
        holder.setImage(currentItem.image)


        val currentUserId = auth.currentUser?.uid
        val postId = currentItem.postId
        val likeRef = database.child("users").child(currentUserId!!).child("isLiked")

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                holder.isLiked = false // Reset like state for each update
                for (childSnapshot in snapshot.children) {
                    val postIdFromDatabase = childSnapshot.getValue(String::class.java)
                    if (postIdFromDatabase == postId) {
                        holder.isLiked = true // Set like state to true if postId is found
                        break
                    }
                }
                // Update like button color based on the like state
                if (holder.isLiked) {
                    holder.likeButton.setColorFilter(Color.RED)
                } else {
                    holder.likeButton.clearColorFilter()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
         // post likes ı alıyoruz
        val postLikeText = database.child("users").child(currentItem.followingUserId).child("posts").child(postId)
            .child("likes")
        postLikeText.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                holder.textLike.text = currentLikes.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
        // Handle like button click
        holder.likeButton.setOnClickListener {
            val isCurrentlyLiked = holder.isLiked // Store the current like status
            val likeRef = database.child("users").child(currentUserId).child("isLiked")

            if (isCurrentlyLiked) {
                // Remove the postId from "isLiked" list if previously liked
                likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            val postIdFromDatabase = childSnapshot.getValue(String::class.java)
                            if (postIdFromDatabase == postId) {
                                childSnapshot.ref.removeValue()
                                break
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })

                // like countu azaltıyoruz
                val postLikeRef = database.child("users").child(currentItem.followingUserId).child("posts").child(postId)
                    .child("likes")
                postLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                        postLikeRef.setValue(currentLikes - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            } else {

                likeRef.push().setValue(postId)

                // like countu arttırıyouz
                val postLikeRef = database.child("users").child(currentItem.followingUserId).child("posts").child(postId)
                    .child("likes")
                postLikeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                        postLikeRef.setValue(currentLikes + 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }
        }

        // comment screen e geçiş yapıyoruz
        holder.showCommentButton.setOnClickListener{
            val intent = Intent(context, CommentsScreen::class.java)
            intent.putExtra("postIDD", postId)
            intent.putExtra("followerId", currentItem.followingUserId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}
