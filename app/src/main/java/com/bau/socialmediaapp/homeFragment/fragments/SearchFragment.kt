package com.bau.socialmediaapp.homeFragment.fragments


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bau.socialmediaapp.PostScreen
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.SearchResultAdapter
import com.bau.socialmediaapp.User
import com.bau.socialmediaapp.comment
import com.bau.socialmediaapp.userSearch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SearchFragment : Fragment() {

    private lateinit var searchEditText: EditText
    private lateinit var searchResultRecyclerView: RecyclerView
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var searchResults: ArrayList<User> // Kullanıcı adlarını saklamak için bir dizi

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        searchResults = ArrayList<User>()

        searchEditText = view.findViewById(R.id.searchEditText)
        searchResultRecyclerView = view.findViewById(R.id.searchResultRecyclerView)
        searchResults = ArrayList()
        searchResultAdapter = SearchResultAdapter(searchResults)

        searchResultRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchResultAdapter
        }


//metin girişini dinler ve metin değişikliklerini algılar.
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
             //Arama metni değiştiğinde çağrılır.
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    performSearch(searchText)
                } else {
                    searchResults.clear()
                    searchResultAdapter.notifyDataSetChanged()
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        return view
    }
//Veritabanından kullanıcı adlarına göre bir arama yapar.
    private fun performSearch(searchText: String) {
        val query = databaseRef.child("users")
            .orderByChild("username")
            .startAt(searchText)
            .endAt(searchText + "\uf8ff")

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                searchResults.clear()
                for (data in snapshot.children) {
                    val username = data.child("username").getValue(String::class.java)
                    val nameSurname = data.child("nameSurname").getValue(String::class.java)
                    val email = data.child("email").getValue(String::class.java)
                    val userIDD = data.child("userId").getValue(String::class.java)
                    val numberFollowers = data.child("numberFollowers").getValue(Int::class.java)
                    val numberFollowing = data.child("numberFollowing").getValue(Int::class.java)
                    val numberPosts = data.child("numberPosts").getValue(Int::class.java)
                    // Mevcut kullanıcının adını kontrol et
                    if (!currentUser?.uid.equals(userIDD)) {
                        // Oluşturulan User nesnesini listeye ekle
                        val user = User( nameSurname ?: "", username ?: "", email ?: "",userIDD ?: "",  numberFollowers ?: 0, numberFollowing ?: 0, numberPosts ?: 0)
                        searchResults.add(user)
                    }
                }
                searchResultAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })
    }


}
