package az.zero.azchat.presentation.main.private_chat_room

import android.Manifest
import android.annotation.SuppressLint
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.common.tryNow
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentPrivateChatRoomBinding
import az.zero.azchat.databinding.SendEditTextBinding
import az.zero.azchat.domain.models.message.Message
import az.zero.azchat.presentation.main.adapter.messages.MessagesAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivateChatRoomFragment : BaseFragment(az.zero.azchat.R.layout.fragment_private_chat_room) {

    val viewModel: PrivateChatRoomViewModel by viewModels()
    private lateinit var binding: FragmentPrivateChatRoomBinding
    private lateinit var messageAdapter: MessagesAdapter
    private var mRecorder: MediaRecorder? = null
    private var mLocalFilePath = ""


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPrivateChatRoomBinding.bind(view)
        initAdapter()
        handleClicks()
        setUpRVs()
        observeEvents()
        observeData()
        setUpSearchView(binding.sendAtTextEd, actionWhenSend = {
            logMe(it)
            viewModel.postAction(PrivateChatActions.SendMessage(it, MessageType.TEXT))
        }, writing = {
            viewModel.postAction(PrivateChatActions.Writing(it))
        })

        mLocalFilePath =
            "${requireActivity().externalCacheDir?.absolutePath}/out${System.currentTimeMillis()}.3gp"
    }

    private fun observeData() {
//        viewModel.messageImage.observe(viewLifecycleOwner) {
//            if (it == null) {
//                binding.sendMessageContainerFl.gone()
//                return@observe
//            }
//            binding.sendMessageContainerFl.show()
//            setImageUsingGlide(binding.sendMessageImageIv, it.toString())
//        }
    }


    private fun observeEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                is PrivateChatEvents.OtherUserState -> {
                    binding.appBar.userStateTv.text = when (event.otherUserStatus) {
                        UserStatus.ONLINE -> {
                            binding.appBar.userStateTv.show()
                            getString(az.zero.azchat.R.string.online)
                        }
                        UserStatus.WRITING -> {
                            binding.appBar.userStateTv.show()
                            getString(az.zero.azchat.R.string.writing)
                        }
                        UserStatus.OFFLINE -> {
                            binding.appBar.userStateTv.gone()
                            ""
                        }
                    }
                }
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
            options,
            onMessageLongClick = {
                viewModel.postAction(PrivateChatActions.MessageLongClick(it))
            },
            onDataChange = {
                viewModel.postAction(PrivateChatActions.DataChanged)
            }
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

    private fun handleClicks() {
        binding.apply {
            appBar.backBtn.setOnClickListener { findNavController().navigateUp() }
            appBar.usernameTv.text = viewModel.username
            setImageUsingGlide(appBar.userImageIv, viewModel.userImage)
            appBar.userStateTv.gone()
        }
    }

    private fun tryScroll(position: Int) {
        try {
            binding.chatsRv.scrollToPosition(position)
        } catch (e: Exception) {
            logMe("PrivateChatRoomFragment registerAdapterDataObserver${e.localizedMessage}")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpSearchView(
        sendEditText: SendEditTextBinding,
        actionWhenSend: (sendMessage: String) -> Unit,
        writing: ((Boolean) -> Unit)? = null,
        actionWhenClick: (() -> Unit)? = null
    ) {
        sendEditText.apply {
            sendIv.setOnClickListener {
                if (writeMessageEd.text.toString().trim().isEmpty()) return@setOnClickListener
                actionWhenSend(writeMessageEd.text.toString().trim())
//                viewModel.onMessageImageSelected(null, MessageType.IMAGE)
                writeMessageEd.setText("")
            }

            writeMessageEd.addTextChangedListener {
                val text = it?.toString()
                if (text.isNullOrEmpty()) writing?.invoke(false)
                else writing?.invoke(true)
            }
            writeMessageEd.setOnClickListener {
                actionWhenClick?.invoke()
            }

            galleryIv.setOnClickListener {
                checkMyPermissions()
            }

            recordIv.setOnTouchListener { _, motionEvent ->
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startRecording()
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        stopRecording()
                        true
                    }
                    else -> false
                }
            }

        }
    }

    private fun startRecording() {
        mRecorder = MediaRecorder()
        mRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(mLocalFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
                start()
                logMe("started")
            } catch (e: Exception) {
                Log.e("TAG", "failed: ${e.localizedMessage}")
            }
        }
    }

    private fun stopRecording() {
        mRecorder?.apply {
            tryNow {
                stop()
                release()
                mRecorder = null
                logMe("stopped")
                viewModel.uploadAudioFile(mLocalFilePath, System.currentTimeMillis()) {
//                    val mediaPlayer = MediaPlayer()
//                    mediaPlayer.apply {
//                        tryNow {
//                            setDataSource(requireContext(), it)
//                            prepare()
//                            logMe("playing....")
//                            setVolume(1.0f, 1.0f)
//                            start()
//                        }
//                    }
                }
            }
        }
    }

    private fun checkMyPermissions() {
        activityResultLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        )
    }

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val failedToGrant = permissions.entries.any { it.value == false }
            if (failedToGrant) {
                toastMy(getString(az.zero.azchat.R.string.not_granted))
                return@registerForActivityResult
            }

            pickImage { uri ->
                viewModel.onMessageImageSelected(uri)
            }
        }

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        viewModel.postAction(PrivateChatActions.ViewResumed)
    }

    override fun onPause() {
        super.onPause()
        viewModel.postAction(PrivateChatActions.ViewPaused)
    }
}