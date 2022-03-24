package com.techtown.matchingservice

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    //Firebase Authentication관리 클래스
    var auth: FirebaseAuth? = null

    //GoogleLogin 관리 클래스
    var googleSignInClient: GoogleSignInClient? = null

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
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        //구글 로그인 클래스를 만듦
    }
}