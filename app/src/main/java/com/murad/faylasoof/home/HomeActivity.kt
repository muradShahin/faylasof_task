package com.murad.faylasoof.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.murad.faylasoof.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        supportActionBar?.hide()
    }


}