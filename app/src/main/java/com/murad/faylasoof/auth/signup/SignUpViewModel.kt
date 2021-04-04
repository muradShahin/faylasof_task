package com.murad.faylasoof.auth.signup

import androidx.lifecycle.*
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.murad.faylasoof.auth.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(

    val signUpRepository: SignUpRepository
) : ViewModel() {


    fun getSignUpWithGoogle(task: Task<GoogleSignInAccount>) =
        signUpRepository.signUpWithGoogle(task).asLiveData()

    fun createUser(user: User, password: String) =
        signUpRepository.createUser(user, password).asLiveData()

    fun createUserInFirestore(user: User) =
        signUpRepository.createUserInFirestore(user).asLiveData()

    fun loginWithFaceBook(token: AccessToken) =
        signUpRepository.handleFacebookAccessToken(token).asLiveData()

    fun uploadImageToFirebase(imageUri: String) =
        signUpRepository.uploadImageToFirebase(imageUri)

    fun getImageDownloadUri() = signUpRepository.imageDownloadLink


}