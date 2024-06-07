package com.bau.socialmediaapp.homeFragment.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bau.socialmediaapp.CreatePost
import com.bau.socialmediaapp.FollowScreen
import com.bau.socialmediaapp.PostAdapter
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.gridModel
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

class ProfileFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var nameText: TextView
    private lateinit var usernameText: TextView
    private lateinit var postText: TextView
    private lateinit var followersText: TextView
    private lateinit var followingText: TextView
    private lateinit var followingLayout: LinearLayout
    private lateinit var followersLayout: LinearLayout

    private lateinit var gridView: GridView
    private lateinit var postAdapter: PostAdapter
    lateinit var postList: ArrayList<gridModel>

    private lateinit var floatButton: FloatingActionButton
    private lateinit var settingsButton: ImageButton


    private lateinit var database: DatabaseReference
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        progressBar = view.findViewById(R.id.progressBar)
        postList = ArrayList<gridModel>()
        auth = Firebase.auth
        val userId = auth.currentUser?.uid

        nameText = view.findViewById(R.id.textName)
        usernameText = view.findViewById(R.id.username)
        postText = view.findViewById(R.id.textPost)
        followersText = view.findViewById(R.id.textFollowers)
        followingText = view.findViewById(R.id.textFollowing)
        floatButton = view.findViewById(R.id.floatButton)
        settingsButton = view.findViewById(R.id.settingsButton)
        followingLayout = view.findViewById(R.id.following)
        followersLayout = view.findViewById(R.id.followers)


        gridView = view.findViewById(R.id.gridView)
        postAdapter = PostAdapter(userId!!,requireContext(),postList)
        gridView.adapter = postAdapter


        database = Firebase.database.reference
        progressBar.visibility = View.VISIBLE // Show progress bar

        // float butonuna basark createpost ekranına geçiş yapıyoruz
        floatButton.setOnClickListener{
            val intent = Intent(requireContext(), CreatePost::class.java)
            startActivity(intent)
        }
        // settings butonuan basarak profile ekranına geçiş yapıyoruz.
        settingsButton.setOnClickListener{
            val intent = Intent(requireContext(),ProfileScreen::class.java)
            startActivity(intent)
            requireActivity().finish()

        }
        //following userları görebilir
        followingLayout.setOnClickListener{
            val intent = Intent(requireContext(), FollowScreen::class.java)
            intent.putExtra("layoutType", "following")
            startActivity(intent)
        }
        //followers userları görebilir
        followersLayout.setOnClickListener{
            val intent = Intent(requireContext(), FollowScreen::class.java)
            intent.putExtra("layoutType", "followers")
            startActivity(intent)
        }
        // users dan name suername numberpost follower following sayılarını çekiyoruz
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
        // postun image caption ve post ıd sini çekiyoruz ve listeye ekliyoruz
        val postsRef = database.child("users").child("$userId").child("posts").orderByChild("createdAt")
        postsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear() // Önceki gönderileri temizle
                for (postSnapshot in snapshot.children) {
                    val imageUrl = postSnapshot.child("imageURL").getValue(String::class.java)
                    val caption = postSnapshot.child("caption").getValue(String::class.java)
                    val postId = postSnapshot.child("postId").getValue(String::class.java)

                    if (!imageUrl.isNullOrEmpty() && !caption.isNullOrEmpty() && !postId.isNullOrEmpty()) {
                        // Eğer imageURL boş değilse, postList'e ekleyelim
                        postList.add(gridModel(postId,caption, imageUrl))
                    }
                }
                postList.reverse()
                // Veri değiştiğinde adapter'a haber ver
                postAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Veritabanı okuması iptal edildiğinde
                Toast.makeText(context, "Failed to load posts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
        return view
    }

}