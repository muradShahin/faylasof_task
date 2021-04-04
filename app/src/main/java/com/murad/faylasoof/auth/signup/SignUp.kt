package com.murad.faylasoof.auth.signup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.ExistUtil
import com.murad.faylasoof.utils.Resource
import com.murad.faylasoof.utils.Status
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.sign_in.*
import javax.inject.Inject


@AndroidEntryPoint
class SignUp : Fragment() {


    private val viewModel: SignUpViewModel by viewModels()
    private val TAG = "SignUp"
    private var selectedUserImage: String? = ""
    private var selectedImageDownloadUri: String? = null
    private lateinit var callbackManager: CallbackManager


    var genders =
        arrayOf("None", "Male", "female")

    private val PERMISSION = mutableListOf<String>("email")

    val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            handleSignIn(task)

        }

    val cameraShowResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {


            if (it.resultCode == Activity.RESULT_OK) {

                val selectedImage = it.data
                imageView2.setImageURI(selectedImage?.data)
                selectedUserImage = selectedImage?.data.toString()

                viewModel.uploadImageToFirebase(selectedUserImage!!)


            }

        }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        spinner.adapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, genders)


        google_btn.setOnClickListener {

            signWithGoogle()
        }

        auth_sign.setOnClickListener {

            auth_sign.visibility = View.GONE
            constraintLayout.visibility = View.VISIBLE
        }

        textView2.setOnClickListener {
            showDataPicker()
        }

        imageView2.setOnClickListener {

            pickFromCamera()
        }

        login.setOnClickListener {

            createUserAuth()
        }


        fb_btn.setOnClickListener {
            doFbLogin()
        }


        /**
         * observing the image if uploaded successfully or not
         */
        viewModel.getImageDownloadUri().observe(viewLifecycleOwner,
            Observer<String> {

                selectedImageDownloadUri = it
            })


    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {

        viewModel.loginWithFaceBook(accessToken).observe(viewLifecycleOwner,
            Observer<Resource<AuthResult>> {

                if (it.status == Status.SUCCESS) {

                    Toast.makeText(requireContext(), "success", Toast.LENGTH_SHORT).show()

                } else {
                    Toast.makeText(requireContext(), "failed", Toast.LENGTH_SHORT).show()
                }

            })


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

    private fun handleSignIn(task: Task<GoogleSignInAccount>) {


        viewModel.getSignUpWithGoogle(task).observe(viewLifecycleOwner,
            Observer<Resource<User>> {
                if (it.status == Status.SUCCESS) {
                    it.data?.let { it1 -> checkIfUserExist(it1) }
                } else {
                    Toast.makeText(requireContext(), "failed", Toast.LENGTH_SHORT).show()
                }
            })


    }

    private fun showDataPicker() {

        val dialog = DatePickerFragmentDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->

            textView2.text = "$year/${monthOfYear + 1}/$dayOfMonth"

        }, 2021, 11, 4)

        dialog.show(requireActivity().supportFragmentManager, "tag")

    }

    private fun pickFromCamera() {

        val intent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        cameraShowResult.launch(intent)
    }


    private fun validateEmail(): Boolean {
        return email_input.text?.isNotEmpty()!!
    }

    private fun validateName(): Boolean {
        return firstname.text?.isNotEmpty()!!
    }

    private fun validateProfilePic(): Boolean {
        return selectedImageDownloadUri != null
    }

    private fun createUserAuth() {
        if (validateEmail())
            if (validateName())
                if (validateProfilePic()) {
                    val firstName = firstname.text.toString()
                    val secondName = secondname.text.toString()
                    val email = email_input.text.toString()
                    val dateOfBirth = textView2.text.toString()
                    val profilePic = selectedImageDownloadUri
                    val gender = genders[spinner.selectedItemPosition]
                    val password = passwordInput.text.toString()
                    val user = User(firstName, secondName, email, profilePic, dateOfBirth, gender)

                    viewModel.createUser(user, password).observe(viewLifecycleOwner,
                        Observer<Resource<AuthResult>> {

                            if (it.status == Status.SUCCESS) {
                                addUserToFireStore(user)
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "failed to create user",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        })

                } else
                    Toast.makeText(
                        requireContext(),
                        "Please upload a profile pic",
                        Toast.LENGTH_SHORT
                    ).show()
            else
                firstname.error = "required"
        else
            email.error = "required"
    }

    private fun checkIfUserExist(user: User) {
        val existUtil = ExistUtil(requireContext(), user)
        existUtil.checkIfUserExist()
        Toast.makeText(requireContext(), user.email, Toast.LENGTH_SHORT).show()

        existUtil.userExistResult.observe(viewLifecycleOwner, Observer<Resource<Boolean>> {

            if (it.status == Status.SUCCESS) {

                view?.findNavController()?.navigate(SignUpDirections.actionSignUpToLoginView())

            } else {
                addUserToFireStore(user)
            }

        })

    }

    private fun addUserToFireStore(user: User) {

        viewModel.createUserInFirestore(user).observe(viewLifecycleOwner,
            Observer<Resource<DocumentReference>> {

                if (it.status == Status.SUCCESS) {
                    Toast.makeText(requireContext(), "added successfully", Toast.LENGTH_SHORT)
                        .show()
                    view?.findNavController()?.navigate(SignUpDirections.actionSignUpToLoginView())

                } else {
                    Toast.makeText(requireContext(), "failed", Toast.LENGTH_SHORT).show()
                }

            })


    }


    private fun doFbLogin() {

        /**
         * facebook configurations
         */

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(requireActivity(), PERMISSION)
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult?) {
                    Toast.makeText(
                        requireContext(),
                        "onSuccess ${result?.accessToken}",
                        Toast.LENGTH_SHORT
                    ).show()

                    result?.accessToken?.let { handleFacebookAccessToken(it) }

                }

                override fun onCancel() {
                    Log.d(TAG, "onSuccess: facebook cancel")

                    Log.d(TAG, "facebook:onCancel");
                }

                override fun onError(error: FacebookException?) {
                    Log.d(TAG, "onSuccess: facebook error")

                    Log.d(TAG, "facebook:onError", error)
                }


            })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Toast.makeText(requireContext(), "on activity result ${requestCode}", Toast.LENGTH_SHORT)
            .show()
        callbackManager.onActivityResult(requestCode, Activity.RESULT_OK, data)

    }


}