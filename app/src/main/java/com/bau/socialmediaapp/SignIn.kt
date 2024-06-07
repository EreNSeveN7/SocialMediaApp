package com.bau.socialmediaapp

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bau.socialmediaapp.databinding.ActivitySignInBinding
import com.bau.socialmediaapp.homeFragment.HomeScreen
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        emailEditText = binding.TextEmail
        passwordEditText = binding.TextPassword

        binding.backButtonSignIn.setOnClickListener{
            val intent = Intent(this,LandingPage::class.java)
            startActivity(intent)
            finish()
        }
        binding.textViewForgot.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Reset Password")
            val input = EditText(this)
            input.hint = "Enter your email"
            input.background = ContextCompat.getDrawable(this, R.drawable.custom3)

            builder.setView(input)

            // reset password maili gonderiyoruz eer email girilmiş ise
            builder.setPositiveButton("Send") { dialog, which ->
                val email = input.text.toString()
                if (email.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    baseContext,
                                    "Şifre sıfırlama e-postası gönderildi. Lütfen e-postanızı kontrol edin.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    baseContext,
                                    "Şifre sıfırlama e-postası gönderilirken bir hata oluştu: ${task.exception?.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        baseContext,
                        "Lütfen bir e-posta adresi girin",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            builder.setNegativeButton("Cancel") { dialog, which ->
                dialog.cancel()
            }

            builder.show()
        }
        binding.signIn.setOnClickListener{
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            // edittext alanlarından en az biri boş  ise toast mesajı dondur
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // edittext alanları dolu ise email ve apssword ile sign ın yapıyoruz
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val userAuth = auth.currentUser
                        val userId = userAuth?.uid

                        if (userId != null) {
                            val intent = Intent(this, HomeScreen::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(
                            baseContext, "Giriş sırasında bir hata oluştu: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

        }
    }
}