package com.ravimessagingdalal.messagingapp.Activities

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ravimessagingdalal.messagingapp.Adapters.MessagesAdapter
import com.ravimessagingdalal.messagingapp.Models.Message
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.databinding.ActivityChatBinding
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class ChatActivity : AppCompatActivity() {
   private lateinit var binding: ActivityChatBinding
    private lateinit var adapter: MessagesAdapter
    private lateinit var messages: ArrayList<Message>
    private lateinit var senderRoom: String
   private lateinit var receiverRoom: String
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
   private lateinit var dialog: ProgressDialog
    private lateinit var senderUid: String
   private lateinit var receiverUid: String
   private lateinit var token: String
    private lateinit var name: String
    private lateinit var myName:String
    private lateinit var profile: String
    private val PICK_PHOTO_TO_SEND=55
    private val  READ_EXTERNAL_STORAGE_TO_SEND=54
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val state= connectivityManager.activeNetworkInfo
        if(!(state!=null && state.isConnectedOrConnecting)){
            Toast.makeText(this,"Check your internet connnection...", Toast.LENGTH_LONG).show()
        }

        setSupportActionBar(binding.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Sending image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
         name = intent.getStringExtra("name").toString()
         profile = intent.getStringExtra("image").toString()
         token = intent.getStringExtra("token").toString()

        //Toast.makeText(this, token, Toast.LENGTH_SHORT).show();
        binding.name.setText(name)
        Glide.with(this).load(profile)
            .placeholder(R.drawable.avatar)
            .into(binding.profile)
        binding.imageView2.setOnClickListener{
            finish()
        }

        database.getReference().child("users").child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    myName = snapshot.getValue(User::class.java)!!.name.toString()
                }

                override fun onCancelled(error: DatabaseError) {}
            })



        receiverUid = intent.getStringExtra("uid").toString()
        senderUid = FirebaseAuth.getInstance().uid!!
        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (!status!!.isEmpty()) {
                            if (status == "Offline") {
                                binding.status.setVisibility(View.GONE)
                            } else {
                                binding.status.setText(status)
                                binding.status.setVisibility(View.VISIBLE)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this, messages!!, senderRoom!!, receiverRoom!!)
        binding.recyclerView.layoutManager = LinearLayoutManager(this,
            RecyclerView.VERTICAL,true)
        binding.recyclerView.adapter = adapter

        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val message= snapshot1.getValue(Message::class.java)
                        message!!.messageId=snapshot1.key
                        messages!!.add(message)
                    }
                    messages.reverse()
                    adapter = MessagesAdapter(this@ChatActivity, messages!!, senderRoom!!, receiverRoom!!)
                    binding.recyclerView.adapter = adapter

                }

                override fun onCancelled(error: DatabaseError) {}
            })
        binding.sendBtn.setOnClickListener(object : View.OnClickListener {
          override  fun onClick(v: View?) {
                val messageTxt: String = binding.messageBox.getText().toString().trimEnd()
              if(messageTxt.isEmpty())
                  return
                val date = Date()
                val message = Message(messageTxt, senderUid, date.getTime())
                binding.messageBox.setText("")
                val randomKey = database!!.reference.push().key
                val lastMsgObj: HashMap<String, Any> = HashMap()
                lastMsgObj["lastMsg"] = message.message!!
                lastMsgObj["lastMsgTime"] = date.getTime()
                database!!.reference.child("chats").child(senderRoom!!).updateChildren(lastMsgObj)
                database!!.reference.child("chats").child(receiverRoom!!).updateChildren(lastMsgObj)
                database!!.reference.child("chats")
                    .child(senderRoom!!)
                    .child("messages")
                    .child(randomKey!!)
                    .setValue(message).addOnSuccessListener {
                        database!!.reference.child("chats")
                            .child(receiverRoom!!)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message).addOnSuccessListener {
                                sendNotification(
                                    myName,
                                    message.message,
                                    token
                                )
                            }
                    }
            }
        })
        binding.attachment.setOnClickListener {
pickImageToSend()
        }
        val handler = Handler()
        binding.messageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                database!!.reference.child("presence").child(senderUid!!).setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping =
                Runnable {
                    database!!.reference.child("presence").child(senderUid!!).setValue("Online")
                }
        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)


    }

    private fun pickImageToSend() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, PICK_PHOTO_TO_SEND)
        }
        else{
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_TO_SEND
            )
        }
    }

    fun sendNotification(name: String?, message: String?, token: String?) {
        try {
            val queue = Volley.newRequestQueue(this)
            val url = "https://fcm.googleapis.com/fcm/send"
            val data = JSONObject()
            data.put("title", name)
            data.put("body", message)
            val notificationData = JSONObject()
            notificationData.put("notification", data)
            notificationData.put("to", token)
            val request: JsonObjectRequest =
                object : JsonObjectRequest(url, notificationData, object : Response.Listener<JSONObject?> {
                 override   fun onResponse(response: JSONObject?) {
                        // Toast.makeText(ChatActivity.this, "success", Toast.LENGTH_SHORT).show();
                    }
                }, object : Response.ErrorListener {
                  override  fun onErrorResponse(error: VolleyError) {
                        Toast.makeText(
                            this@ChatActivity,
                            error.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }) {
                    @Throws(AuthFailureError::class)
                    override fun getHeaders(): Map<String, String> {
                        val map: HashMap<String, String> = HashMap()
                        val key =
                            "Key=AAAAbRMXinQ:APA91bHrwauz4dumo0gqAv5Jj6S6Ns8KXwXgX7KJjWHA8XVuKaoKK6qYK2pWgAEl8ijrZAFz5_p2scRKoqaxPCMe1aa9epJsie9Ls06bmg39bfv80zOqY3k4iqhY6aEZrqAFrWaqjw2q"
                        map["Content-Type"] = "application/json"
                        map["Authorization"] = key
                        return map
                    }
                }
            queue.add(request)
        } catch (ex: Exception) {
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int,data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_TO_SEND && resultCode == Activity.RESULT_OK){
            if (data != null) {
                if (data.data != null) {
                    val selectedImage= data.data
                    val calendar: Calendar = Calendar.getInstance()
                    val reference = storage!!.reference.child("chats")
                        .child(calendar.getTimeInMillis().toString() + "")
                    dialog!!.show()
                    reference.putFile(selectedImage!!).addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            reference.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageTxt: String =
                                    binding.messageBox.getText().toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.getTime())
                                message.message="photo"
                                message.imageUrl=filePath
                                binding.messageBox.setText("")
                                val randomKey = database!!.reference.push().key
                                val lastMsgObj: HashMap<String, Any> = HashMap()
                                lastMsgObj["lastMsg"] = message.message!!
                                lastMsgObj["lastMsgTime"] = date.getTime()
                                database!!.reference.child("chats").child(senderRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats").child(receiverRoom!!)
                                    .updateChildren(lastMsgObj)
                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomKey!!)
                                    .setValue(message).addOnSuccessListener {
                                        database!!.reference.child("chats")
                                            .child(receiverRoom!!)
                                            .child("messages")
                                            .child(randomKey)
                                            .setValue(message).addOnSuccessListener { }
                                    }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence").child(currentId!!).setValue("Offline")
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_TO_SEND -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImageToSend()
                }
                else
                    Toast.makeText(this,"Allow the permission to select an image...",Toast.LENGTH_SHORT).show()
            }
        }
    }

}