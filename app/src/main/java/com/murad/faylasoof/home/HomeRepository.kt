package com.murad.faylasoof.home

import com.google.firebase.firestore.FirebaseFirestore
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class HomeRepository @Inject constructor(val fireStore: FirebaseFirestore) {


    private val TAG = "HomeRepository"

    /**
     * fetch all users
     */

    fun fetchAllUsers(): Flow<Resource<List<User>>> = flow {

        emit(getAllUsersFromDb())
    }


    private suspend fun getAllUsersFromDb(): Resource<List<User>> {

        return try {

            val querySnapshot = fireStore.collection("users").get().await()
            val usersList = querySnapshot.toObjects(User::class.java)
            Resource.success(usersList)

        } catch (e: Exception) {

            Resource.error(e.localizedMessage, null)
        }

    }


    /**
     * fetch users on search
     */
    fun fetchUsersOnSearch(query: String): Flow<Resource<List<User>>> = flow {

        emit(searchForUser(query))
    }


    private suspend fun searchForUser(query: String): Resource<List<User>> {

        try {

            val querySnapshot = fireStore.collection("users").get().await()
            val usersList = querySnapshot.toObjects(User::class.java)
            val usersInSearch = ArrayList<User>()
            for (user in usersList) {

                if (user.first_name?.toLowerCase(Locale.getDefault())
                        ?.contains(query.toLowerCase(Locale.getDefault()))!!
                    || user.lastname?.toLowerCase(Locale.getDefault())
                        ?.contains(query.toLowerCase(Locale.getDefault()))!!
                ) {
                    usersInSearch.add(user)
                }

            }

            return Resource.success(usersInSearch)

        } catch (e: Exception) {

            return Resource.error(e.localizedMessage, null)
        }

    }


}