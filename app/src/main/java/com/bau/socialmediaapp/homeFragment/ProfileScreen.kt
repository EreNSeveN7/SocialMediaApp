package com.bau.socialmediaapp.homeFragment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bau.socialmediaapp.ChangePasswordActivity
import com.bau.socialmediaapp.EditProfileActivity
import com.bau.socialmediaapp.LandingPage
import com.bau.socialmediaapp.R
import com.bau.socialmediaapp.databinding.ActivityPostScreenBinding
import com.bau.socialmediaapp.databinding.ActivityProfileScreenBinding
import com.bau.socialmediaapp.homeFragment.fragments.ProfileFragment
import com.bau.socialmediaapp.homeFragment.fragments.SearchFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class ProfileScreen : AppCompatActivity() {
    private lateinit var binding: ActivityProfileScreenBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        // logout butonuna basarak oturumu kapatıyoruz
        binding.logout.setOnClickListener{
            auth.signOut()
            val intent = Intent(this, LandingPage::class.java)
            startActivity(intent)
            finish() //fragmentın bağlı olduğu aktivitenin sonlandırılmasını ifade edyior
        }

        binding.backButtonSettings.setOnClickListener{
            onBackPressed()

        }
        //change password ekranına gidiyourz.
        binding.changePassword.setOnClickListener{
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }
        // edit profile ekranına geçiş yapıyoruz.
        binding.edit.setOnClickListener{
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onBackPressed() {
        super.onBackPressed()
        // Geri butonuna basıldığında fragmenta geri dön
        val intent = Intent(this, HomeScreen::class.java).apply {
            putExtra("FROM_PROFILE_SCREEN", true)
        }
        startActivity(intent)
        finish()
    }

}