package com.murad.faylasoof.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.murad.faylasoof.R
import com.murad.faylasoof.adapters.UsersAdapter
import com.murad.faylasoof.helpers.MyTextWatcher
import com.murad.faylasoof.helpers.MyWatcher
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class home_fragment : Fragment() {

    private  val TAG = "home_fragment"
    lateinit var viewModel:HomeViewModel
    lateinit var userAdapter:UsersAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.home_fragment,container,false) }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        userAdapter = UsersAdapter()

        users_rec.apply {
            layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
            adapter =userAdapter
        }

        getAllUsers("")


        search_text.addTextChangedListener(MyTextWatcher(object :MyWatcher{
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                getAllUsers(s.toString())
            }

        }))
    }

    private fun getAllUsers(query:String){
        progressBar.visibility=View.VISIBLE
        lifecycleScope.launch {

            viewModel.getAllUsers(query).collect {

                Log.d(TAG, "getAllUsers: ${it.data?.get(0)?.first_name}  || ${it.status} || ${it.message}")

                it.data?.let { it1 -> userAdapter.submitUsers(it1) }
                progressBar.visibility=View.GONE

            }

        }


    }


}