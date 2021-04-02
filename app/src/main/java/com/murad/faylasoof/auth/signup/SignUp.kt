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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
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
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.helpers.ExistUtil
import com.murad.faylasoof.helpers.ImageResponse
import com.murad.faylasoof.helpers.Resource
import com.murad.faylasoof.helpers.Status
import com.shagi.materialdatepicker.date.DatePickerFragmentDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.sign_in.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SignUp : Fragment() {


    private lateinit var viewModel:SignUpViewModel
    private  val TAG = "SignUp"
    private var selectedUserImage:String?=""
    private var selectedImageDownloadUri :String? = null
    private lateinit var callbackManager:CallbackManager

    @Inject
    lateinit var auth:FirebaseAuth

    var genders =
        arrayOf("None", "Male","female")

    private val PERMISSION = mutableListOf<String>("email")

    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){

        val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
        handleSignIn(task)

    }

    val cameraShowResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){


        if(it.resultCode == Activity.RESULT_OK ){

            val selectedImage = it.data
            imageView2.setImageURI(selectedImage?.data)
            selectedUserImage = selectedImage?.data.toString()!!

            viewModel.uploadImageToFirebase(selectedUserImage!!)


        }

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


        spinner.adapter = ArrayAdapter(requireActivity(),android.R.layout.simple_spinner_dropdown_item,genders)


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

        viewModel.uploadImageResult.observe(requireActivity(),
            Observer<Resource<ImageResponse>> {

                if(it.status == Status.SUCCESS){

                    Toast.makeText(requireContext(),"Uploaded Successfully !",Toast.LENGTH_SHORT).show()
                    selectedImageDownloadUri = it.data?.downloadUri

                }else{
                    Toast.makeText(requireContext(),"failed to upload the image",Toast.LENGTH_SHORT).show()

                }

            })


    }

    private fun handleFacebookAccessToken(accessToken: AccessToken) {

        lifecycleScope.launch {

            viewModel.performFaceBookLogin(accessToken).collect {

                Log.d(TAG, "handleFacebookAccessToken: ${it.data?.additionalUserInfo}")
                if(it.status == Status.SUCCESS){

                    Toast.makeText(requireContext(),"logged in successfully",Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(requireContext(),"facebook failed",Toast.LENGTH_SHORT).show()

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
            viewModel.performSignUpWithGoogle(task).collect {
                Toast.makeText(requireContext(),"success",Toast.LENGTH_SHORT).show()
                Log.d(TAG, "handleSignIn: ${it.data?.first_name}")

                checkIfUserExist(it.data!!)

            }

        }


    }

    private fun showDataPicker(){

        val dialog = DatePickerFragmentDialog.newInstance({ view, year, monthOfYear, dayOfMonth ->

            textView2.text = "$year/${monthOfYear+1}/$dayOfMonth"

        }, 2021, 11, 4)

        dialog.show(requireActivity().supportFragmentManager, "tag")

    }

    private fun pickFromCamera(){

        val intent =Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        cameraShowResult.launch(intent)
    }


    private fun validateEmail(): Boolean {
        return email_input.text?.isNotEmpty()!!
    }
    private fun validateName():Boolean{
        return firstname.text?.isNotEmpty()!!
    }

    private fun validateProfilePic():Boolean{
        return selectedImageDownloadUri?.isNotEmpty()!!
    }

    private fun createUserAuth(){
        if(validateEmail())
            if(validateName())
                if(validateProfilePic()){
                    val firstName = firstname.text.toString()
                    val secondName = secondname.text.toString()
                    val email = email_input.text.toString()
                    val dateOfBirth = textView2.text.toString()
                    val profilePic=selectedImageDownloadUri
                    val gender = genders[spinner.selectedItemPosition]
                    val password =passwordInput.text.toString()
                    val user = User(firstName , secondName,email,profilePic,dateOfBirth,gender)

                    lifecycleScope.launch {
                        viewModel.performCreateUser(user,password).collect {

                            if(it.user !=null){
                                Toast.makeText(requireActivity(),"Created Successfully",Toast.LENGTH_SHORT).show()
                                addUserToFireStore(user)

                            }else{
                                Toast.makeText(requireActivity(),"Could not create your account",Toast.LENGTH_SHORT).show()

                            }
                        }

                    }

                } else
                    Toast.makeText(requireContext(),"Please upload a profile pic",Toast.LENGTH_SHORT).show()
            else
                firstname.error = "required"
        else
            email.error ="required"
    }

    private fun checkIfUserExist(user: User) {
        val existUtil = ExistUtil(requireContext(),user)
        existUtil.checkIfUserExist()

        existUtil.userExistResult.observe(viewLifecycleOwner, Observer<Resource<Boolean>> {

             if(it.status == Status.SUCCESS){

                 view?.findNavController()?.navigate(SignUpDirections.actionSignUpToLoginView())

             }else{
                 addUserToFireStore(user)
             }

        })

    }

    private fun addUserToFireStore(user: User){

        lifecycleScope.launch {

            viewModel.performCreateUserInFireStore(user).collect {
                if(it.status == Status.SUCCESS){
                    Toast.makeText(requireContext(),"created successfully in firestore",Toast.LENGTH_SHORT).show()

                    view?.findNavController()?.navigate(SignUpDirections.actionSignUpToLoginView())

                }
            }

        }


    }


    private fun doFbLogin(){

        /**
         * facebook configurations
         */

        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance().logInWithReadPermissions(requireActivity(),PERMISSION)
        LoginManager.getInstance().registerCallback(callbackManager,object :FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                Toast.makeText(requireContext(),"onSuccess ${result?.accessToken}",Toast.LENGTH_SHORT).show()

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
        Toast.makeText(requireContext(),"on activity result ${requestCode}",Toast.LENGTH_SHORT).show()
        callbackManager.onActivityResult(requestCode,Activity.RESULT_OK,data)

    }



}