package az.zero.azchat.presentation.main.adapter.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.convertTimeStampToDate
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.logMe
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.databinding.ItemMessageBinding
import az.zero.azchat.databinding.ItemMessageMirroredBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class MessagesAdapter(
    private val uid: String,
    private val options: FirestoreRecyclerOptions<Message>,
    val onMessageLongClick: (Message) -> Unit,
    val onDataChange: () -> Unit,
) : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

    private var lastedClickedMessage = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == SENDER_VIEW) {
            val binding = ItemMessageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
            SenderMessagesViewHolder(binding)

        } else {
            val binding = ItemMessageMirroredBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
            ReceiverMessagesViewHolder(binding)
        }
    }

    override fun onBindViewHolder(
        viewHolder: RecyclerView.ViewHolder,
        viewType: Int,
        message: Message
    ) {
        if (viewHolder is SenderMessagesViewHolder) {
            (viewHolder).bind(message)
        } else if (viewHolder is ReceiverMessagesViewHolder) {
            (viewHolder).bind(message)
        }

    }

    override fun onDataChanged() {
        super.onDataChanged()
        onDataChange()
    }

    inner class ReceiverMessagesViewHolder(private val binding: ItemMessageMirroredBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                onMessageLongClick(getItem(adapterPosition))
                true
            }

            binding.root.setOnClickListener {
                if (lastedClickedMessage != -1 && lastedClickedMessage != adapterPosition) {
                    getItem(lastedClickedMessage).clicked = false
                    notifyItemChanged(lastedClickedMessage)
                }

                getItem(adapterPosition).clicked = !getItem(adapterPosition).clicked
                lastedClickedMessage = adapterPosition
                notifyItemChanged(adapterPosition)

            }
        }

        fun bind(currentItem: Message) {
            binding.apply {
                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                binding.sendAtTextTv.isVisible = currentItem.clicked
                binding.constraintLayout2.gone()
            }
        }
    }

    inner class SenderMessagesViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                onMessageLongClick(getItem(adapterPosition))
                true
            }

            binding.root.setOnClickListener {
                if (lastedClickedMessage != -1 && lastedClickedMessage != adapterPosition) {
                    getItem(lastedClickedMessage).clicked = false
                    notifyItemChanged(lastedClickedMessage)
                }

                getItem(adapterPosition).clicked = !getItem(adapterPosition).clicked
                lastedClickedMessage = adapterPosition
                notifyItemChanged(adapterPosition)

            }
        }

        fun bind(currentItem: Message) {
            binding.apply {
                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                binding.sendAtTextTv.isVisible = currentItem.clicked

                binding.msgSeenIv.isVisible = currentItem.seen
                binding.msgSentIv.isVisible = !currentItem.seen
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).sentBy == uid) {
            SENDER_VIEW
        } else {
            RECEIVER_VIEW
        }
    }

    companion object {
        const val SENDER_VIEW = 1
        const val RECEIVER_VIEW = 2
    }
}