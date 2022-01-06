package az.zero.azchat.presentation.main.private_chat_room

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.databinding.FragmentPrivateChatRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PrivateChatRoomFragment : Fragment(R.layout.fragment_private_chat_room) {

    val viewModel: PrivateChatRoomViewModel by viewModels()
    private lateinit var binding: FragmentPrivateChatRoomBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentPrivateChatRoomBinding.bind(view)
        handleClicks()

    }

    private fun handleClicks() {

    }


}