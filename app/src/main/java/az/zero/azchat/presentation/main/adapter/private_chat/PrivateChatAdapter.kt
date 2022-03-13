package az.zero.azchat.presentation.main.adapter.private_chat

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.convertTimeStampToDate
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
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                onStudentClickListener?.let { it(getItem(adapterPosition)) }
            }
        }

        fun bind(currentItem: PrivateChat) {
            binding.apply {
                val roomName: String
                val roomImage: String

                if (currentItem.group.ofTypeGroup!!) {
                    roomName = currentItem.group.name!!
                    roomImage = currentItem.group.image!!
                } else {
                    roomName = currentItem.user.name!!
                    roomImage = currentItem.user.imageUrl!!
                }


                privateChatNameTv.text = roomName
                setImageUsingGlide(privateChatImageIv, roomImage)

                val lastMessage = currentItem.group.lastSentMessage
                if (lastMessage != null) {
                    tvSentAt.text = convertTimeStampToDate(
                        lastMessage.sentAt!!
                    ).split(" ")[1]

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && lastMessage.sentBy != uid) {
                        if (lastMessage.seen || currentItem.group.ofTypeGroup!!) {
                            lastMessageTv.setTextAppearance(R.style.bodyTextStyle)
                            tvSentAt.setTextAppearance(R.style.verySmallTextStyle)
                            newMessageIndicator.gone()
                        } else {
                            lastMessageTv.setTextAppearance(R.style.headerTextStyleSmall)
                            tvSentAt.setTextAppearance(R.style.headerTextStyleSmall)
                            newMessageIndicator.show()
                        }
                    }

                    val messageText = when {
                        lastMessage.deleted!! -> lastMessageTv.context.getString(R.string.deleted_message)
                        !lastMessage.messageText.isNullOrEmpty() -> lastMessage.messageText
                        lastMessage.imageUri.isNotEmpty() -> lastMessageTv.context.getString(R.string.sent_an_image)
                        else -> lastMessageTv.context.getString(R.string.sent_an_audio)
                    }
                    logMe("$messageText", "messageText")
                    val sentBy = if (lastMessage.sentBy!! == uid) {
                        lastMessageTv.context.getString(R.string.you)
                    } else ""
                    lastMessageTv.text = "$sentBy$messageText"
                }
            }
        }
    }

    override fun submitList(list: MutableList<PrivateChat>?) {
        super.submitList(list)
    }


    private var onStudentClickListener: ((PrivateChat) -> Unit)? = null
    fun setOnStudentClickListener(listener: (PrivateChat) -> Unit) {
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