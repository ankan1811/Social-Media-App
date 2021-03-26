package com.example.socialapp.daos

import com.example.socialapp.models.Post
import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PostDao { //similar to the structure of userDao.kt

    val db = FirebaseFirestore.getInstance()
    val postCollections = db.collection("posts")
    val auth = Firebase.auth

    fun addPost(text: String) {
        GlobalScope.launch {//backgroung thread (scope of coroutines
            val currentUserId = auth.currentUser!!.uid//
            val userDao = UserDao()
            val user = userDao.getUserById(currentUserId).await().toObject(User::class.java)!! //We get the actual user task which we convert to user object from userDao.kt

            val currentTime = System.currentTimeMillis() //get the time at which pot is created
            val post = Post(text, user, currentTime)//Pass these 3 things to the post for Post.kt
            postCollections.document().set(post)
        }
    }

    fun getPostById(postId: String): Task<DocumentSnapshot> { //It will take the id of the post and return the task(document post)
        return postCollections.document(postId).get()
    }

    fun updateLikes(postId: String) { //We need the id of the current user and put it in the 
        GlobalScope.launch {
            val currentUserId = auth.currentUser!!.uid
            val post = getPostById(postId).await().toObject(Post::class.java)!! //Now we need the post whose likedby should be updtaed
            //Task will be converted to Post object
            val isLiked = post.likedBy.contains(currentUserId) //We check if the currentid is present in the likedby of the post or not

            if(isLiked) { //if the current user has already liked the post then remove it i.e. dislike the post and vice versa
                post.likedBy.remove(currentUserId)
            } else {
                post.likedBy.add(currentUserId)
            }
            postCollections.document(postId).set(post) //Now we have to update that particular post documenty by passing the post id.
        }

    }

}
