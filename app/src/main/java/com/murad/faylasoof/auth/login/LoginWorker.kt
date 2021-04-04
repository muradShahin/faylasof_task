package com.murad.faylasoof.auth.login

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.CountDownLatch

class LoginWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {


    override fun doWork(): Result {

        val firebaseAuth = Firebase.auth
        val countDownLatch = CountDownLatch(2)

        val data = inputData
        val email=data.getString("email")
        val password =data.getString("pass")

       try {

           firebaseAuth.signInWithEmailAndPassword(email!!,password!!)
               .addOnSuccessListener {

                   countDownLatch.countDown()
               }.addOnFailureListener {

                   countDownLatch.countDown()
               }


       }catch (e:Exception){

       }

        countDownLatch.await()
        return Result.success()
    }
}