package com.example.notenexus.Activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.example.notenexus.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class MainActivity : AppCompatActivity() {

    lateinit var googleSignIn: Button
    lateinit var googleSignInClient: GoogleSignInClient

    lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN=100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        googleSignIn =findViewById(R.id.google_sign_in)

        firebaseAuth=FirebaseAuth.getInstance()

        checkUser()

        val googleSignInOptions=GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)

            .requestIdToken("852933388849-o5j1h3aqnieptrsmbasun6tu9hgp9vgm.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,googleSignInOptions)

        googleSignIn.setOnClickListener{
            val intent=googleSignInClient.signInIntent

            startActivityForResult(intent,RC_SIGN_IN)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            RC_SIGN_IN ->{
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)

                try {
                      val account = accountTask.getResult(ApiException:: class.java)
                    firebaseAuthWithGoogleAccount(account)


                }
                catch (e:Exception){
                    Toast.makeText(this, "Sign in unsuccessful",Toast.LENGTH_SHORT).show()

                }
            }
        }


    }

    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount?) {
        val credential= GoogleAuthProvider.getCredential(account!!.idToken,null)

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                startActivity(Intent(this,NoteActivity::class.java))
                overridePendingTransition(0,0)
                finish()
            }
            .addOnFailureListener{
                Toast.makeText(this,it.message,Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUser() {

        val firebaseUser=firebaseAuth.currentUser

        if(firebaseUser!=null){

            startActivity(Intent(this,NoteActivity::class.java))
            overridePendingTransition(0,0)
            finish()

        }
        else{
            firebaseAuth.signOut()
        }
    }
}