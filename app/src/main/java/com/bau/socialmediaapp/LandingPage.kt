package com.bau.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bau.socialmediaapp.databinding.ActivityLandingPageBinding
import com.bau.socialmediaapp.homeFragment.HomeScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LandingPage : AppCompatActivity() {
    private lateinit var binding: ActivityLandingPageBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingPageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth= Firebase.auth
        val currentUser= auth.currentUser   // güncel bir kullanıcı girişi varsa.kullanıcı her defasında emialini ve passwordunu yazmaz
        if (currentUser != null){

            val intent = Intent(this, HomeScreen::class.java)
            startActivity(intent)
            finish()

        }
         // signup butonuna bastığımızda signup ekranına gidiyoruz
        binding.SignUpButton.setOnClickListener{
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
            finish()
        }
        // signın butonuna bastığımızda signın ekranına gidiyoruz
        binding.SignInButton.setOnClickListener{
            val intent = Intent(this,SignIn::class.java)
            startActivity(intent)
            finish()
        }

    }
}