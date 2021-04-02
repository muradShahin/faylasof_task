package com.murad.faylasoof.auth.login

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.ExistUtil
import com.murad.faylasoof.helpers.Resource
import com.murad.faylasoof.helpers.Status
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_view.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.*


@AndroidEntryPoint
class LoginView : Fragment() {


    /**
     * to handle google sign in call back
     */
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        handleSignIn(task)

    }

    private  val TAG = "LoginView"

    private lateinit var viewModel: LoginViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_view,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)



        google_btn.setOnClickListener {

            signWithGoogle()
        }


        button2.setOnClickListener {

            loginWithEmailAndPassword()
        }











        button.setOnClickListener {
            getView()?.findNavController()?.navigate(LoginViewDirections.actionLoginViewToSignUp())
        }

    }

    private fun loginWithEmailAndPassword() {

        val email = email_input.text.toString()
        val password = passwordInput.text.toString()

        lifecycleScope.launch {

            viewModel.loginWithEmailAndPass(email,password).collect {

                if(it.status == Status.SUCCESS){
                    /**
                     * navigate to home
                     */
                }else{
                    Toast.makeText(requireContext(),"could not login",Toast.LENGTH_SHORT).show()
                }

            }
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
            viewModel.loginWithGoogle(task).collect {
                Toast.makeText(requireContext(),"success", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "handleSignIn: ${it.data?.first_name}")

                val user = it.data
               val existUtil = ExistUtil(requireContext(),user!!)

                existUtil.checkIfUserExist()
                existUtil.userExistResult.observe(viewLifecycleOwner, Observer<Resource<Boolean>> {

                    if(it.status == Status.SUCCESS){

                        /**
                         * user already exist on our database so no need to add it again
                         */

                        // navigate to home activity

                    }else{
                        addUserToDb(user)
                    }

                })
            }

        }


    }

    private fun addUserToDb(user: User) {

        lifecycleScope.launch {

            viewModel.performAddUserToDb(user).collect {

                if(it.status == Status.SUCCESS){

                    //navigate to home

                }else{

                    Toast.makeText(requireContext(),"we ran ito a problem please try again",Toast.LENGTH_SHORT).show()
                }
            }

        }

    }


}