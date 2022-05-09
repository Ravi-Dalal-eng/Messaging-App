package com.ravimessagingdalal.messagingapp.Activities

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.databinding.ActivitySetUpProfileBinding



class SetUpProfileActivity : AppCompatActivity() {
    private  lateinit  var binding: ActivitySetUpProfileBinding
    private  lateinit  var auth: FirebaseAuth
    private  lateinit  var database: FirebaseDatabase
    private  lateinit  var storage: FirebaseStorage
    private  var selectedImage: Uri?=null
    private lateinit var context:SetUpProfileActivity
    private  lateinit  var dialog: ProgressDialog
    private val PICK_PHOTO_FOR_AVATAR=45
    private val READ_EXTERNAL_STORAGE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySetUpProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        dialog = ProgressDialog(this)
        dialog.setMessage("Updating profile...")
        dialog.setCancelable(false)
        context=this
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.imageView.setOnClickListener {
           pickImage()
        }

        binding.continueBtn.setOnClickListener {
         val name = binding.nameBox.getText().toString().trimEnd()

            if(name.isEmpty()) {
                binding.nameBox.setError("Please type a name");
               return@setOnClickListener
            }

            dialog.show()
            if(selectedImage != null) {
               val reference = storage.getReference().child("Profiles").child(auth.uid!!)
                reference.putFile(selectedImage!!).addOnCompleteListener(object : OnCompleteListener<UploadTask.TaskSnapshot> {
                   override fun onComplete(task:Task<UploadTask.TaskSnapshot> ) {
                        if(task.isSuccessful()) {
                            reference.getDownloadUrl().addOnSuccessListener(object :
                                OnSuccessListener<Uri> {
                              override fun  onSuccess(uri:Uri) {
                                    val imageUrl = uri.toString()

                                    val uid = auth.uid
                                    val email = auth.currentUser!!.email

                                  val user = User(uid!!, name, email!!, imageUrl)

                                    database.getReference()
                                        .child("users")
                                        .child(uid)
                                        .setValue(user)
                                        .addOnSuccessListener(object :OnSuccessListener<Void> {
                                           override fun onSuccess(aVoid:Void? ) {
                                                dialog.dismiss()
                                              val intent = Intent(context, MainActivity::class.java)
                                                startActivity(intent)
                                                finish()
                                            }
                                        })
                                }
                            })
                        }
                    }
                })
            } else {
                val uid = auth.getUid()
                val email = auth.currentUser!!.email

                val user =User(uid!!, name, email!!, "No Image")

                database.getReference()
                    .child("users")
                    .child(uid)
                    .setValue(user)
                    .addOnSuccessListener(object :OnSuccessListener<Void> {
                        override fun  onSuccess(aVoid:Void? ) {
                            dialog.dismiss()
                           val intent = Intent(context, MainActivity::class.java)
                            startActivity(intent);
                            finish()
                        }
                    })
            }

        }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int,data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (data.data != null) {
                    val uri = data.data
                    binding.imageView.setImageURI(uri)
                    selectedImage = uri
                }
            }
        }

    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickImage()
                }
                else
                    Toast.makeText(this,"Allow the permission to select a pic...",Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun pickImage(){
        if (ActivityCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
}
