package com.murad.faylasoof.auth.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.ExistUtil
import com.murad.faylasoof.utils.Resource
import com.murad.faylasoof.utils.Status
import com.murad.faylasoof.home.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.login_view.*


@AndroidEntryPoint
class LoginView : Fragment() {

    /**
     * to handle google sign in call back
     */
    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignIn(task)

        }

    private val TAG = "LoginView"

    private  val viewModel: LoginViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        google_btn.setOnClickListener {

            signWithGoogle()
        }

        button2.setOnClickListener {

            if(validateEmail())
                 if(validatePassword())
                      loginWithEmailAndPassword()
                 else
                     passwordLay.error = "required"
            else
                emailLay.error = "required"
        }

        button.setOnClickListener {
            getView()?.findNavController()?.navigate(LoginViewDirections.actionLoginViewToSignUp())
        }

    }

    private fun loginWithEmailAndPassword() {
        showProgressBar()
        val email = email_input.text.toString()
        val password = passwordInput.text.toString()

        viewModel.loginWithEmailAndPassword(email, password).observe(viewLifecycleOwner,
            Observer<Resource<Any>> {

                if (it.status == Status.SUCCESS) {
                    hideProgressBar()
                    goToHomeActivity()
                } else {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "failed to login", Toast.LENGTH_SHORT).show()
                }

            })

    }





    private fun signWithGoogle() {

        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(Scope(Scopes.PLUS_LOGIN))
                .build()

            val googleClient = GoogleSignIn.getClient(requireActivity(), gso)

            val signInIntent: Intent = googleClient.signInIntent
            resultLauncher.launch(signInIntent)


        } catch (e: Exception) {
            Log.d(TAG, "signWithGoogle: ${e.message}")
        }


    }

    private fun handleSignIn(task: Task<GoogleSignInAccount>) {
        showProgressBar()
        viewModel.loginWithGoogle(task).observe(viewLifecycleOwner,
            Observer<Resource<User>> {

                val existUtil = it.data?.let { it1 -> ExistUtil(requireContext(), it1) }
                existUtil?.checkIfUserExist()
                existUtil?.userExistResult?.observe(viewLifecycleOwner,
                    Observer<Resource<Boolean>> {result->

                        if (result.status == Status.SUCCESS) {

                            hideProgressBar()
                            goToHomeActivity()

                        } else {

                            result.data?.let { user -> addUserToDb(existUtil.user) }
                        }

                    })
            })


    }

    private fun addUserToDb(user: User) {
        showProgressBar()
        viewModel.addUserToDb(user).observe(viewLifecycleOwner,
            Observer<Resource<DocumentReference>> {result->

                if (result.status == Status.SUCCESS) {
                    hideProgressBar()
                    goToHomeActivity()
                } else {
                    hideProgressBar()
                    Toast.makeText(requireContext(), "failed to login", Toast.LENGTH_SHORT).show()
                }

            })

    }

    private fun goToHomeActivity() {

        val intent = Intent(requireActivity(), HomeActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showProgressBar() {
        progressBar2.visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        progressBar2.visibility = View.GONE
    }

    private fun validateEmail() = email_input.text.toString().isNotEmpty()
    private fun validatePassword() = passwordInput.text.toString().isNotEmpty()

}