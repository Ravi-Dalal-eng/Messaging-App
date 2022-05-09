package com.ravimessagingdalal.messagingapp.Adapters


import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ravimessagingdalal.messagingapp.Activities.ChatActivity
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.databinding.ItemUsersBinding
import java.sql.Date
import java.text.SimpleDateFormat
import kotlin.collections.ArrayList


class UsersAdapter(context: Context, users: ArrayList<User>) :
    RecyclerView.Adapter<UsersAdapter.UsersViewHolder>() {
    var context: Context
    var users: ArrayList<User>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.item_users, parent, false)
        return UsersViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {
        val user = users[position]
        val senderId = FirebaseAuth.getInstance().uid
        val senderRoom = senderId + user.uid
        FirebaseDatabase.getInstance().reference
            .child("chats")
            .child(senderRoom)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child("lastMsg").getValue(
                            String::class.java
                        )
                        val time = snapshot.child("lastMsgTime").getValue(
                            Long::class.java
                        )!!
                        val dateFormat = SimpleDateFormat("hh:mm a")
                        holder.binding.msgTime.setText(dateFormat.format(Date(time)))
                        holder.binding.lastMsg.setText(lastMsg)
                    } else {
                        holder.binding.lastMsg.setText("Tap to chat")
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        holder.binding.username.setText(user.name)
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.avatar)
            .into(holder.binding.profile)
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("name", user.name)
                intent.putExtra("image", user.profileImage)
                intent.putExtra("uid", user.uid)
                intent.putExtra("token", user.token)
                context.startActivity(intent)
            }
        })
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UsersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemUsersBinding

        init {
            binding = ItemUsersBinding.bind(itemView)
        }
    }

    init {
        this.context = context
        this.users = users
    }
}