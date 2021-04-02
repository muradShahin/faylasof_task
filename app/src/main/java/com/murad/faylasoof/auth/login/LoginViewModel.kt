package com.murad.faylasoof.auth.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {


    private  val TAG = "LoginViewModel"

    @Inject
    lateinit var auth:FirebaseAuth
    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore


    /**
     * firebase auth login
     */

    fun loginWithEmailAndPass(email:String,password:String):Flow<Resource<AuthResult>>{

        val loginTask : Flow<Resource<AuthResult>> = flow {

            val result = performLoginWithEmailAndPassword(email, password)

            emit(result)
        }

        return loginTask

    }

    private suspend fun performLoginWithEmailAndPassword(email: String,password: String):Resource<AuthResult>{

        try {

            return Resource.success(auth.signInWithEmailAndPassword(email,password).await())

        }catch (e:Exception){
            return Resource.error("could'nt login",null)
        }
    }

    /**
     *
     * login with google
     */

    fun loginWithGoogle(task : Task<GoogleSignInAccount>):Flow<Resource<User>>{

        val loginWithGoogleTask : Flow<Resource<User>> = flow {

            val result = handleSignInResultGoogle(task)

            emit(result)

        }

        return loginWithGoogleTask

    }




    private fun handleSignInResultGoogle(task: Task<GoogleSignInAccount>) :Resource<User>{

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


    /**
     * add user to database (after login with google)
     */


    fun performAddUserToDb(user: User) : Flow<Resource<DocumentReference>>{


        val task : Flow<Resource<DocumentReference>> = flow {

            val result = createUserInFireStore(user)

            emit(result)
        }

        return task

    }

    private suspend fun createUserInFireStore(user: User) :Resource<DocumentReference>{

        try {

            return Resource.success(firebaseFirestore.collection("users")
                    .add(user).await())

        }catch (e:Exception){
            return Resource.error("${e.message}",null)
        }

    }





}