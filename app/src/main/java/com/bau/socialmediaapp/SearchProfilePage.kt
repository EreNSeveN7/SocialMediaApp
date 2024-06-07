package com.bau.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.content.Context
import com.bau.socialmediaapp.databinding.ActivitySearchProfilePageBinding
import com.bau.socialmediaapp.homeFragment.ProfileScreen
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database


// search yaptığımız kullanıcının profile sayfası
class SearchProfilePage : AppCompatActivity() {
    private lateinit var binding: ActivitySearchProfilePageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var nameText: TextView
    private lateinit var usernameText: TextView
    private lateinit var postText: TextView
    private lateinit var followersText: TextView
    private lateinit var followingText: TextView

    private lateinit var gridView: GridView
    private lateinit var postAdapter: PostAdapter
    lateinit var postList: ArrayList<gridModel>

    private lateinit var floatButton: FloatingActionButton
    private lateinit var settingsButton: ImageButton


    private lateinit var database: DatabaseReference
    private lateinit var progressBar: ProgressBar
   // private var isFollow: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchProfilePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val userId = intent.getStringExtra("IDD").toString()
        progressBar = view.findViewById(R.id.progressBar)
        postList = ArrayList<gridModel>()

        nameText = view.findViewById(R.id.textName)
        usernameText = view.findViewById(R.id.username)
        postText = view.findViewById(R.id.textPost)
        followersText = view.findViewById(R.id.textFollowers)
        followingText = view.findViewById(R.id.textFollowing)

        gridView = view.findViewById(R.id.gridView)
        postAdapter = PostAdapter(userId,this ,postList)
        gridView.adapter = postAdapter


        auth = Firebase.auth
        database = Firebase.database.reference
        progressBar.visibility = View.VISIBLE // Show progress bar


        val followingRef = database.child("users").child(auth.currentUser!!.uid).child("following")


        // eğer güncel kullanıcı aradığı kullanıcıyı takip edip etmediğini gormek için
        followingRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children) {
                    val userIdFromDatabase = childSnapshot.getValue(String::class.java)
                    if (userIdFromDatabase == userId) {
                        binding.follow.text = "Followed"
                        break
                    }
                    else {
                        binding.follow.text = "Follow"

                    }

                }


            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })



        binding.follow.setOnClickListener{
            followUser(userId)

        }
        binding.back.setOnClickListener {
            finish()
        }

        //arama yaptığımız kullanıcın id sini kullnarak bilgilerini çekiyoruz
        database.child("users").child("$userId").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("nameSurname").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)
                    val post = snapshot.child("numberPosts").getValue(Int::class.java)
                    val followers = snapshot.child("numberFollowers").getValue(Int::class.java)
                    val following = snapshot.child("numberFollowing").getValue(Int::class.java)

                    nameText.text = name
                    usernameText.text = username
                    postText.text = post.toString()
                    followersText.text = followers.toString()
                    followingText.text = following.toString()


                    progressBar.visibility = View.GONE

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Veritabanı okuması iptal edildiğinde
            }
        })
         // arama yaptığımız kullanıcın postalrını çekiyoruz
        val postsRef = database.child("users").child("$userId").child("posts")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // Önceki gönderileri temizle
                for (postSnapshot in snapshot.children) {
                    val imageUrl = postSnapshot.child("imageURL").getValue(String::class.java)
                    val imageName = postSnapshot.child("caption").getValue(String::class.java)
                    val postId = postSnapshot.child("postId").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty() && !imageName.isNullOrEmpty() && !postId.isNullOrEmpty()) {
                        // Eğer imageURL boş değilse, postList'e ekleyelim
                        postList.add(gridModel(postId,imageName, imageUrl))
                    }
                }
                // Veri değiştiğinde adapter'a haber ver
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Veritabanı okuması iptal edildiğinde
            }
        })

    }
    // follow butonuna bastığımızda eğer onceden takip ediyorsa takipten çıakr ve -1 azalır
    // eğer takip etmiyorsa takip eder ve +1 artar
    private fun followUser(userId: String) {
        val currentUser = auth.currentUser
        val followerRef = database.child("users").child(userId).child("followers")
        val followingRef = database.child("users").child(currentUser!!.uid).child("following")


        val currentUserFollowingRef = database.child("users").child(currentUser.uid).child("messageUser")
        val userIdFollowingRef = database.child("users").child(userId).child("messageUser")

        if (binding.follow.text == "Followed") {
            // Unfollow operation
            followingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val userIdFromDatabase = childSnapshot.getValue(String::class.java)
                        if (userIdFromDatabase == userId) {
                            binding.follow.text = "Follow"
                            childSnapshot.ref.removeValue()
                            decrementFollowingCount(currentUser.uid)
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })

            followerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val userIdFromDatabase = childSnapshot.getValue(String::class.java)
                        if (userIdFromDatabase == currentUser.uid) {
                            childSnapshot.ref.removeValue()
                            decrementFollowerCount(userId)
                            break
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        } else if (binding.follow.text == "Follow") {
            // Follow operation
            followerRef.push().setValue(currentUser.uid)
                .addOnSuccessListener {
                    Toast.makeText(this, "Successfully followed the user.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to follow the user: ${e.message}", Toast.LENGTH_SHORT).show()
                }

            currentUserFollowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var isFollowing = false
                    for (followingSnapshot in dataSnapshot.children) {
                        val followingId = followingSnapshot.getValue(String::class.java)
                        if (followingId == userId) {
                            // currentUser zaten userId'yi takip ediyor
                            isFollowing = true
                            break
                        }
                    }
                    if (!isFollowing) {
                        // currentUser'nin userId'yi takip etmediğini belirledik, şimdi ekleme işlemi yapabiliriz
                        currentUserFollowingRef.push().setValue(userId)
                            .addOnSuccessListener {
                                // Başarılı olduğunda yapılacak işlemler
                            }
                            .addOnFailureListener { e ->
                                // Başarısız olduğunda yapılacak işlemler
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Hata durumunda burada işlemler yapılabilir
                }
            })
            userIdFollowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var isFollowing = false
                    for (followingSnapshot in dataSnapshot.children) {
                        val followingId = followingSnapshot.getValue(String::class.java)
                        if (followingId == currentUser.uid) {
                            // currentUser zaten userId'yi takip ediyor
                            isFollowing = true
                            break
                        }
                    }
                    if (!isFollowing) {
                        // currentUser'nin userId'yi takip etmediğini belirledik, şimdi ekleme işlemi yapabiliriz
                        userIdFollowingRef.push().setValue(currentUser.uid)
                            .addOnSuccessListener {
                                // Başarılı olduğunda yapılacak işlemler
                            }
                            .addOnFailureListener { e ->
                                // Başarısız olduğunda yapılacak işlemler
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Hata durumunda burada işlemler yapılabilir
                }
            })
            followingRef.push().setValue(userId)
                .addOnSuccessListener {
                    binding.follow.text = "Followed"
                    incrementFollowingCount(currentUser.uid)
                    incrementFollowerCount(userId)
                    Toast.makeText(this, "Successfully followed the user.", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to follow the user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // decrement fonksiyonu
    private fun decrementFollowingCount(uid: String) {
        val userFollowingRef = database.child("users").child(uid).child("numberFollowing")
        userFollowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numberFollowing = snapshot.getValue(Int::class.java) ?: 0
                if (numberFollowing > 0) {
                    userFollowingRef.setValue(numberFollowing - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun decrementFollowerCount(uid: String) {
        val userFollowerRef = database.child("users").child(uid).child("numberFollowers")
        userFollowerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numberFollowers = snapshot.getValue(Int::class.java) ?: 0
                if (numberFollowers > 0) {
                    userFollowerRef.setValue(numberFollowers - 1)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun incrementFollowingCount(uid: String) {
        val userFollowingRef = database.child("users").child(uid).child("numberFollowing")
        userFollowingRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numberFollowing = snapshot.getValue(Int::class.java) ?: 0
                userFollowingRef.setValue(numberFollowing + 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }

    private fun incrementFollowerCount(uid: String) {
        val userFollowerRef = database.child("users").child(uid).child("numberFollowers")
        userFollowerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val numberFollowers = snapshot.getValue(Int::class.java) ?: 0
                userFollowerRef.setValue(numberFollowers + 1)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }


}