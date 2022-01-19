package az.zero.azchat.presentation.main.adapter.private_chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.databinding.ItemPrivateChatBinding

class PrivateChatAdapter :
    ListAdapter<PrivateChat, PrivateChatAdapter.PrivateChatViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateChatViewHolder {
        val binding = ItemPrivateChatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return PrivateChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrivateChatViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) holder.bind(currentItem)
    }

    inner class PrivateChatViewHolder(private val binding: ItemPrivateChatBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onStudentClickListener?.let {
                    val item = getItem(adapterPosition)
                    it(item.group.gid!!, item.user.name!!, item.user.imageUrl ?: "", item.user.uid!!)
                }
            }
        }

        fun bind(currentItem: PrivateChat) {
            binding.apply {
                privateChatNameTv.text = currentItem.user.name ?: ""
                setImageUsingGlide(privateChatImageIv, currentItem.user.imageUrl ?: "")
                lastMessageTv.text =
                    currentItem.group.lastSentMessage?.messageText ?: currentItem.user.bio
            }
        }
    }

    override fun submitList(list: MutableList<PrivateChat>?) {
        super.submitList(list)
        // Forced to notify as the recycler view updates silently when the same list ref is passed :(
        notifyDataSetChanged()
    }


    private var onStudentClickListener: ((String, String, String, String) -> Unit)? = null
    fun setOnStudentClickListener(listener: (String, String, String, String) -> Unit) {
        onStudentClickListener = listener
    }

    class DiffCallback : DiffUtil.ItemCallback<PrivateChat>() {
        override fun areItemsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean {
            return oldItem.group.gid == newItem.group.gid
        }

        override fun areContentsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean {
            return oldItem == newItem
        }
    }

}