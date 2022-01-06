package az.zero.azchat.presentation.main.adapter.group

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.databinding.ItemGroupBinding

class PrivateChatAdapter :
    ListAdapter<PrivateChat, PrivateChatAdapter.PrivateChatViewHolder>(DiffUtil) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrivateChatViewHolder {
        val binding = ItemGroupBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return PrivateChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PrivateChatViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) holder.bind(currentItem)
    }

    inner class PrivateChatViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onStudentClickListener?.let {
                    it(getItem(adapterPosition).group.gid!!)
                }
            }
        }

        fun bind(currentItem: PrivateChat) {
            binding.apply {
                groupNameTv.text = currentItem.user.name ?: ""
                setImageUsingGlide(groupImageIv, currentItem.user.imageUrl ?: "")
//                lastMessageTv.text = currentItem.message?.messageText ?: currentItem.user.bio
                lastMessageTv.text =
                    currentItem.group.lastSentMessage?.messageText ?: currentItem.user.bio
            }
        }
    }


    private var onStudentClickListener: ((String) -> Unit)? = null
    fun setOnStudentClickListener(listener: (String) -> Unit) {
        onStudentClickListener = listener
    }

    companion object {
        private val DiffUtil = object : DiffUtil.ItemCallback<PrivateChat>() {
            override fun areItemsTheSame(oldItem: PrivateChat, newItem: PrivateChat) =
                oldItem == newItem

            override fun areContentsTheSame(oldItem: PrivateChat, newItem: PrivateChat) =
                oldItem == newItem
        }
    }


}