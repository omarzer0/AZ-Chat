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
                    it(item.group.gid!!, item.user.name!!)
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


    private var onStudentClickListener: ((String, String) -> Unit)? = null
    fun setOnStudentClickListener(listener: (String, String) -> Unit) {
        onStudentClickListener = listener
    }

    class DiffCallback : DiffUtil.ItemCallback<PrivateChat>() {
        override fun areItemsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean =
            oldItem == newItem
    }

}