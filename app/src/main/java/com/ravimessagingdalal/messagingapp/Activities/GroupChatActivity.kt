package com.ravimessagingdalal.messagingapp.Activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ravimessagingdalal.messagingapp.Adapters.GroupMessagesAdapter
import com.ravimessagingdalal.messagingapp.Models.Message
import com.ravimessagingdalal.messagingapp.databinding.ActivityGroupChatBinding
import java.util.*
import kotlin.collections.ArrayList


class GroupChatActivity : AppCompatActivity() {

    private lateinit var binding:ActivityGroupChatBinding
    private lateinit var adapter: GroupMessagesAdapter
   private lateinit var messages: ArrayList<Message>

    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage

    private lateinit var dialog: ProgressDialog

    private lateinit var senderUid: String
    private val PICK_PHOTO_FOR_AVATAR=5
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 6
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityGroupChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val state= connectivityManager.activeNetworkInfo
        if(!(state!=null && state.isConnectedOrConnecting)){
            Toast.makeText(this,"Check your internet connnection...", Toast.LENGTH_LONG).show()
        }

        supportActionBar!!.title = "Group Chats"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        senderUid = FirebaseAuth.getInstance().uid!!
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()

        dialog = ProgressDialog(this)
        dialog!!.setMessage("Sending image...")
        dialog!!.setCancelable(false)

        messages = ArrayList()
        adapter = GroupMessagesAdapter(this, messages)
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
        RecyclerView.VERTICAL,true)
        binding.recyclerView.adapter = adapter

        database!!.reference.child("public")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message = snapshot1.getValue(Message::class.java)
                        message!!.messageId=snapshot1.key
                        messages!!.add(message!!)
                    }
                    messages.reverse()
                    adapter = GroupMessagesAdapter(this@GroupChatActivity, messages)
                    binding.recyclerView.adapter = adapter

                }

                override fun onCancelled(error: DatabaseError) {}
            })

        binding.sendBtn.setOnClickListener{
                val messageTxt = binding.messageBox.text.toString().trimEnd()
            if(messageTxt.isEmpty())
                return@setOnClickListener
                val date = Date()
                val message = Message(messageTxt, senderUid, date.getTime())
                binding.messageBox.setText("")
                database!!.reference.child("public")
                    .push()
                    .setValue(message)
            }


        binding.attachment.setOnClickListener{
            pickTheImage()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK)
         {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage: Uri? = data.data
                    val calendar: Calendar = Calendar.getInstance()
                    val reference = storage!!.reference.child("chats")
                        .child(calendar.getTimeInMillis().toString() + "")
                    dialog!!.show()
                    reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageTxt = binding.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.getTime())
                                message.message="photo"
                                message.imageUrl=filePath
                                binding.messageBox.setText("")
                                database!!.reference.child("public")
                                    .push()
                                    .setValue(message)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
    private fun pickTheImage(){
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR)
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickTheImage()
                }
                else
                    Toast.makeText(this,"Allow the permission to select an image...", Toast.LENGTH_SHORT).show()
            }
        }
    }
}