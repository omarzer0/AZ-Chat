package az.zero.azchat.presentation.main.adapter.messages

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.convertTimeStampToDate
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.domain.models.message.Message
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
                if (currentItem.deleted!!) return
                lovedImgIv.isVisible = currentItem.loved!!
                binding.messageTextTv.isVisible = currentItem.messageText!!.isNotEmpty()

                if (currentItem.imageUri != "") {
                    setImageUsingGlide(messageImageIv, currentItem.imageUri)
                    mirroredCl.background =
                        getDrawable(mirroredCl.context, R.drawable.four_corner_mirrored_background)
                    messageImageContainerCv.show()
                } else {
                    messageImageContainerCv.gone()
                    mirroredCl.background =
                        getDrawable(mirroredCl.context, R.drawable.three_corner_mirrored_background)
                }

                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                sendAtTextTv.isVisible = currentItem.clicked
                constraintLayout2.gone()
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
                if (currentItem.deleted!!) return
                lovedImgIv.isVisible = currentItem.loved!!
                binding.messageTextTv.isVisible = currentItem.messageText!!.isNotEmpty()

                if (currentItem.imageUri != "") {
                    setImageUsingGlide(messageImageIv, currentItem.imageUri)
                    normalCl.background =
                        getDrawable(normalCl.context, R.drawable.four_corner_normal_background)
                    messageImageContainerCv.show()
                } else {
                    messageImageContainerCv.gone()
                    normalCl.background =
                        getDrawable(normalCl.context, R.drawable.three_corner_normal_background)
                }

                messageTextTv.text = currentItem.messageText
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                sendAtTextTv.isVisible = currentItem.clicked
                msgSeenIv.isVisible = currentItem.seen
                msgSentIv.isVisible = !currentItem.seen
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