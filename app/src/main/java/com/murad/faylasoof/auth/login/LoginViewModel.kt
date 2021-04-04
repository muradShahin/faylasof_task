package com.murad.faylasoof.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.murad.faylasoof.auth.models.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(
    val loginRepository: LoginRepository
) : ViewModel() {


    fun loginWithEmailAndPassword(email: String, password: String) =
        loginRepository.loginWithEmailAndPassword(email, password).asLiveData()


    fun loginWithGoogle(task: Task<GoogleSignInAccount>) =
        loginRepository.loginWithGoogle(task).asLiveData()


    fun addUserToDb(user: User) = loginRepository.addUserToDb(user).asLiveData()


}