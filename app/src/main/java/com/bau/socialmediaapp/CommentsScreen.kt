package com.bau.socialmediaapp

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bau.socialmediaapp.databinding.ActivityCommentsScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Calendar
import java.util.Locale

class CommentsScreen : AppCompatActivity() {
    private lateinit var binding: ActivityCommentsScreenBinding
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    private lateinit var alertDialog: AlertDialog // AlertDialog değişkeni burada tanımlanıyor

    private  lateinit var commentArrayList : ArrayList<comment>
    private  lateinit var commentAdapter : commentAdapter
    private var username: String? = null
    private var followerUserId: String? = null
    private var postId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommentsScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        postId = intent.getStringExtra("postIDD")
        followerUserId = intent.getStringExtra("followerId")
        commentArrayList = ArrayList<comment>()
        binding.rcycCommentScreen.layoutManager = LinearLayoutManager(this)
        commentAdapter = commentAdapter(commentArrayList)
        binding.rcycCommentScreen.adapter = commentAdapter


        binding.back.setOnClickListener {
            finish()
        }

        binding.floatAddComment.setOnClickListener{
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.messages_text, null)
            val alertDialogBuilder = AlertDialog.Builder(this)

            alertDialogBuilder.setTitle("Add a comment")
            alertDialogBuilder.setView(dialogView)

            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val sendButton = dialogView.findViewById<Button>(R.id.sendButton)
            val commentEditText = dialogView.findViewById<EditText>(R.id.message)

            val currentTime = Calendar.getInstance().timeInMillis // Telefonun yerel saat bilgisi
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(currentTime)

            val userRef = databaseRef.child("users").child(auth.currentUser!!.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    username = snapshot.child("username").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

            cancelButton.setOnClickListener {
                // AlertDialog kapatma
                println("cancel butonuna basıldı")
                alertDialog.dismiss()
            }
            // send e bastığımızda veritabanına yeni bir comment ref olusturur
            sendButton.setOnClickListener {
                val commentText = commentEditText.text.toString()
                val commentRef = databaseRef.child("users").child(followerUserId ?: "").child("posts").child(postId ?: "").child("comments").push()
                val commentData = hashMapOf(
                    "username" to username,
                    "userId" to auth.currentUser!!.uid,
                    "comment" to commentText,
                    "createdAt" to formattedDate
                )

                commentRef.setValue(commentData)
                    .addOnSuccessListener {
                        // Başarılı bir şekilde eklendiğinde yapılacak işlemler
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss() // AlertDialog'ı kapat
                    }
                    .addOnFailureListener {
                        // Ekleme işlemi başarısız olduğunda yapılacak işlemler
                        Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                    }
            }



        // AlertDialog'u oluştur ve göster
        alertDialog = alertDialogBuilder.create()
        alertDialog.show()
        }



        fetchComments()

    }

    // commentleri fetchliyoruz
    private fun fetchComments() {

        val commentsRef = databaseRef.child("users").child(followerUserId ?: "").child("posts").child(postId ?: "").child("comments")

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentArrayList.clear() // Mevcut yorum listesini temizle

                for (commentSnapshot in snapshot.children) {
                    val name = commentSnapshot.child("username").getValue(String::class.java)
                    val comment = commentSnapshot.child("comment").getValue(String::class.java)
                    val commentTimestamp = commentSnapshot.child("createdAt").getValue(String::class.java)

                    if (name != null && comment != null && commentTimestamp != null) {
                        val comment = comment(name, comment, commentTimestamp)
                        commentArrayList.add(comment)
                    }
                }
                commentAdapter.notifyDataSetChanged() // Adaptörü güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda bir şeyler yap
            }
        })
    }
}