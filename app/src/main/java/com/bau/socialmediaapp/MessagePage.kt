package com.bau.socialmediaapp

import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager

import com.bau.socialmediaapp.databinding.ActivityMessagePageBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.auth.User
import com.google.firebase.messaging.FirebaseMessaging
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Locale

// takip ettiğimiz kişiler bu sayfdada listeleniyor ve burdaki kişilere mesaj gonderebiliyoruz

class MessagePage : AppCompatActivity() {
    private lateinit var binding: ActivityMessagePageBinding
    private lateinit var messageAdapter: messageAdapter
    private lateinit var messageList: ArrayList<Message>
    private  lateinit var userId : String
    private  lateinit var currentUserId: String
    private lateinit var  database:DatabaseReference
    private  lateinit var chatId : String
    private  lateinit var reverseChatId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessagePageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        userId = intent.getStringExtra("userId").toString()
        database = FirebaseDatabase.getInstance().reference
        // Chat ID'yi almak için createChatId fonksiyonunu çağır
        createChatId(currentUserId, userId) { chatId ->
            this@MessagePage.chatId = chatId

            binding.sendButton.setOnClickListener {
                // Mesajı al
                val messageContent = binding.messageEditText.text.toString().trim()

                // Mesaj boş değilse ve gönderen ve alıcı UID'leri varsa
                if (messageContent.isNotEmpty() && currentUserId.isNotEmpty() && userId.isNotEmpty()) {
                    // Firebase veritabanına mesajı gönder
                    sendMessage(chatId, currentUserId, userId, messageContent)
                }
            }

            binding.messageBackButton.setOnClickListener{
                finish()
            }


            database.child("users").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val name = snapshot.child("nameSurname").getValue(String::class.java)
                        binding.messageUserName.text = name
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Veritabanı okuması iptal edildiğinde
                }
            })


            messageList = ArrayList()
            messageAdapter = messageAdapter(messageList)
            binding.messageRecyclerView.adapter = messageAdapter
            binding.messageRecyclerView.layoutManager = LinearLayoutManager(this)

            readMessages(chatId)

        }
    }

    private fun createChatId(uid1: String, uid2: String, callback: (String) -> Unit) {
        var id :String = ""
        val reference = database.child("messages")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var isId = false
                for (messageSnapshot in snapshot.children) {

                    // messageId değerini almak için doğru alanı belirleyin
                    val messageId = messageSnapshot.key // Örnek olarak, düğümün anahtarını kullanıyoruz

                    if (messageId == uid1 + uid2 ) {
                        id = messageId
                        isId = true
                        break
                    }
                }
                if (!isId) {
                    id = uid2 + uid1
                }
                callback(id)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
                Toast.makeText(
                    applicationContext,
                    "Database error: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun sendMessage(chatUid: String,senderUid: String, receiverUid: String, messageContent: String) {
        // Mesajın gönderilme zamanını al
        val currentTime = Calendar.getInstance().timeInMillis // Telefonun yerel saat bilgisi
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val formattedDate = dateFormat.format(currentTime)
        // Mesajı Firebase veritabanına ekle
        val messageRef = database.child("messages").child(chatUid).push()
        val message = Message(senderUid, receiverUid, messageContent, formattedDate)
        messageRef.setValue(message)
            .addOnSuccessListener {
                // Mesaj gönderme başarılı
                // EditText'i temizle
                binding.messageEditText.setText("")
            }
            .addOnFailureListener { exception ->
                // Mesaj gönderme başarısız
                // Hata durumunda burada işlemler yapılabilir
            }




    }



    private fun readMessages(chatUid: String) {
        // Firebase veritabanındaki "messages" düğümünden mesajları dinle
        database.child("messages").child(chatUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Daha önceki mesajları temizle
                messageList.clear()
                // Her bir mesajı ArrayList'e ekle
                for (messageSnapshot in snapshot.children) {
                    val senderUid = messageSnapshot.child("senderUid").getValue(String::class.java) ?: ""
                    val receiverUid = messageSnapshot.child("receiverUid").getValue(String::class.java) ?: ""
                    val messageContent = messageSnapshot.child("messageContent").getValue(String::class.java) ?: ""
                    val timestamp = messageSnapshot.child("timestamp").getValue(String::class.java) ?: ""

                    // Mesajı oluştur ve ArrayList'e ekle
                    val message = Message(senderUid, receiverUid, messageContent, timestamp)
                    messageList.add(message)
                }
                // Adaptera değişiklik olduğunu bildir
                messageAdapter.notifyDataSetChanged()
                binding.messageRecyclerView.scrollToPosition(messageAdapter.itemCount - 1)

            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda burada işlemler yapılabilir
            }
        })
    }

}
