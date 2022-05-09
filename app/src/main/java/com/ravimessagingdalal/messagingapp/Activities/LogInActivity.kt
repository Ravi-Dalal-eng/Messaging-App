package com.ravimessagingdalal.messagingapp.Activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.ravimessagingdalal.messagingapp.databinding.ActivityLogInBinding


class LogInActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLogInBinding
    private lateinit var auth:FirebaseAuth
private lateinit var dialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        dialog= ProgressDialog(this)
        dialog.setMessage("Logging in...")
        dialog.setCancelable(false)
        auth= FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            val intent=Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.continueBtn.setOnClickListener {
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
              auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this) {
                  if(it.isSuccessful){
                      dialog.dismiss()
                      val intent=Intent(this,MainActivity::class.java)
                      startActivity(intent)
                      finish()
                  }
                 else{
                      Toast.makeText(this,"Invalid Email or Password",Toast.LENGTH_LONG).show()
             dialog.dismiss()
                 }
              }

            }
            else
                Toast.makeText(this,"Check your internet connnection...",Toast.LENGTH_LONG).show()
        }
        binding.createAccount.setOnClickListener {
            val intent=Intent(this,CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }
}
