package com.ravimessagingdalal.messagingapp.Fragments

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.ravimessagingdalal.messagingapp.Adapters.StatusAdapter
import com.ravimessagingdalal.messagingapp.Models.Status
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.Models.UserStatus
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.Utils.MyViewModel
import com.ravimessagingdalal.messagingapp.databinding.FragmentStatusBinding
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class StatusFragment : Fragment() {
    private var _binding: FragmentStatusBinding?=null
    private val binding get()=_binding!!
private val PICK_STATUS=22
    private val READ_EXTERNAL_STORAGE_CODE=25
    private lateinit var database: FirebaseDatabase
    private lateinit var usersStatus: ArrayList<UserStatus>
     private lateinit var statusAdapter: StatusAdapter
private lateinit var dialog:ProgressDialog
private lateinit var user:User
    private lateinit var myViewModel: MyViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding= FragmentStatusBinding.bind(view)

        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val state= connectivityManager.activeNetworkInfo
        if(!(state!=null && state.isConnectedOrConnecting)){
            Toast.makeText(requireContext(),"Check your internet connnection...", Toast.LENGTH_LONG).show()
        }


        usersStatus = ArrayList()
         statusAdapter = StatusAdapter(requireContext(), usersStatus)

        binding.statusList.adapter=statusAdapter
        database= FirebaseDatabase.getInstance()
        binding.statusList.showShimmerAdapter()

        dialog = ProgressDialog(requireContext())
        dialog.setMessage("Uploading Status...")
        dialog.setCancelable(false)
        database.getReference().child("users").child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)!!
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        myViewModel= ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        myViewModel.getStatus().observe(viewLifecycleOwner){
            usersStatus.clear()
            usersStatus.addAll(it)
            binding.statusList.hideShimmerAdapter()
            statusAdapter.notifyDataSetChanged()
        }





        binding.floatingActionButton.setOnClickListener {
            pickStatus()
        }

    }

    private fun pickStatus() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, PICK_STATUS)
        }
        else{
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_EXTERNAL_STORAGE_CODE
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            READ_EXTERNAL_STORAGE_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickStatus()
                }
                else
                    Toast.makeText(requireContext(),"Allow the permission to select a status...", Toast.LENGTH_SHORT).show()
            }
        }
    }

   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
       super.onActivityResult(requestCode, resultCode, data)
       if (requestCode == PICK_STATUS && resultCode == Activity.RESULT_OK) {
           if (data != null) {
               if (data.data != null) {
                   dialog.show()
                   val storage = FirebaseStorage.getInstance()
                   val date = Date()
                   val reference =
                       storage.reference.child("status").child(date.getTime().toString() + "")
                   reference.putFile(data.data!!).addOnCompleteListener { task ->
                       if (task.isSuccessful) {
                           reference.downloadUrl.addOnSuccessListener { uri ->
                               val userStatus = UserStatus()
                               userStatus.name = user.name
                               userStatus.profileImage = user.profileImage
                               userStatus.userId=user.uid
                               userStatus.lastUpdated = date.getTime()
                               val obj: HashMap<String, Any> = HashMap()
                               obj.put("name",userStatus.name!!)
                               obj.put("userId",userStatus.userId!!)
                               obj.put("lastUpdated", userStatus.lastUpdated!!)
                               obj.put("profileImage",userStatus.profileImage!!)
                               val imageUrl = uri.toString()
                               val status = Status(imageUrl, userStatus.lastUpdated)
                               database.getReference()
                                   .child("stories")
                                   .child(FirebaseAuth.getInstance().uid!!)
                                   .updateChildren(obj)
                               database.getReference().child("stories")
                                   .child(FirebaseAuth.getInstance().uid!!)
                                   .child("statuses")
                                   .push()
                                   .setValue(status)
                               dialog.dismiss()
                           }
                       }
                   }
               }
           }
       }
   }
}