package com.bau.socialmediaapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bau.socialmediaapp.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""

        // Kullanıcının mevcut verilerini al ve EditText alanlarına yerleştir
        getUserData()

        binding.btnChangePassword.setOnClickListener {
            // EditText alanlarından yeni verileri al
            val firstName = binding.FirstName.text.toString().trim()
            val userName = binding.UserName.text.toString().trim()

            // Firebase veritabanında kullanıcı verilerini güncelle
            if (validateInput(firstName, userName)) {
                updateUserData(firstName, userName)
            }

        }

        binding.backButtonEdit.setOnClickListener {
            finish()
        }
    }

    private fun getUserData() {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(currentUserUid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val name = snapshot.child("nameSurname").getValue(String::class.java)
                    val username = snapshot.child("username").getValue(String::class.java)

                    binding.FirstName.setText(name)
                    binding.UserName.setText(username)

                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda yapılacak işlemler
            }
        })
    }

    private fun validateInput(nameSurname: String, userName: String): Boolean {
        // Girilen verilerin doğruluğunu kontrol et
        if (nameSurname.isEmpty() ||  userName.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

//  namesurname username updatele
    private fun updateUserData(nameSurname: String, userName: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(currentUserUid)


        userRef.child("nameSurname").setValue(nameSurname)
        userRef.child("username").setValue(userName)
        Toast.makeText(this, "Your profile updated successfully", Toast.LENGTH_SHORT).show()

    }
}
