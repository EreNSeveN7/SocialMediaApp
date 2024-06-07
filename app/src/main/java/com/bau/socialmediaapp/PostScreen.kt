package com.bau.socialmediaapp

import android.graphics.Color
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bau.socialmediaapp.databinding.ActivityPostScreenBinding
import com.bau.socialmediaapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso;
import java.util.Calendar
import java.util.Locale

class PostScreen : AppCompatActivity() {
    private lateinit var binding: ActivityPostScreenBinding
    private lateinit var alertDialog: AlertDialog // AlertDialog değişkeni burada tanımlanıyor

    private val databaseRef = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    private  lateinit var commentArrayList : ArrayList<comment>
    private  lateinit var commentAdapter : commentAdapter


    private lateinit var likeButton: ImageButton
    private var isLiked: Boolean = true
    private var username: String? = null
    private lateinit var userId:String
    private lateinit var postId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostScreenBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        commentArrayList = ArrayList<comment>()
        binding.rcycComment.layoutManager = LinearLayoutManager(this)
        commentAdapter = commentAdapter(commentArrayList)
        binding.rcycComment.adapter = commentAdapter


        postId = intent.getStringExtra("postId").toString()
        userId = intent.getStringExtra("IDD").toString()
        val captionRef = databaseRef.child("users").child(auth.currentUser!!.uid).child("posts").child(postId)

        captionRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                    val caption = snapshot.child("caption").getValue(String::class.java)

                binding.textView11.text = caption

            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
        val postLikeText = databaseRef.child("users").child(userId).child("posts").child(postId)
            .child("likes")
        postLikeText.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentLikes = snapshot.getValue(Int::class.java) ?: 0
                binding.textView12.text =
                    currentLikes.toString()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })

        val likeRef = databaseRef.child("users").child(auth.currentUser!!.uid ?: "").child("isLiked")
        likeButton = binding.imageButton3

        binding.imageButton.setOnClickListener{
            finish()
        }
        binding.imageButton2.setOnClickListener{
// AlertDialog oluşturma
            val inflater = layoutInflater
            val dialogView = inflater.inflate(R.layout.messages_text, null)
            val alertDialogBuilder = AlertDialog.Builder(this)

            alertDialogBuilder.setTitle("Add a comment")
            alertDialogBuilder.setView(dialogView)

            val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
            val sendButton = dialogView.findViewById<Button>(R.id.sendButton)
            val commentEditText = dialogView.findViewById<EditText>(R.id.message)

            val currentTime = Calendar.getInstance().timeInMillis // Telefonun yerel saat bilgisi
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val formattedDate = dateFormat.format(currentTime)

            val userRef = databaseRef.child("users").child(auth.currentUser!!.uid)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    username = snapshot.child("username").getValue(String::class.java)
                }

                override fun onCancelled(error: DatabaseError) {


                }
            })

            // İptal butonuna tıklama işlemi
            cancelButton.setOnClickListener {
                // AlertDialog kapatma
                println("cancel butonuna basıldı")
                alertDialog.dismiss()
            }

            // Gönder butonuna tıklama işlemi
            sendButton.setOnClickListener {
                val commentText = commentEditText.text.toString()
                val commentRef = databaseRef.child("users").child(userId ?: "").child("posts").child(postId ?: "").child("comments").push()
                val commentData = hashMapOf(
                    "username" to username,
                    "userId" to userId,
                    "comment" to commentText,
                    "createdAt" to formattedDate
                )

                commentRef.setValue(commentData)
                    .addOnSuccessListener {
                        // Başarılı bir şekilde eklendiğinde yapılacak işlemler
                        Toast.makeText(this, "Comment added successfully", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss() // AlertDialog'ı kapat
                    }
                    .addOnFailureListener {
                        // Ekleme işlemi başarısız olduğunda yapılacak işlemler
                        Toast.makeText(this, "Failed to add comment", Toast.LENGTH_SHORT).show()
                    }
            }

            // AlertDialog'u oluştur ve göster
            alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }

        likeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLiked = false
                for (childSnapshot in snapshot.children) {
                    val postIdFromDatabase = childSnapshot.getValue(String::class.java)
                    if (postIdFromDatabase == postId) {
                        isLiked = true
                        break
                    }
                }

                if (isLiked) {
                    likeButton.setColorFilter(Color.RED) // Change color to red if liked
                } else {
                    likeButton.clearColorFilter() // Remove color filter if unliked
                }
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        likeButton.setOnClickListener {
            if (isLiked) {
                // İlgili postun postId'sini isLiked'dan kaldır
                likeRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            val postIdFromDatabase = childSnapshot.getValue(String::class.java)
                            if (postIdFromDatabase == postId) {
                                childSnapshot.ref.removeValue()
                                break
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })
                // Likenumber'ı azalt
                val likeNumberRef = databaseRef.child("users").child(userId!!).child("posts").child(postId!!).child("likes")
                likeNumberRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java)
                        if (currentLikes != null) {
                            likeNumberRef.setValue(currentLikes - 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            } else {
                // Beğenilmediyse ilgili postun postId'sini isLiked'a ekle
                likeRef.push().setValue(postId)

                // Likenumber'ı arttır
                val likeNumberRef = databaseRef.child("users").child(userId!!).child("posts").child(postId!!).child("likes")
                likeNumberRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val currentLikes = snapshot.getValue(Int::class.java)
                        if (currentLikes != null) {
                            likeNumberRef.setValue(currentLikes + 1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
            }

            // Buton rengini güncelle
            isLiked = !isLiked
            if (isLiked == true) {
                likeButton.setColorFilter(Color.RED) // Change color to red if liked
            } else {
                likeButton.clearColorFilter() // Remove color filter if unliked
            }
        }

        if (postId != null) {
            databaseRef.child("users").child(userId ?: "").child("posts").child(postId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val imageURL = snapshot.child("imageURL").getValue(String::class.java)

                            // imageURL'i kullanarak resmi yükle ve ImageView'e ekle
                            imageURL?.let {
                                Picasso.get().load(imageURL).resize(300,300).into(binding.imageView4)
                            }
                        } else {
                            // postId'ye sahip bir gönderi bulunamadı, uygun bir hata işle
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {
                        // Veritabanı işlemi iptal edildi, uygun bir hata işle
                    }
                })
        }

        fetchComments()
    }

    private fun fetchComments() {

        val commentsRef = databaseRef.child("users").child(userId ?: "").child("posts").child(postId ?: "").child("comments")

        commentsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentArrayList.clear() // Mevcut yorum listesini temizle

                for (commentSnapshot in snapshot.children) {
                    val name = commentSnapshot.child("username").getValue(String::class.java)
                    val comment = commentSnapshot.child("comment").getValue(String::class.java)
                    val commentTimestamp = commentSnapshot.child("createdAt").getValue(String::class.java)

                    if (name != null && comment != null && commentTimestamp != null) {
                        val comment = comment(name, comment, commentTimestamp)
                        commentArrayList.add(comment)
                    }
                }

                commentAdapter.notifyDataSetChanged() // Adaptörü güncelle
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda bir şeyler yap
            }
        })
    }
}


