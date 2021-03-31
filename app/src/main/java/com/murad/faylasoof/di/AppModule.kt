package com.murad.faylasoof.di

import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun providesFirestore() = Firebase.firestore


    @Singleton
    @Provides
    fun providesFirebaseAuth() = Firebase.auth


    @Singleton
    @Provides
    fun providesFirebaseStorageReference():StorageReference{
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference

        return storageRef
    }

}