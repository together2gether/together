package com.techtown.matchingservice

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {
    //Firebase Authentication관리 클래스
    var auth: FirebaseAuth? = null

    //GoogleLogin 관리 클래스
    var googleSignInClient: GoogleSignInClient? = null

    //Facebook 로그인 처리 결과 관리 클래스
    var callbackManager: CallbackManager? = null

    //GoogleLogin
    val GOOGLE_LOGIN_CODE = 9001 //Intent Request ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        //Firebase 로그인 통합 관리하는 Object 만들기
        auth = FirebaseAuth.getInstance()

        //구글 로그인 옵션
        var gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("372316850033-9lvhsfl1ea12mks4rdgl69rk3s3ulbmb.apps.googleusercontent.com")
                .requestEmail()
                .build()

        //구글 로그인 클래스를 만듦
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        callbackManager = CallbackManager.Factory.create()

        //구글 로그인 버튼 세팅
        val google_sign_in_button: ImageButton = findViewById(R.id.google_sign_in_button)
        google_sign_in_button.setOnClickListener{ googleLogin() }
    }
    fun googleLogin(){
        //하울 스타그램에는 progress_bar가 있음
        var signInIntent = googleSignInClient?.signInIntent
        startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //구글에서 승인된 정보를 가지고 오기
        if(requestCode == GOOGLE_LOGIN_CODE){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            //책에는 if(result!-null)이 없음
            if (result != null) {
                if(result.isSuccess){
                    var account = result.signInAccount
                    firebaseAuthWithGoogle(account!!)
                } else{
                    //로딩이나 프로그레스 바 넣을까...?
                }
            }
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener{task ->
                //프로그레스바
                if(task.isSuccessful){
                    //다음 페이지 호출
                    moveMainPage(auth?.currentUser)
                }
            }
    }

    fun moveMainPage(user : FirebaseUser?){
        //User is signed in
        if(user!=null){
            Toast.makeText(this, "로그인 완료", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onStart(){
        super.onStart()

        //자동로그인 설정
        moveMainPage(auth?.currentUser)
    }
}