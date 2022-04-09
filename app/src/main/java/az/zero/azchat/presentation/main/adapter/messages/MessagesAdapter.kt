package az.zero.azchat.presentation.main.adapter.messages

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.appcompat.widget.PopupMenu
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.audio.media_player.AudioHandler
import az.zero.azchat.common.audio.media_player.AudioPlaybackListener
import az.zero.azchat.common.convertTimeStampToDate
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.getShimmerDrawable
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.databinding.ItemMessageBinding
import az.zero.azchat.databinding.ItemMessageMirroredBinding
import az.zero.azchat.databinding.VoiceMessageBinding
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.presentation.main.private_chat_room.MessageType
import az.zero.azchat.presentation.main.private_chat_room.MessageType.*
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class MessagesAdapter(
    private val uid: String,
    private val isGroup: Boolean,
    options: FirestoreRecyclerOptions<Message>,
    val onReceivedMessageLongClick: (Message) -> Unit,
    val onSenderMessageLongClick: (message: Message, clickAction: MessageLongClickAction) -> Unit,
    val onDataChange: (isEmpty: Boolean) -> Unit,
    val onImageClicked: (image: String) -> Unit,
    private val audioHandler: AudioHandler
) : FirestoreRecyclerAdapter<Message, RecyclerView.ViewHolder>(options),
    AudioPlaybackListener {

    private var lastedClickedMessage = -1
    private var playingAudioView: ImageView? = null
    private var playingId = ""
    private var menuListener: PopupMenu.OnMenuItemClickListener? = null

    init {
        audioHandler.initListener(this)
    }

    fun clearAllAudio() {
        audioHandler.clearAll()
    }

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
        onDataChange(itemCount <= 0)
    }

    inner class ReceiverMessagesViewHolder(private val binding: ItemMessageMirroredBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val message = getItem(adapterPosition)
                    if (!message.deleted!!) onReceivedMessageLongClick(message)
                }
                true
            }

            binding.messageImageIv.setOnClickListener {
                onImageClicked(getItem(adapterPosition).imageUri)
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

            binding.voicePlayerView.playPauseBtn.setOnClickListener {
                handleAudioPlay(
                    getItem(adapterPosition),
                    binding.voicePlayerView.audioSeekBarSb,
                    binding.voicePlayerView.playPauseBtn,
                    binding.voicePlayerView.audioPlayedTimeTv
                )
            }
        }

        fun bind(currentItem: Message) {
            binding.apply {

                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                sendAtTextTv.isVisible = currentItem.clicked
                lovedImgIv.isVisible = currentItem.loved!! && !currentItem.deleted!!
                updatedTextTv.isVisible = currentItem.updated!!

                if (isGroup) {
                    tvUsername.text = currentItem.senderName
                    tvUsername.show()
                } else {
                    tvUsername.gone()
                }

                if (!isGroup) {
                    msgSeenIv.isVisible = currentItem.seen
                    msgSentIv.isVisible = !currentItem.seen
                }

                if (currentItem.deleted!!) {
                    showDeletedLayout(
                        messageTextTv,
                        mirroredCl,
                        voicePlayerView.root,
                        messageImageContainerCv,
                    )
                    return
                }

                handleBinding(
                    getMessageType(currentItem),
                    currentItem,
                    mirroredCl,
                    messageImageContainerCv,
                    voicePlayerView,
                    messageTextTv,
                    messageImageIv,
                    false
                )
            }
        }
    }

    inner class SenderMessagesViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnLongClickListener {
                showMenu(it, getItem(adapterPosition))
                true
            }

            binding.messageImageIv.setOnLongClickListener {
                showMenu(it, getItem(adapterPosition))
                true
            }

            binding.messageImageIv.setOnClickListener {
                onImageClicked(getItem(adapterPosition).imageUri)
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

            binding.voicePlayerView.playPauseBtn.setOnClickListener {
                handleAudioPlay(
                    getItem(adapterPosition),
                    binding.voicePlayerView.audioSeekBarSb,
                    binding.voicePlayerView.playPauseBtn,
                    binding.voicePlayerView.audioPlayedTimeTv
                )
            }
        }

        fun bind(currentItem: Message) {
            binding.apply {
                sendAtTextTv.text = convertTimeStampToDate(currentItem.sentAt!!)
                sendAtTextTv.isVisible = currentItem.clicked

                lovedImgIv.isVisible = currentItem.loved!! && !currentItem.deleted!!
                updatedTextTv.isVisible = currentItem.updated!!

                if (!isGroup) {
                    msgSeenIv.isVisible = currentItem.seen
                    msgSentIv.isVisible = !currentItem.seen
                }

                if (currentItem.deleted!!) {
                    showDeletedLayout(
                        messageTextTv,
                        normalCl,
                        voicePlayerView.root,
                        messageImageContainerCv,
                    )
                    return
                }

                handleBinding(
                    getMessageType(currentItem),
                    currentItem,
                    normalCl,
                    messageImageContainerCv,
                    voicePlayerView,
                    messageTextTv,
                    messageImageIv,
                    true
                )

            }
        }
    }


    fun showMenu(view: View, message: Message) {
        if (message.deleted!!) return
        val inflateMenu = if (getMessageType(message) == TEXT) {
            R.menu.message_action_menu
        } else {
            R.menu.audio_image_action_menu
        }
        val popup = PopupMenu(view.context, view)
        popup.setOnMenuItemClickListener(initMenuListener(message))
        popup.inflate(inflateMenu)
        popup.setForceShowIcon(true)
        popup.show()
    }


    private fun initMenuListener(message: Message): PopupMenu.OnMenuItemClickListener {
        menuListener = null
        menuListener = PopupMenu.OnMenuItemClickListener {
            when (it.itemId) {
                R.id.delete_action -> {
                    onSenderMessageLongClick(message, MessageLongClickAction.DELETE)
                    true
                }
                R.id.edit_action -> {
                    onSenderMessageLongClick(message, MessageLongClickAction.EDIT)
                    true
                }
                else -> false
            }
        }
        return menuListener!!
    }

    private fun getMessageType(message: Message) = when {
        message.imageUri.isNotEmpty() -> {
            IMAGE
        }
        message.audioUri.isNotEmpty() -> {
            AUDIO
        }
        else -> {
            TEXT
        }
    }

    private fun showDeletedLayout(
        textTv: TextView,
        root: View,
        voicePlayerView: View,
        imageContainerView: View
    ) {
        voicePlayerView.gone()
        imageContainerView.gone()
        textTv.text = root.context.getString(R.string.deleted_message)
        textTv.show()
        root.background = getDrawable(root.context, R.drawable.four_corner_deleted_background)
        return
    }

    private fun handleAudioPlay(
        message: Message,
        seekBar: SeekBar,
        playPauseImage: ImageView,
        textTvToUpdate: TextView
    ) {
        if (message.audioUri.isEmpty()) return
        audioHandler.playAudio(
            message.audioUri,
            seekBar,
            textTvToUpdate
        )
        if (playingAudioView != null && message.id!! != playingId) {
            playingAudioView?.setImageResource(R.drawable.ic_play)
        }
        playingAudioView = playPauseImage
        playingId = message.id!!
    }

    fun handleBinding(
        messageType: MessageType,
        currentItem: Message,
        imageAndTextBgCl: ConstraintLayout,
        messageImageContainerCv: CardView,
        voicePlayerView: VoiceMessageBinding,
        messageTextTv: TextView,
        messageImageIv: ImageView,
        isSender: Boolean
    ) {

        when (messageType) {
            TEXT -> {
                messageTextTv.show()
                messageImageContainerCv.gone()
                voicePlayerView.root.gone()

                val bg = if (isSender) R.drawable.three_corner_normal_background
                else R.drawable.three_corner_mirrored_background

                imageAndTextBgCl.background =
                    getDrawable(imageAndTextBgCl.context, bg)
                messageTextTv.text = currentItem.messageText

            }
            AUDIO -> {
                messageTextTv.gone()
                messageImageContainerCv.gone()
                voicePlayerView.root.show()

                voicePlayerView.apply {
                    audioTimeTv.text =
                        audioHandler.createTimeLabel(currentItem.audioDuration.toInt())
                    audioTimeTv.isVisible = currentItem.audioDuration != -1L
                }
            }
            IMAGE -> {
                messageTextTv.gone()
                messageImageContainerCv.show()
                voicePlayerView.root.gone()

                setImageUsingGlide(
                    messageImageIv,
                    currentItem.imageUri,
                    isProfileImage = false,
                    errorImage = getShimmerDrawable()
                )
                imageAndTextBgCl.background =
                    getDrawable(
                        imageAndTextBgCl.context,
                        R.drawable.four_corner_normal_background
                    )
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

    override fun onCompletion() {
        logMe("onCompletion", "playingAudioView")
        playingAudioView?.setImageResource(R.drawable.ic_play)
    }

    override fun onPause() {
        logMe("onPause", "playingAudioView")
        playingAudioView?.setImageResource(R.drawable.ic_play)
    }

    override fun onStart() {
        logMe("onStart $playingAudioView", "playingAudioView")
        playingAudioView?.setImageResource(R.drawable.ic_pause)
    }

    override fun onResume() {
        logMe("onResume $playingAudioView", "playingAudioView222")
        playingAudioView!!.setImageResource(R.drawable.ic_pause)
    }
}