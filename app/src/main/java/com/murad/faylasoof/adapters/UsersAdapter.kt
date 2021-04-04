package com.murad.faylasoof.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.murad.faylasoof.R
import com.murad.faylasoof.auth.models.User
import com.murad.faylasoof.databinding.UserRowBinding
import java.util.ArrayList

class UsersAdapter : ListAdapter<User, UsersAdapter.UserViewHolder>(UserDiffUtil()) {


    class UserViewHolder(val view: UserRowBinding) : RecyclerView.ViewHolder(view.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val user_row = UserRowBinding.inflate(layoutInflater, parent, false)
        return UserViewHolder(user_row)
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        holder.view.user = getItem(position)
    }


    class UserDiffUtil : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.email == newItem.email

        override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem

    }
}