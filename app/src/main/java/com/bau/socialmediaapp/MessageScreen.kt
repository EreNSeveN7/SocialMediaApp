package com.bau.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bau.socialmediaapp.databinding.ActivityMessageScreenBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class MessageScreen : AppCompatActivity() {
    private lateinit var binding: ActivityMessageScreenBinding
    private lateinit var database: DatabaseReference
    private  lateinit var messageArrayList : ArrayList<userMessage>
    private  lateinit var messageAdapter : messageUserAdapter
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Firebase veritabanı referansını al
        database = FirebaseDatabase.getInstance().reference
        messageArrayList = ArrayList<userMessage>()

        binding.rcycMessage.layoutManager = LinearLayoutManager(this)
        messageAdapter = messageUserAdapter(messageArrayList)
        binding.rcycMessage.adapter = messageAdapter



        binding.backButtonMesssage.setOnClickListener{
            finish()
        }

        // userıd sini alarak geçiş yapıyor
        messageAdapter.setOnItemClickListener(object : messageUserAdapter.OnItemClickListener {
            override fun onItemClick(userId: String) {
                val intent = Intent(this@MessageScreen, MessagePage::class.java)
                intent.putExtra("userId", userId)
                startActivity(intent)
            }
        })


        val followersRef = database.child("users").child(currentUser!!.uid).child("messageUser")


        followersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //messageArrayList.clear() // Önceki verileri temizle
                for (followerSnapshot in snapshot.children) {
                    val followerId = followerSnapshot.getValue(String::class.java)
                    if (followerId != null) {
                        // Her takipçi için kullanıcı bilgilerini al
                        val userRef = database.child("users").child(followerId)

                        userRef.addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                if (userSnapshot.exists()) {
                                    val userId = userSnapshot.child("userId").getValue(String::class.java)
                                    val username = userSnapshot.child("username").getValue(String::class.java)

                                    val user  = userMessage(username!!, userId!!)
                                    messageArrayList.add(user)

                                }
                                messageAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle database error
                                Toast.makeText(
                                    applicationContext,
                                    "Database error: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    applicationContext,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })


    }



}
