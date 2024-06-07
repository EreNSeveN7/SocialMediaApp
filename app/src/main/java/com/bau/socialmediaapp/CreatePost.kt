package com.bau.socialmediaapp

import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bau.socialmediaapp.databinding.ActivityCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class CreatePost : AppCompatActivity() {
    private lateinit var binding: ActivityCreatePostBinding

    private val storageRef = FirebaseStorage.getInstance().reference
    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.settingBackButton.setOnClickListener{
            finish()
        }
        //phone da bulunana gallery i açıyor
        binding.buttonGallery.setOnClickListener {
            openGallery()
        }
          // postu eklıyoruz image ve caption ile birlikte
        binding.postButton.setOnClickListener {
            uploadImageToFirebaseStorage()
        }
    }
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        resultLauncher.launch(intent)
    }
    // seçilen görüntünün URI'sini alır ve binding.imageGallery.setImageURI(selectedImageUri) ile bir ImageView'a yükler.
    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                selectedImageUri = data?.data
                binding.imageGallery.setImageURI(selectedImageUri)

            }
        }

    // firebase storage a resmi eklıyoruz
    private fun uploadImageToFirebaseStorage() {
        val caption = binding.caption.text.toString()

        if (selectedImageUri == null || caption.isEmpty()) {
            Toast.makeText(this, "Please select an image and write a caption", Toast.LENGTH_SHORT).show()
            return
        }

        val currentTime = Calendar.getInstance().timeInMillis // Telefonun yerel saat bilgisi
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime)

        selectedImageUri?.let { uri ->
            val filename = UUID.randomUUID().toString()
            val ref = storageRef.child("images/$filename")

            ref.putFile(uri)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener { imageUrl ->
                        // Fotoğraf yüklendikten sonra URL'yi alın
                        val userId = auth.currentUser?.uid

                        val postRef = databaseRef.child("users").child("$userId").child("posts").push()
                        val post = hashMapOf(
                            "imageURL" to imageUrl.toString(),
                            "likes" to 0,
                            "postId" to postRef.key,
                            "caption" to caption,
                            "createdAt" to formattedDate
                        )
                        postRef.setValue(post)
                            .addOnSuccessListener {
                                // Post başarıyla eklendi
                                incrementPostCount(auth.currentUser!!.uid)
                                finish()
                            }
                            .addOnFailureListener {
                                // Post ekleme hatası
                            }
                    }
                }
                .addOnFailureListener {
                    // Yükleme hatası
                }
        }
    }
    // her post eklediğimizde post sayısını arttırıyoruz
    private fun incrementPostCount(uid: String) {
        val userFollowingRef = databaseRef.child("users").child(uid).child("numberPosts")
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
}