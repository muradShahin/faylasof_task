package com.murad.faylasoof.auth.signup

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.ImageResponse
import com.murad.faylasoof.helpers.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor() : ViewModel() {


    private  val TAG = "SignUpViewModel"

    @Inject
    lateinit var storageReference: StorageReference

    @Inject
    lateinit var auth: FirebaseAuth

    @Inject
    lateinit var firebaseFirestore:FirebaseFirestore

    private val _uploadImageResult = MutableLiveData<Resource<ImageResponse>>()
    val uploadImageResult :LiveData<Resource<ImageResponse>> get() = _uploadImageResult


    fun performSignUpWithGoogle(task: Task<GoogleSignInAccount>) :Flow<Resource<User>>{
        val signWithGoogleResult : Flow<Resource<User>> = flow {

           val result= handleSignInResultGoogle(task)


            emit(result)
        }

        return signWithGoogleResult

    }

    fun performCreateUser(user: User,password: String):Flow<AuthResult>{

        val createUserResult : Flow<AuthResult> = flow {

            val result = createUser(user,password)
            emit(result)
        }

        return createUserResult
    }

    fun performCreateUserInFireStore(user: User):Flow<Resource<DocumentReference>>{

        val createUserResult :Flow<Resource<DocumentReference>> = flow {
           val result = createUserInFireStore(user)

            emit(result)
        }
        return createUserResult
    }

    private fun handleSignInResultGoogle(task:Task<GoogleSignInAccount>) :Resource<User>{

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







     fun uploadImageToFirebase(imageUri:String){

        storageReference = storageReference.child("/profileImages").child("image"+ UUID.randomUUID())


        viewModelScope.launch(Dispatchers.IO) {
            /**
             * first we upload the image using putFile
             */


               storageReference.putFile(imageUri.toUri()).addOnSuccessListener {

                   /**
                    * here we retrieve the download Uri for the image we just uploaded
                    */
                   storageReference.downloadUrl.addOnSuccessListener {

                       Log.d(TAG, "uploadImageToFirebase: reached")
                       _uploadImageResult.value = Resource.success(ImageResponse(true, it.toString()))
                   }.addOnFailureListener {

                       _uploadImageResult.value  = Resource.error("failed", ImageResponse(false, ""))

                   }

               }.addOnFailureListener {
                   _uploadImageResult.value  = Resource.error("failed", ImageResponse(false, ""))

               }

        }

    }


   private suspend fun createUser(user: User,password:String):AuthResult{


       return  auth.createUserWithEmailAndPassword(user.email!!,password).await()


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