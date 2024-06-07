package com.bau.socialmediaapp

data class User(val nameSurname:String,val username: String,
                val email: String,val userId:String,val numberFollowers: Int = 0,
                val numberFollowing: Int = 0,
                val numberPosts: Int = 0) {




}