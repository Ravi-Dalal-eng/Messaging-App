package com.ravimessagingdalal.messagingapp.Activities

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationBarView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.ravimessagingdalal.messagingapp.Fragments.StatusFragment
import com.ravimessagingdalal.messagingapp.Fragments.UsersFragment
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       if(savedInstanceState!=null){
    val id=savedInstanceState.getInt("ITEM_ID")
           if(id==R.id.chats)
               supportFragmentManager.beginTransaction().replace(binding.frameLayout.id,UsersFragment())
                   .commit()
        else
           supportFragmentManager.beginTransaction().replace(binding.frameLayout.id,StatusFragment())
               .commit()
       }
        else
           supportFragmentManager.beginTransaction().replace(binding.frameLayout.id,UsersFragment())
               .commit()



        database= FirebaseDatabase.getInstance()



        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->
                val map: HashMap<String, Any> = HashMap()
                map["token"] = token
                database.reference
                    .child("users")
                    .child(FirebaseAuth.getInstance().uid!!)
                    .updateChildren(map)
            }



        binding.bottomNavigationView
            .setOnItemSelectedListener(object :NavigationBarView.OnItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    if (!item.isChecked) {
                        when (item.itemId) {
                            R.id.chats -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(binding.frameLayout.id, UsersFragment()).commit()
                                return true
                            }
                            R.id.status -> {
                                supportFragmentManager.beginTransaction()
                                    .replace(binding.frameLayout.id, StatusFragment()).commit()
                                return true
                            }
                        }
                    }
                        return false
                }
            })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database.getReference().child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database.getReference().child("presence").child(currentId!!).setValue("Offline")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.group -> startActivity(Intent(this@MainActivity, GroupChatActivity::class.java))
            R.id.logout ->{
                FirebaseAuth.getInstance().signOut()
                Thread.sleep(500)
                startActivity(Intent(this@MainActivity, LogInActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.topmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("ITEM_ID",binding.bottomNavigationView.selectedItemId)
    }
}