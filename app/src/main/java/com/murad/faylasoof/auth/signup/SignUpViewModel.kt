package com.murad.faylasoof.auth.signup

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception

class SignUpViewModel() : ViewModel() {


    private  val TAG = "SignUpViewModel"


    fun performSignUpWithGoogle(task: Task<GoogleSignInAccount>) :Flow<Resource<User>>{
        val signWithGoogleResult : Flow<Resource<User>> = flow {

           val result= handleSignInResultGoogle(task)


            emit(result)
        }

        return signWithGoogleResult

    }


     fun handleSignInResultGoogle(task:Task<GoogleSignInAccount>) :Resource<User>{

        try {
            val account = task.getResult(ApiException::class.java)

            Log.d(TAG, "handleSignInResultGoogle: ${account?.id}")

            val first_name  =account?.givenName
            val second_name = account?.familyName
            val email = account?.email
            val profile_pic = account?.photoUrl.toString()

            val resource = Resource.success(User(first_name,second_name,email,profile_pic))


            return resource
        }catch (e:Exception){

            Log.d(TAG, "handleSignInResultGoogle: failed ${e.message}")

            return Resource.error("handleSignInResultGoogle: failed",null)
        }

    }


}