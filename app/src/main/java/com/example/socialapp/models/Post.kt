package com.example.socialapp.models

//Contains all information about the post
data class Post (
    val text: String = "",//text inside the post
    val createdBy: User = User(), //name of post creator
    val createdAt: Long = 0L, //time at which it is created
    val likedBy: ArrayList<String> = ArrayList())//We are storing the user id of everyone who has liked the post