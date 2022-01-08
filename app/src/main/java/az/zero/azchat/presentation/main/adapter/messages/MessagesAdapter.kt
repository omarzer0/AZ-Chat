package az.zero.azchat.presentation.main.adapter.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.convertTimeStampToDate
import az.zero.azchat.common.logMe
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.databinding.ItemMessageBinding
import az.zero.azchat.databinding.ItemMessageMirroredBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*


class MessagesAdapter(
    private val uid: String,
    private val options: FirestoreRecyclerOptions<Message>,
    val onMessageClick: (Message) -> Unit
) : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options) {

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


    inner class ReceiverMessagesViewHolder(private val binding: ItemMessageMirroredBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                onMessageClick(getItem(adapterPosition))
                true
            }
        }

        fun bind(currentItem: Message) {
            binding.apply {
                cvRoot.setCardBackgroundColor(
                    ContextCompat.getColor(
                        cvRoot.context,
                        R.color.receiver_messages_bg_color
                    )
                )
                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
            }
        }
    }

    inner class SenderMessagesViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                onMessageClick(getItem(adapterPosition))
                true
            }
        }

        fun bind(currentItem: Message) {
            binding.apply {
                cvRoot.setCardBackgroundColor(
                    ContextCompat.getColor(
                        cvRoot.context,
                        R.color.sender_messages_bg_color
                    )
                )
                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
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