package az.zero.azchat.presentation.main.private_chat_room

import android.Manifest
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.MainNavGraphDirections
import az.zero.azchat.R
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.audio.media_player.AudioHandler
import az.zero.azchat.common.audio.record.AudioRecordListener
import az.zero.azchat.common.audio.record.AudioRecorderHelper
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hide
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.common.tryNow
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentPrivateChatRoomBinding
import az.zero.azchat.databinding.SendEditTextBinding
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.presentation.main.MainActivity
import az.zero.azchat.presentation.main.adapter.messages.MessagesAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PrivateChatRoomFragment : BaseFragment(R.layout.fragment_private_chat_room),
    AudioRecordListener {

    val viewModel: PrivateChatRoomViewModel by viewModels()
    private lateinit var binding: FragmentPrivateChatRoomBinding
    private lateinit var messageAdapter: MessagesAdapter

    @Inject
    lateinit var audioHandler: AudioHandler

    @Inject
    lateinit var mMediaPlayer: MediaPlayer

    @Inject
    lateinit var sharedPreferences: SharedPreferenceManger

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPrivateChatRoomBinding.bind(view)
        initAdapter()
        setDataToViews()
        setUpRVs()
        observeEvents()
        handleClicks()
        AudioRecorderHelper(this, binding.sendAtTextEd.recordIv, this)
        setUpSearchView(binding.sendAtTextEd, actionWhenSend = {
            logMe(it)
            viewModel.postAction(
                PrivateChatActions.SendMessage(
                    messageText = it,
                    messageType = MessageType.TEXT
                )
            )
        }, writing = {
            viewModel.postAction(PrivateChatActions.Writing(it))
        })
    }

    private fun handleClicks() {
        tryNow {
            (activity as MainActivity).binding.toolbar.setOnClickListener {
                navigateToAction(
                    PrivateChatRoomFragmentDirections.actionPrivateChatRoomFragmentToChatDetailsFragment(
                        viewModel.getCurrentPrivateChat()
                    )
                )
            }
        }
    }

    private fun observeEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                is PrivateChatEvents.OtherUserState -> {
                    tryNow {
                        (activity as MainActivity).binding.apply {
                            userStateTv.text = when (event.otherUserStatus) {
                                UserStatus.ONLINE -> {
                                    userStateTv.show()
                                    getString(R.string.online)
                                }
                                UserStatus.WRITING -> {
                                    userStateTv.show()
                                    getString(R.string.writing)
                                }
                                UserStatus.OFFLINE -> {
                                    userStateTv.gone()
                                    ""
                                }
                            }
                        }
                    }
                }
                PrivateChatEvents.PlaySendMessageSound -> tryNow { mMediaPlayer.start() }
            }
        }

        viewModel.editAreaState.observe(viewLifecycleOwner) {
            logMe("editAreaState $it", "editAreaState")
            // it.first => shouldShow
            binding.sendAtTextEd.apply {
                editGroup.isVisible = it.first
                normalGroup.isVisible = !it.first
                sendIv.gone()
                writeMessageEd.setText("${it.second}")
                editMessageTv.text = "${it.second}"
                editMessageContainerCv.isVisible = it.first
                submitEditMessageTv.isVisible = it.first

            }
        }
    }

    private fun initAdapter() {
        binding.chatsRv.itemAnimator = null
        val uid = viewModel.getUID()
        val query = viewModel.getMessagesQuery()
        val options: FirestoreRecyclerOptions<Message> = FirestoreRecyclerOptions.Builder<Message>()
            .setQuery(query, Message::class.java)
            .build()
        messageAdapter = MessagesAdapter(
            uid,
            viewModel.isGroup,
            options,
            onReceivedMessageLongClick = {
                viewModel.postAction(PrivateChatActions.ReceiverMessageLongClick(it))
            },
            onSenderMessageLongClick = { message, action ->
                viewModel.postAction(PrivateChatActions.SenderMessageLongClick(message, action))
            },
            onDataChange = { isListEmpty ->
                binding.apply {
                    noMessagesGroup.isVisible = isListEmpty
                    chatsRv.isVisible = !isListEmpty
                }
                viewModel.postAction(PrivateChatActions.DataChanged)
            }, onImageClicked = { image ->
                val action = MainNavGraphDirections.actionGlobalImageViewerFragment(image)
                navigateToAction(action)
            },
            audioHandler

        )
        messageAdapter.startListening()
        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                tryScroll(positionStart)
            }
        })
    }

    private fun setUpRVs() {
        binding.chatsRv.adapter = messageAdapter
        binding.chatsRv.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) binding.chatsRv.smoothScrollToPosition(bottom)
        }
    }

    private fun setDataToViews() {
        val roomName: String
        val roomImage: String

        if (viewModel.isGroup) {
            roomName = viewModel.groupName
            roomImage = viewModel.groupImage
        } else {
            roomName = viewModel.username
            roomImage = viewModel.userImage
        }

        tryNow {
            (activity as MainActivity).binding.apply {
                usernameTv.text = roomName
                userStateTv.gone()
                setImageUsingGlide(
                    userImageIv,
                    roomImage,
                    isProfileImage = false,
                    if (viewModel.isGroup) R.drawable.no_group_image else R.drawable.no_profile_image
                )
            }
        }

    }

    private fun tryScroll(position: Int) {
        try {
            binding.chatsRv.scrollToPosition(position)
        } catch (e: Exception) {
            logMe("PrivateChatRoomFragment registerAdapterDataObserver${e.localizedMessage}")
        }
    }

    private fun setUpSearchView(
        sendEditText: SendEditTextBinding,
        actionWhenSend: (sendMessage: String) -> Unit,
        writing: ((Boolean) -> Unit)? = null,
        actionWhenClick: (() -> Unit)? = null
    ) {
        sendEditText.apply {
            sendIv.setOnClickListener {
                val text = writeMessageEd.text.toString().trim()
                if (text.isNotEmpty())
                    actionWhenSend(writeMessageEd.text.toString().trim())
                writeMessageEd.setText("")
            }

            writeMessageEd.addTextChangedListener {
//                TransitionManager.beginDelayedTransition(root)
                val text = it?.toString()?.trim()
                if (viewModel.isEditMode()) {
                    return@addTextChangedListener
                }
                logMe("vm editAreaState ${viewModel.isEditMode()}", "editAreaState")
                if (text.isNullOrEmpty()) {
                    writing?.invoke(false)
                    normalGroup.show()
                    editGroup.gone()
                    writingGroup.gone()
                } else {
                    writing?.invoke(true)
                    normalGroup.gone()
                    editGroup.gone()
                    writingGroup.show()
                }
            }
            writeMessageEd.setOnClickListener {
                actionWhenClick?.invoke()
            }

            galleryIv.setOnClickListener {
                checkMyPermissions()
            }

            editMessageCancel.setOnClickListener {
                viewModel.postAction(PrivateChatActions.CancelEditClick)
            }

            submitEditMessageTv.setOnClickListener {
                val text = writeMessageEd.text.toString().trim()
                if (text.isEmpty()) return@setOnClickListener
                writeMessageEd.setText("")
                viewModel.postAction(PrivateChatActions.SendEditedMessage(text))
                viewModel.postAction(PrivateChatActions.CancelEditClick)
            }

        }
    }

    private fun checkMyPermissions() {
        tryNow {
            activityResultLauncher.launch(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            )
        }
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val failedToGrant = permissions.entries.any { it.value == false }
            if (failedToGrant) {
                toastMy(getString(az.zero.azchat.R.string.camera_not_granted))
                return@registerForActivityResult
            }

            pickImage { uri ->
                viewModel.onMessageImageSelected(uri)
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter.stopListening()
        messageAdapter.clearAllAudio()
        tryNow {
            mMediaPlayer.apply {
                stop()
                release()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.postAction(PrivateChatActions.ViewResumed)
        sharedPreferences.currentGid = viewModel.getGID()

        tryNow {
            mMediaPlayer.prepare()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.postAction(PrivateChatActions.ViewPaused)
        sharedPreferences.currentGid = ""
    }

    override fun onRecordSuccess(filePath: String, duration: Long) {
        viewModel.postAction(
            PrivateChatActions.SendMessage(
                messageAudio = filePath,
                audioDuration = duration,
                messageType = MessageType.AUDIO
            )
        )
    }

    override fun onRecordFailure(error: String) {

    }

    override fun onTouchDown() {
        binding.sendAtTextEd.root.hide()
        binding.recordCustomView.root.show()
    }

    override fun onTouchUp() {
        binding.sendAtTextEd.root.show()
        binding.recordCustomView.root.gone()
        hideKeyboard()
    }
}