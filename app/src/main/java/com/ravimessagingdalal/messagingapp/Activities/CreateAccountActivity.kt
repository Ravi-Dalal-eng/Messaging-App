package com.ravimessagingdalal.messagingapp.Activities

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.ravimessagingdalal.messagingapp.databinding.ActivityCreateAccountBinding
import java.util.concurrent.TimeUnit


class CreateAccountActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateAccountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: ProgressDialog

    private lateinit var context: CreateAccountActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this
        supportActionBar?.hide()
        dialog= ProgressDialog(this)
        dialog.setMessage("Creating new account...")
        dialog.setCancelable(false)
        binding.mailBox.requestFocus()

        auth = FirebaseAuth.getInstance()

        binding.registerBtn.setOnClickListener {
            var email=binding.mailBox.text.toString().trim()
            val password=binding.passwordBox.text.toString().trim()
            if(email.isEmpty()) {
                binding.mailBox.setError("Enter your Email")
                return@setOnClickListener
            }
            if(password.isEmpty()) {
                binding.passwordBox.setError("Enter your Password")
                return@setOnClickListener
            }
            val connectivityManager =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val state= connectivityManager.activeNetworkInfo
            if(state!=null && state.isConnectedOrConnecting){
            dialog.show()
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this) {
                    if(it.isSuccessful){
                        dialog.dismiss()
                        val intent=Intent(this,SetUpProfileActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    }
                    else{
                        Toast.makeText(this,"This Email is already register by someone",Toast.LENGTH_LONG).show()
                dialog.dismiss()
                    }
                }

            }
            else
                Toast.makeText(this,"Check your internet connnection...",Toast.LENGTH_LONG).show()
        }

    }



}







