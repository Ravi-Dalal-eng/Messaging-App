package com.ravimessagingdalal.messagingapp.Fragments


import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider

import com.ravimessagingdalal.messagingapp.Adapters.UsersAdapter
import com.ravimessagingdalal.messagingapp.Models.User
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.Utils.MyViewModel
import com.ravimessagingdalal.messagingapp.databinding.FragmentUsersBinding


class UsersFragment : Fragment() {
    private var _binding: FragmentUsersBinding?=null
    private val binding get()=_binding!!
    private lateinit var users: ArrayList<User>
   private lateinit var usersAdapter: UsersAdapter
private lateinit var myViewModel: MyViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding=FragmentUsersBinding.bind(view)

        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val state= connectivityManager.activeNetworkInfo
        if(!(state!=null && state.isConnectedOrConnecting)){
            Toast.makeText(requireContext(),"Check your internet connnection...", Toast.LENGTH_LONG).show()
        }

        users = ArrayList()
        usersAdapter = UsersAdapter(requireContext(), users)

        binding.recyclerView.adapter = usersAdapter
        binding.recyclerView.showShimmerAdapter()
        myViewModel=ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
            myViewModel.getUsers().observe(viewLifecycleOwner){
    users.clear()
    users.addAll(it)
    binding.recyclerView.hideShimmerAdapter()
    usersAdapter.notifyDataSetChanged()
}


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding=null
    }
}