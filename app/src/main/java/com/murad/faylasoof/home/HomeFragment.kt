package com.murad.faylasoof.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.murad.faylasoof.R
import com.murad.faylasoof.adapters.UsersAdapter
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.utils.MyTextWatcher
import com.murad.faylasoof.utils.MyWatcher
import com.murad.faylasoof.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.home_fragment.*

@AndroidEntryPoint
class HomeFragment : Fragment() {

    lateinit var userAdapter: UsersAdapter

    val viewModel: HomeViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home_fragment, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userAdapter = UsersAdapter()
        users_rec.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = userAdapter
        }

        getAllUsers()


        search_text.addTextChangedListener(MyTextWatcher(object : MyWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                progressBar.visibility = View.VISIBLE
                viewModel.searchForUsers(s.toString()).observe(viewLifecycleOwner,
                    Observer<Resource<List<User>>> {result->

                        progressBar.visibility = View.GONE

                        result.data?.let { list -> userAdapter.submitList(list) }

                    })
            }

        }))


    }

    private fun getAllUsers() {
        progressBar.visibility = View.VISIBLE

        viewModel.getUsers().observe(viewLifecycleOwner,
            Observer<Resource<List<User>>> {result->

                progressBar.visibility = View.GONE
                result.data?.let { list -> userAdapter.submitList(list) }
            })
    }


}