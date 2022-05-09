package com.ravimessagingdalal.messagingapp.Adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.pgreze.reactions.ReactionPopup
import com.github.pgreze.reactions.ReactionsConfigBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.ravimessagingdalal.messagingapp.Models.Message
import com.ravimessagingdalal.messagingapp.R
import com.ravimessagingdalal.messagingapp.databinding.DeleteDialogBinding
import com.ravimessagingdalal.messagingapp.databinding.ItemReceiveBinding
import com.ravimessagingdalal.messagingapp.databinding.ItemSentBinding


class MessagesAdapter(context: Context, messages: ArrayList<Message>,
                      senderRoom: String,
                      receiverRoom: String) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var context: Context
    var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    var senderRoom: String
    var receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false)
            SentViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messages[position]
        return if (FirebaseAuth.getInstance().uid == message.senderId) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message: Message = messages[position]
        val reactions = intArrayOf(
            R.drawable.ic_fb_like,
            R.drawable.ic_fb_love,
            R.drawable.ic_fb_laugh,
            R.drawable.ic_fb_wow,
            R.drawable.ic_fb_sad,
            R.drawable.ic_fb_angry
        )
        val config = ReactionsConfigBuilder(context)
            .withReactions(reactions)
            .build()
        val popup = ReactionPopup(context, config) { pos ->
            if (pos < 0)
                return@ReactionPopup true

            val viewHolder = holder as ReceiverViewHolder
            viewHolder.binding.feeling.setImageResource(reactions[pos])
            viewHolder.binding.feeling.setVisibility(View.VISIBLE)

            message.feeling = pos
            FirebaseDatabase.getInstance().reference
                .child("chats")
                .child(senderRoom)
                .child("messages")
                .child(message.messageId!!).setValue(message)
            FirebaseDatabase.getInstance().reference
                .child("chats")
                .child(receiverRoom)
                .child("messages")
                .child(message.messageId!!).setValue(message)
            true
        }
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.ic_status)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.setText(message.message)
            if (message.feeling >= 0) {
                viewHolder.binding.feeling.setImageResource(reactions[message.feeling])
                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE)
            }

            viewHolder.itemView.setOnLongClickListener(object : View.OnLongClickListener {
                override fun onLongClick(v: View?): Boolean {
                    val view: View =
                        LayoutInflater.from(context).inflate(R.layout.delete_dialog, null)
                    val binding: DeleteDialogBinding = DeleteDialogBinding.bind(view)
                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.getRoot())
                        .create()

                    binding.delete.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            FirebaseDatabase.getInstance().reference
                                .child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(message.messageId!!).setValue(null)
                            FirebaseDatabase.getInstance().reference
                                .child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child(message.messageId!!).setValue(null)
                            dialog.dismiss()
                        }
                    })
                    binding.cancel.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            dialog.dismiss()
                        }
                    })
                    dialog.show()
                    return false
                }
            })
        } else {
            val viewHolder = holder as ReceiverViewHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.ic_status)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.setText(message.message)
            if (message.feeling >= 0) {
                //message.setFeeling(reactions[message.getFeeling()]);
                viewHolder.binding.feeling.setImageResource(reactions[message.feeling])
                viewHolder.binding.feeling.setVisibility(View.VISIBLE)
            } else {
                viewHolder.binding.feeling.setVisibility(View.GONE)
            }
            viewHolder.binding.message.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    popup.onTouch(v!!, event!!)
                    return false
                }
            })
            viewHolder.binding.image.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    popup.onTouch(v!!, event!!)
                    return false
                }
            })


        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemSentBinding

        init {
            binding = ItemSentBinding.bind(itemView)
        }
    }

    inner class ReceiverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ItemReceiveBinding

        init {
            binding = ItemReceiveBinding.bind(itemView)
        }
    }

    init {
        this.context = context
        this.messages = messages
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }
}