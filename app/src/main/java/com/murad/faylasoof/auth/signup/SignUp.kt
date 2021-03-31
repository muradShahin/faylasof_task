package com.murad.faylasoof.auth.signup

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.murad.faylasoof.R
import kotlinx.android.synthetic.main.sign_in.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class SignUp : Fragment() {


    private lateinit var viewModel:SignUpViewModel
    private  val TAG = "SignUp"

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        handleSignIn(task)

    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
       return inflater.inflate(R.layout.sign_in,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)

        sign_google.setOnClickListener {

            signWithGoogle()
        }
    }

    private fun signWithGoogle() {

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
            .requestScopes(Scope(Scopes.PLUS_LOGIN))
                .build()

        val googleClient = GoogleSignIn.getClient(requireActivity(), gso)

        val signInIntent: Intent = googleClient.signInIntent
        resultLauncher.launch(signInIntent)


    }

    private fun handleSignIn(task : Task<GoogleSignInAccount>){


        lifecycleScope.launch {
            viewModel.performSignUpWithGoogle(task).collect {
                Toast.makeText(requireContext(),"success",Toast.LENGTH_SHORT).show()
                Log.d(TAG, "handleSignIn: ${it.data?.first_name}")
            }

        }


    }
}