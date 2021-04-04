package com.murad.faylasoof.utils


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.murad.faylasoof.auth.models.User
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent


/**
 * this class will be used to check if the user exist on the firestore or not
 */


class ExistUtil(val context: Context, val user: User) {

    /**
     * here we will inject the firestore dependency
     */

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface FireStoreDependency {

        fun provideFireStore(): FirebaseFirestore
    }

    private fun getFirebaseFireStore(): FirebaseFirestore {

        val firebaseStore = EntryPointAccessors.fromApplication(
            context,
            FireStoreDependency::class.java
        )

        return firebaseStore.provideFireStore()
    }

    private val TAG = "ExistUtil"

    private val _userExistResult = MutableLiveData<Resource<Boolean>>()
    val userExistResult: LiveData<Resource<Boolean>> get() = _userExistResult

    fun checkIfUserExist() {
        Log.d(TAG, "checkIfUserExist: trueeeeeee")

        val firestore = getFirebaseFireStore()

        firestore.collection("users").get().addOnSuccessListener {

            for (query in it) {

                if (query.getString("email").equals(user.email)) {

                    _userExistResult.postValue(Resource.success(true))

                    break

                } else {
                    _userExistResult.postValue(Resource.error("something went wrong", false))
                }

            }
        }.addOnFailureListener {
            Log.d(TAG, "checkIfUserExist: ${it.message}")
            _userExistResult.postValue(Resource.error("something went wrong", false))
        }

    }
}