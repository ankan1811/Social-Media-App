package com.example.socialapp.models

data class User(val uid: String = "",//user id,display name and image (Image will be the image of the signed in email id)
                val displayName: String? = "",
                val imageUrl: String = "")
