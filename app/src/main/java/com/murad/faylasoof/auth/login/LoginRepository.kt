package com.murad.faylasoof.auth.login

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.Resource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class LoginRepository @Inject constructor(

    val firebaseAuth: FirebaseAuth,
    val firestore: FirebaseFirestore

) {


    fun loginWithEmailAndPassword(email: String, password: String) = flow {

        try {


            emit(Resource.success(firebaseAuth.signInWithEmailAndPassword(email, password).await()))

        } catch (e: Exception) {
            emit(Resource.error("failed",false))

        }


    }

    fun loginWithGoogle(task: Task<GoogleSignInAccount>) = flow {

        try {
            val account = task.getResult(ApiException::class.java)


            val first_name = account?.givenName
            val second_name = account?.familyName
            val email = account?.email
            val profile_pic = account?.photoUrl.toString()

            emit(Resource.success(User(first_name, second_name, email, profile_pic)))

        } catch (e: Exception) {

            emit(Resource.error("could not login with google", null))
        }
    }


    fun addUserToDb(user: User) = flow {

        try {
            emit(Resource.success(firestore.collection("users").add(user).await()))

        } catch (e: Exception) {

            emit(Resource.error("user could not be added", null))
        }

    }


}