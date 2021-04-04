package com.murad.faylasoof.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    val homeRepository: HomeRepository
) : ViewModel() {

    init {
        getUsers()
    }

    fun getUsers() = homeRepository.fetchAllUsers().asLiveData()


    fun searchForUsers(query: String) = homeRepository.fetchUsersOnSearch(query).asLiveData()
}