package com.example.socialapp.daos //To put the user entities in user database

import com.example.socialapp.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserDao { //This is how we add data inside firebase

    private val db = FirebaseFirestore.getInstance() //To get the reference of the user database i.e. usersCollection
    //Now a bd has multiple collections.We ant the users collection
    private val usersCollection = db.collection("users")

    fun addUser(user: User?) {
        user?.let {//If User exists we need to put the user in the user database
            GlobalScope.launch(Dispatchers.IO) {//Database calls should be in background thread otherwise main UI thread will be blocked

                usersCollection.document(user.uid).set(it)//Inside the collection that entry 's id should be equal to user id
            }
        }
    }

    fun getUserById(uId: String): Task<DocumentSnapshot> { 
        return usersCollection.document(uId).get() //get function returns a task(user data here)
    }

}
