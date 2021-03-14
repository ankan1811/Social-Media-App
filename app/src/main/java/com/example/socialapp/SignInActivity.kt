package com.example.socialapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.socialapp.daos.UserDao
import com.example.socialapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private val RC_SIGN_IN: Int = 123
    private val TAG = "SignInActivity Tag"
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
        //Integrate Google Sign-In into your app by following the steps
        // on the Integrating Google Sign-In into Your Android App page. 
        //When you configure the GoogleSignInOptions object, call requestIdToken:


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //Now all client_id API keys are present in firebase
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth //Get the auth which will be used in firebaseAuthWithGoogle function later

        signInButton.setOnClickListener {  //As soon as sign in will be clicked signIn function created below will be called
            signIn()
        }

    }

    override fun onStart() {//onStart is called immediately after onCreate
        //In Onstart method we check that if user is already signed in .
        //If he is signed in we will take him to main activity else deactivate him.
        super.onStart()
        val currentUser = auth.currentUser //firebase auth class will bring the current user through auth
        updateUI(currentUser)//Pass it to updateUI function created below
    }
    //You must pass your server's client ID to the requestIdToken method. To find the OAuth 2.0 client ID:
    //a.Open the Credentials page in the GCP Console.
    //b.The Web application type client ID is your backend server's OAuth 2.0 client ID.
    private fun signIn() {
        //copied from docs
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)//signInIntent is for showing that box which contains the list of all email ids.
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { //Copied from docs
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) { //We just need the task
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account =
                completedTask.getResult(ApiException::class.java)!! //completedTask IS THE TASK WHICH WILL HAVE OUR ACCOUNT through which we have signed in
                //We will take out the account from there
            Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w(TAG, "signInResult:failed code=" + e.statusCode)

        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)//We will get the credentials
        signInButton.visibility = View.GONE//signin button will be invisible
        progressBar.visibility = View.VISIBLE//progressbar will be invisible from starting and it will be visible now if sign in takes time
        //Now we have to run the coroutines (Background thread and get rid of callback hell)
        GlobalScope.launch(Dispatchers.IO) {
            val auth = auth.signInWithCredential(credential).await()//we get authentication through which we sign in
            val firebaseUser = auth.user //we will get the firebase user
            withContext(Dispatchers.Main) {//Now we want to update the UI in the main thread so we switch from background thread to main 
                //thread using withContext
                updateUI(firebaseUser)
            }
        }

    }

    private fun updateUI(firebaseUser: FirebaseUser?) {
        //In updateUI function if firebase user exists or not.
        // If it exists then we will go to the next page(MainActivity) else we will ask to sign in again.
        if(firebaseUser != null) {

            //Make the user with the help of firebase user
            //photoUrl is actually an URI so we convert it to string.
            val user = User(firebaseUser.uid, firebaseUser.displayName, firebaseUser.photoUrl.toString())
            val usersDao = UserDao()//we want to add the user to the db with the help of dao
            usersDao.addUser(user)

            val mainActivityIntent = Intent(this, MainActivity::class.java)
            startActivity(mainActivityIntent)//starts mainActivity
            finish()
        } else {
            signInButton.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
