package com.bau.socialmediaapp.homeFragment

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.databinding.ActivityHomeScreenBinding
import com.bau.socialmediaapp.homeFragment.fragments.HomeFragment
import com.bau.socialmediaapp.homeFragment.fragments.SearchFragment
import com.bau.socialmediaapp.homeFragment.fragments.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class HomeScreen : AppCompatActivity() {
    private lateinit var binding: ActivityHomeScreenBinding
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        auth= Firebase.auth
        bottomNav = binding.bottomNav
        loadFragment(HomeFragment()) // başlangıcta home fragmentı başlatıyoruz

        val fromProfileScreen = intent.getBooleanExtra("FROM_PROFILE_SCREEN", false)
        if (fromProfileScreen) {
            // Eğer bayrak varsa ve ProfileScreen aktivitesinden geliyorsa, ProfileFragment'ı yükle
            loadFragment(ProfileFragment())
        }

        // yonlendirme işlemini yapıyoruz
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_search-> {
                    loadFragment(SearchFragment())
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(ProfileFragment())
                    true
                }

                else -> false
            }
        }


    }
    private  fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fl_wrapper,fragment)
        transaction.commit()
    }
}