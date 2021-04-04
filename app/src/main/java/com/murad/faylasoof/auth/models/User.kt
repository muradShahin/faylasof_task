package com.murad.faylasoof.auth.models

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.murad.faylasoof.R

data class User(
    val first_name: String? = "",
    val lastname: String? = "",
    val email: String? = "",
    val profilePic: String? = "",
    val dateOfBirth: String? = "",
    val gender: String? = "none"
) {

    object DataBindingAdapter {
        @JvmStatic
        @BindingAdapter("android:loadImage")
        fun loadImage(imageView: ImageView, url: String) {

            Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.user)
                .into(imageView)

        }
    }

}