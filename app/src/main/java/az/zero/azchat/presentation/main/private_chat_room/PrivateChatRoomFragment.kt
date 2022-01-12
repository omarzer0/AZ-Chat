package az.zero.azchat.presentation.main.private_chat_room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.logMe
import az.zero.azchat.common.setImageUsingGlide
import az.zero.azchat.common.setUpSearchView
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.databinding.FragmentPrivateChatRoomBinding
import az.zero.azchat.presentation.main.adapter.messages.MessagesAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivateChatRoomFragment : BaseFragment(R.layout.fragment_private_chat_room) {

    val viewModel: PrivateChatRoomViewModel by viewModels()
    private lateinit var binding: FragmentPrivateChatRoomBinding
    private lateinit var messageAdapter: MessagesAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPrivateChatRoomBinding.bind(view)
        initAdapter()
        handleClicks()
        setUpRVs()
        setUpSearchView(binding.sendAtTextEd) {
            logMe(it)
            viewModel.postAction(PrivateChatActions.SendMessage(it))
        }

    }

    private fun initAdapter() {
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
    }

    private fun handleClicks() {
        binding.apply {
            appBar.backBtn.setOnClickListener { findNavController().navigateUp() }
            appBar.usernameTv.text = viewModel.username
            setImageUsingGlide(appBar.userImageIv,viewModel.userImage)
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

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter.stopListening()
    }
}