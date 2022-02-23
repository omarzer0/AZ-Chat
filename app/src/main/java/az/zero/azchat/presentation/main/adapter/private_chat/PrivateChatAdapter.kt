package az.zero.azchat.presentation.main.adapter.private_chat

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.databinding.ItemPrivateChatBinding
import az.zero.azchat.domain.models.private_chat.PrivateChat

class PrivateChatAdapter(private val uid: String) :
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
                    it(
                        item.group.gid!!,
                        item.user.name!!,
                        item.user.imageUrl ?: "",
                        item.user.uid!!,
                        item.user.notificationToken!!
                    )
                }
            }
        }

        fun bind(currentItem: PrivateChat) {
            binding.apply {
                privateChatNameTv.text = currentItem.user.name ?: ""
                setImageUsingGlide(privateChatImageIv, currentItem.user.imageUrl ?: "")
                val lastMessage = currentItem.group.lastSentMessage ?: return


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lastMessage.sentBy != uid) {
                    if (lastMessage.seen) {
                        lastMessageTv.setTextAppearance(R.style.bodyTextStyle)
                        newMessageIndicator.gone()
                    } else {
                        lastMessageTv.setTextAppearance(R.style.headerTextStyleSmall)
                        newMessageIndicator.show()
                    }
                }

                val messageText = when {
                    !lastMessage.messageText.isNullOrEmpty() -> lastMessage.messageText
                    lastMessage.imageUrl.isNotEmpty() -> lastMessageTv.context.getString(R.string.sent_an_image)
                    else -> ""
                }
                logMe("$messageText", "messageText")
                val sentBy = if (lastMessage.sentBy!! == uid) {
                    lastMessageTv.context.getString(R.string.you)
                } else ""
                lastMessageTv.text = "$sentBy$messageText"
            }
        }
    }

    override fun submitList(list: MutableList<PrivateChat>?) {
        super.submitList(list)
        // Forced to notify as the recycler view updates silently when the same list ref is passed :(
//        notifyDataSetChanged()
    }


    private var onStudentClickListener: ((String, String, String, String, String) -> Unit)? = null
    fun setOnStudentClickListener(listener: (String, String, String, String, String) -> Unit) {
        onStudentClickListener = listener
    }

    class DiffCallback : DiffUtil.ItemCallback<PrivateChat>() {
        override fun areItemsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: PrivateChat, newItem: PrivateChat): Boolean {
            return oldItem == newItem
        }
    }

}