package com.murad.faylasoof.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.murad.faylasoof.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Auth_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





    }
}