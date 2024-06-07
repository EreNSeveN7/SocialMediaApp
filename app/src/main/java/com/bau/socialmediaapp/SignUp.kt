package com.bau.socialmediaapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.bau.socialmediaapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class SignUp : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var database: DatabaseReference

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameSurnameEditText: EditText
    private lateinit var usernameEditText: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth= FirebaseAuth.getInstance()   // auth ve firebase i başlatıyoruz
        firestore = FirebaseFirestore.getInstance()
        database = Firebase.database.reference

        nameSurnameEditText = binding.editTextNameSurname
        usernameEditText = binding.editTextUsername
        emailEditText = binding.editTextRegisterEmail
        passwordEditText = binding.editTextRegisterPassword


        binding.backButton.setOnClickListener{ // back butonuna bastığımızda landing page e gidiyoruz
            val intent = Intent(this,LandingPage::class.java)
            startActivity(intent)
            finish()
        }

        binding.signUpButton.setOnClickListener{
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val nameSurname = nameSurnameEditText.text.toString()
            val username = usernameEditText.text.toString()


            // eğer emial password name username alanalrından en az biri boş ise toast mesajı dondur.
            if (email.isEmpty() || password.isEmpty() || nameSurname.isEmpty() || username.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // edittext alanları dolu ise email ve passwordu kullanrak bir user olustur
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        // olusturulan user ın bilgilerini database e kaydediyoruz
                        val userAuth = auth.currentUser
                        val userId = userAuth?.uid
                        val stringUserId = userId.toString()
                        val user = User(nameSurname,username,email,stringUserId)
                        if (userId != null) {
                            database.child("users").child(userId).setValue(user)
                            Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                            val intent = Intent(this, SignIn::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            this, "Error: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }


    }
}