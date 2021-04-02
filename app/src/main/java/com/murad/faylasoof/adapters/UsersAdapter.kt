package com.murad.faylasoof.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.databinding.UserRowBinding
import java.util.ArrayList

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder>(){

    private var users = ArrayList<User>()

    fun submitUsers(users:List<User>){

        this.users = users as ArrayList<User>
        notifyDataSetChanged()
    }

    class UserViewHolder(val view:UserRowBinding):RecyclerView.ViewHolder(view.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val user_row = UserRowBinding.inflate(layoutInflater,parent,false)
        return UserViewHolder(user_row)
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        holder.view.user = users[position]
    }
}