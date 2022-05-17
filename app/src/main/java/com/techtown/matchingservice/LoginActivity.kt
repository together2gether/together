package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.techtown.matchingservice.databinding.LoginBinding
import com.techtown.matchingservice.model.UsersInfo

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var binding: LoginBinding
    lateinit var googleSigninClient : GoogleSignInClient

    val database = Firebase.database("https://matchingservice-ac54b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val infoRef = database.getReference("usersInfo")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.login)
        auth = FirebaseAuth.getInstance()

        binding.googleSigninButton.setOnClickListener {
            LoadingDialog(this).show()
            //First step
            googleLogin()
        }
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("372316850033-9lvhsfl1ea12mks4rdgl69rk3s3ulbmb.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSigninClient = GoogleSignIn.getClient(this, gso)
    }

    private fun googleLogin() {
        val signInIntent = googleSigninClient.signInIntent
        googleLoginResult.launch(signInIntent)
    }

    /*override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account != null){
            moveMainPage(auth?.currentUser)
        }
    }*/


    fun firebaseAuthWithGoogle(idToken: String?) {
        var credential = GoogleAuthProvider.getCredential(idToken,null)
        auth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                if (auth.currentUser!!.isEmailVerified) {
                    val uid = auth.currentUser!!.uid.toString()
                    infoRef.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val userInfo = snapshot.getValue<UsersInfo>()
                            if(userInfo == null){
                                LoadingDialog(this@LoginActivity).dismiss()
                                moveModifyPage(task.result?.user)
                            }
                            else {
                                LoadingDialog(this@LoginActivity).dismiss()
                                moveMainPage(task.result?.user)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }
                    })

                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    var googleLoginResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        // 구글 로그인이 성공했을때 이메일 값이 넘어오는데 -> Token -> Email -> Firebase 서버
        var data = result.data
        var task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)
        firebaseAuthWithGoogle(account.idToken)
    }

    fun moveMainPage(user: FirebaseUser?){
        if(user != null){
            startActivity(Intent(this,Home::class.java))
            finish()
        }
    }

    fun moveModifyPage(user: FirebaseUser?){
        if(user!=null){
            startActivity(Intent(this,MainActivity::class.java))
            startActivity(Intent(this, ModifyInfo::class.java))
            finish()
        }
    }
}