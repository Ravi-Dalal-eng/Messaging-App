package com.ravimessagingdalal.messagingapp.Utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ravimessagingdalal.messagingapp.Models.Status
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.Models.UserStatus

class MyViewModel:ViewModel() {
    private val database=FirebaseDatabase.getInstance()
    private val users: MutableLiveData<List<User>> by lazy {
        MutableLiveData<List<User>>().also {
            loadUsers()
        }
    }

    fun getUsers(): LiveData<List<User>> {
        return users
    }

    private fun loadUsers() {
        database.getReference().child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list=ArrayList<User>()

                for (snapshot1 in snapshot.children) {
                    val user = snapshot1.getValue(
                        User::class.java
                    )
                    if (!user?.uid.equals(FirebaseAuth.getInstance().uid))
                        list.add(user!!)
                }
               users.postValue(list)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }





    private val status: MutableLiveData<List<UserStatus>> by lazy {
        MutableLiveData<List<UserStatus>>().also {
            loadStatus()
        }
    }

    fun getStatus(): LiveData<List<UserStatus>> {
        return status
    }

    private fun loadStatus() {


        database.reference.child("stories").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list=ArrayList<UserStatus>()
                if (snapshot.exists()) {
                    for (storySnapshot in snapshot.children) {
                        val status = UserStatus()
                        status.name = storySnapshot.child("name").getValue(String::class.java)
                        status.profileImage = storySnapshot.child("profileImage").getValue(
                            String::class.java
                        )
                        status.userId=storySnapshot.child("userId").getValue(String::class.java)
                        status.lastUpdated =
                            storySnapshot.child("lastUpdated").getValue(Long::class.java)!!!!
                        val statuses: ArrayList<Status> = ArrayList()
                        for (statusSnapshot in storySnapshot.child("statuses").children) {
                            val sampleStatus = statusSnapshot.getValue(Status::class.java)
                            statuses.add(sampleStatus!!)
                        }
                        status.statuses = statuses
                        if(status.userId.equals(FirebaseAuth.getInstance().uid))
                            list.add(0,status)
                        else
                            list.add(status)
                    }

                }
                status.postValue(list)

            }

            override fun onCancelled(error: DatabaseError) {}
        })


    }




}