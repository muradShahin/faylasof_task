package com.murad.faylasoof.auth.signup

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.Resource
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class SignUpRepository @Inject constructor(

    val auth: FirebaseAuth,
    val fireStore: FirebaseFirestore,
    val storageReference: StorageReference
) {

    private val TAG = "SignUpRepository"
    private val _imageDownloadLink = MutableLiveData<String>()
    val imageDownloadLink: LiveData<String> get() = _imageDownloadLink

    fun signUpWithGoogle(task: Task<GoogleSignInAccount>) = flow {

        try {
            val account = task.getResult(ApiException::class.java)

            val first_name = account?.givenName
            val second_name = account?.familyName
            val email = account?.email
            val profile_pic = account?.photoUrl.toString()

            Log.d(TAG, "signUpWithGoogle: ${email}")
            emit(Resource.success(User(first_name, second_name, email, profile_pic)))

        } catch (e: Exception) {

            emit(Resource.error("Could not signUp with google", null))
        }

    }


    fun uploadImageToFirebase(imageUri: String) {

        try {

            storageReference.child("/profileImages").child("image" + UUID.randomUUID())
            storageReference.putFile(imageUri.toUri()).addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener {

                    _imageDownloadLink.value = it.toString()
                }


            }


        } catch (e: Exception) {

            Resource.error("could not upload photo", null)
        }


    }


    fun createUser(user: User, password: String) = flow {

        try {

            emit(
                Resource.success(
                    auth.createUserWithEmailAndPassword(user.email, password).await()
                )
            )

        } catch (e: Exception) {
            emit(Resource.error("could not create user", null))
        }

    }

    fun createUserInFirestore(user: User) = flow {

        try {

            emit(Resource.success(fireStore.collection("users").add(user).await()))


        } catch (e: Exception) {
            emit(Resource.error("could not create user", null))
        }

    }

    fun handleFacebookAccessToken(token: AccessToken) = flow {
        val credential = FacebookAuthProvider.getCredential(token.token)
        try {

            emit(Resource.success(auth.signInWithCredential(credential).await()))

        } catch (e: java.lang.Exception) {

            emit(Resource.error("failed to handle facebook login", null))
        }

    }


}