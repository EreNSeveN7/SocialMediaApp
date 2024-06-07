package com.bau.socialmediaapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bau.socialmediaapp.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ChangePasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        binding.btnChangePassword.setOnClickListener {
            val newPassword = binding.NewPassword.text.toString().trim()
            val confirmNewPassword = binding.ConfirmNewPassword.text.toString().trim()
            val oldPassword = binding.OldPassword.text.toString().trim()

            //edittext alanları boş ise toast mesajı dondursun
            if (newPassword.isNotEmpty() && confirmNewPassword.isNotEmpty() && oldPassword.isNotEmpty()) {
                if (newPassword == confirmNewPassword) {
                    // Reauthenticate user before changing password
                    val user = auth.currentUser
                    val credential = EmailAuthProvider.getCredential(user?.email!!, oldPassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                // If reauthentication successful, change password
                                user.updatePassword(newPassword)
                                    .addOnCompleteListener { passwordTask ->
                                        if (passwordTask.isSuccessful) {

                                            Toast.makeText(this,"Password updated",Toast.LENGTH_LONG).show()
                                            binding.NewPassword.text=null
                                            binding.ConfirmNewPassword.text=null
                                            binding.OldPassword.text=null
                                            finish()
                                        } else {
                                            Toast.makeText(this,"Password update failed",Toast.LENGTH_LONG).show()

                                        }
                                    }
                            } else {
                                Toast.makeText(this,"The old password you entered is incorrect",Toast.LENGTH_LONG).show()


                            }
                        }
                } else {
                    Toast.makeText(this,"New passwords do not match",Toast.LENGTH_LONG).show()


                }
            } else {
                Toast.makeText(this,"Fields are empty",Toast.LENGTH_LONG).show()


            }
        }

        binding.imageButton6.setOnClickListener {
            finish()
        }
    }
}
