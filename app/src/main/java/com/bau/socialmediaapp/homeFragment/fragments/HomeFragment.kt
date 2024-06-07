package com.bau.socialmediaapp.homeFragment.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bau.socialmediaapp.MessageScreen
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.homePostAdapter
import com.bau.socialmediaapp.postModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class HomeFragment : Fragment() {
    private lateinit var messageButton: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var rcycView: RecyclerView
    private lateinit var postsList: ArrayList<postModel>
    private lateinit var postAdapter: homePostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        postsList = ArrayList()
        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference
        val userId = auth.currentUser?.uid

        rcycView = view.findViewById(R.id.recyclerHome)
        postAdapter = homePostAdapter(requireContext(), postsList)
        rcycView.adapter = postAdapter
        rcycView.layoutManager = LinearLayoutManager(context)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result

                println(token)

            })
        // following yapılan kullanıcıların postlarını alıyoruz
        val followingIdRef = database.child("users").child("$userId").child("following")
        followingIdRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postsList.clear()
                for (postSnapshot in snapshot.children) {
                    val followingUserId = postSnapshot.getValue(String::class.java)
                    followingUserId?.let { userId ->
                        database.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userDataSnapshot: DataSnapshot) {
                                val username = userDataSnapshot.child("username").getValue(String::class.java)
                                username?.let {
                                    val postsRef = database.child("users").child(userId).child("posts").orderByChild("createdAt")

                                    postsRef.addValueEventListener(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            for (postSnapshot in snapshot.children) {
                                                val imageUrl = postSnapshot.child("imageURL").getValue(String::class.java)
                                                val imageName = postSnapshot.child("caption").getValue(String::class.java)
                                                val postId = postSnapshot.child("postId").getValue(String::class.java)
                                                val createdAt = postSnapshot.child("createdAt").getValue(String::class.java)
                                                val caption = postSnapshot.child("caption").getValue(String::class.java)

                                                if (!imageUrl.isNullOrEmpty() && !imageName.isNullOrEmpty() && !postId.isNullOrEmpty() && !createdAt.isNullOrEmpty() && !caption.isNullOrEmpty()) {
                                                    // Check if the post already exists in the list
                                                    val existingPost = postsList.find { it.postId == postId }
                                                    if (existingPost == null) {
                                                        postsList.add(postModel(postId, followingUserId, username, imageUrl, createdAt,caption))
                                                    }
                                                }
                                            }
                                            // Sort the postsList based on createdAt date in ascending order
                                            postsList.sortByDescending { it.createdAt }

                                            postAdapter.notifyDataSetChanged()
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Handle database error
                                            Toast.makeText(context, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                // Handle database error
                            }
                        })
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(context, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // butona bastığımızda message ekranına geçiş yapıyoruz
        messageButton = view.findViewById(R.id.messageButton)
        messageButton.setOnClickListener{
            val intent = Intent(requireContext(), MessageScreen::class.java)
            startActivity(intent)
        }

        return view
    }
}