package com.ravimessagingdalal.messagingapp.Adapters


import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ravimessagingdalal.messagingapp.Activities.MainActivity
import com.ravimessagingdalal.messagingapp.Models.Status
import com.ravimessagingdalal.messagingapp.Models.UserStatus
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.databinding.ItemStatusBinding
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory


class StatusAdapter(context: Context, userStatuses: ArrayList<UserStatus>) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {
    var context: Context
    var userStatuses: ArrayList<UserStatus>
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.item_status, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val userStatus = userStatuses[position]
        holder.binding.statusOwnerName.setText(userStatus.name)
        if(userStatus.userId.equals(FirebaseAuth.getInstance().uid)){
            holder.binding.line.visibility=View.VISIBLE
            holder.binding.circularStatusView.setPortionsColor(Color.parseColor("#FF03DAC5"))
        }
        val lastStatus: Status = userStatus.statuses!![userStatus.statuses!!.size - 1]
        Glide.with(context).load(lastStatus.imageUrl)
            .into(holder.binding.image)
        holder.binding.circularStatusView.setPortionsCount(userStatus.statuses!!.size)
        holder.binding.topLayout.setOnClickListener(object : View.OnClickListener {
           override fun onClick(v: View?) {
                val myStories: ArrayList<MyStory> = ArrayList()
                for (status in userStatus.statuses!!) {
                    myStories.add(MyStory(status.imageUrl))
                }
                StoryView.Builder((context as MainActivity).supportFragmentManager)
                    .setStoriesList(myStories) // Required
                    .setStoryDuration(5000)
                    .setTitleText(userStatus.name)// Default is 2000 Millis (2 Seconds)
                    .setTitleLogoUrl(userStatus.profileImage)
                    // Default is Hidden
                    .setStoryClickListeners(object : StoryClickListeners {
                        override fun onDescriptionClickListener(position: Int) {
                            //your action
                        }

                        override fun onTitleIconClickListener(position: Int) {
                            //your action
                        }
                    }) // Optional Listeners
                    .build() // Must be called before calling show method
                    .show()
            }
        })
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }

    inner class StatusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemStatusBinding

        init {
            binding = ItemStatusBinding.bind(itemView)
        }
    }

    init {
        this.context = context
        this.userStatuses = userStatuses
    }
}