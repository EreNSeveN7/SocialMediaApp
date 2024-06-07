package com.bau.socialmediaapp

data class Message(
    val senderUid: String,
    val receiverUid: String,
    val messageContent: String,
    val timestamp: String
)