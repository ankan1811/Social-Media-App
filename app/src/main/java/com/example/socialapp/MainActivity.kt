package com.example.socialapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.socialapp.daos.PostDao
import com.example.socialapp.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IPostAdapter {

    private lateinit var postDao: PostDao
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fab.setOnClickListener{ //fab is the id of the floating button
            //As soon as that button will be clicked a new activty will open where we can create post
            val intent = Intent(this, CreatePostActivity::class.java)//router in main activity to proceed to the next activity i.e.CreatePostActivity
            startActivity(intent)
        }

        setUpRecyclerView()
    }

    private fun setUpRecyclerView() { //This part consists of the logic about how the posts will be shown in the recycler view
        //So the posts will be sorted in the descending order of created At i.e. the latest created post will come at the top
        postDao = PostDao()
        val postsCollections = postDao.postCollections
        val query = postsCollections.orderBy("createdAt", Query.Direction.DESCENDING)//createdAt is a Long
        val recyclerViewOptions = FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()//Used to build the query

        adapter = PostAdapter(recyclerViewOptions, this)//Pass the options in PostAdapter.kt

        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() { //As soon as the app starts adapter will start listening to any changes in the firebase and firestore database and vice versa
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onLikeClicked(postId: String) {
        postDao.updateLikes(postId) //If like is clickd thio=s function will be called
    }
}
