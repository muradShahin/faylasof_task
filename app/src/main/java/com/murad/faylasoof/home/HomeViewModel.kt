package com.murad.faylasoof.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    @Inject
    lateinit var firebaseFirestore: FirebaseFirestore

    private  val TAG = "HomeViewModel"


    fun getAllUsers(query:String):Flow<Resource<List<User>>>{


        val task : Flow<Resource<List<User>>> = flow {

            val result = getAllUsersFromDb(query)

            Log.d(TAG, "getAllUsers : Flow exceptiom reached}")

            try {

                //if(result.data?.isNotEmpty()!!)
                  emit(result)

            }catch (e:Exception){
                Log.d(TAG, "getAllUsers : Flow exceptiom ${e.message}")
                emit(Resource.error("no users found",ArrayList<User>()))
            }
        }


        return task
    }

    private suspend fun getAllUsersFromDb(query: String) : Resource<List<User>> {

       try {

          val querySnapshot= firebaseFirestore.collection("users").get().await()
          val usersList = ArrayList<User>()
           for (row in querySnapshot){

               val user = row.toObject(User::class.java)
               if(query.isNotEmpty()) {
                   if (user.first_name?.contains(query)!!) {

                       usersList.add(user)

                   }
               }else{
                   usersList.add(user)
               }

           }



           return Resource.success(usersList)

       } catch (e:Exception){

           return Resource.error(e.localizedMessage,null)
       }
    }
}