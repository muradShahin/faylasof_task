package com.murad.faylasoof.auth.models

data class User(
    val first_name:String?,
    val lastname:String? ="",
    val email:String?,
    val profilePic:String?,
    val dateOfBirth:String? = "",
    val gender:String? = "none"
)