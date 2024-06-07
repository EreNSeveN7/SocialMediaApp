package com.bau.socialmediaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.databinding.ActivityFollowScreenBinding
import com.bau.socialmediaapp.databinding.ActivityHomeScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FollowScreen : AppCompatActivity() {
    private lateinit var binding: ActivityFollowScreenBinding
    private  lateinit var followArrayList : ArrayList<followUser>
    private  lateinit var followAdapter : followAdapter
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        followArrayList = ArrayList<followUser>()
        binding.recycFollow.layoutManager = LinearLayoutManager(this)
        followAdapter = followAdapter(followArrayList)
        binding.recycFollow.adapter = followAdapter

        // eğer followinge bastıysak following,followersa bastıysak followrsin verilerini alıyoruz
        val layoutType = intent.getStringExtra("layoutType")
        if (layoutType == "following") {
            fetchUsers("following")
            binding.title.text ="Following"
        } else if (layoutType == "followers") {
            fetchUsers("followers")
            binding.title.text ="Followers"

        }

        binding.backButton.setOnClickListener {
            finish()
        }



    }
    // userların bilgilerini fetch liyoruz
    private fun fetchUsers(s:String) {
        val followingRef = databaseRef.child("users").child(auth.currentUser!!.uid).child(s)

        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                followArrayList.clear()

                for (userIdSnapshot in snapshot.children) {
                    val userId = userIdSnapshot.value // Kullanıcı ID'sini al
                    println(userId)
                    if (userId != null) {
                        // Kullanıcı ID'sini kullanarak kullanıcı verilerini al
                        val userRef = databaseRef.child("users").child(userId.toString())
                        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userDataSnapshot: DataSnapshot) {
                                val name = userDataSnapshot.child("nameSurname").getValue(String::class.java)
                                val username = userDataSnapshot.child("username").getValue(String::class.java)

                                if (name != null && username != null) {
                                    val fUser = followUser(name, username)
                                    followArrayList.add(fUser)
                                }
                                followAdapter.notifyDataSetChanged()
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Hata durumunda bir şeyler yap
                            }
                        })
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda bir şeyler yap
            }
        })
    }


}