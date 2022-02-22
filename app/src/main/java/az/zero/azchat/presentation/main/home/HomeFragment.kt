package az.zero.azchat.presentation.main.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentHomeBinding
import az.zero.azchat.presentation.main.adapter.private_chat.PrivateChatAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var privateChatAdapter: PrivateChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setUpRVs()
        setHasOptionsMenu(true)
        observeViewEvents()
        handleClicks()
    }


    private fun observeViewEvents() {
        viewModel.privateChats.observe(viewLifecycleOwner) {
            privateChatAdapter.submitList(it)
        }

        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                HomeFragmentEvent.AddChat -> {
                    navigateToAction(HomeFragmentDirections.actionHomeFragmentToAddChatFragment())
                }
                is HomeFragmentEvent.PrivateChatsClick -> {
                    // go to chat screen
                    logMe("home ${event.gid}")
                    navigateToAction(
                        HomeFragmentDirections.actionHomeFragmentToPrivateChatRoomFragment(
                            event.gid,
                            event.username,
                            event.userImage,
                            event.otherUserUID,
                            false,
                            event.notificationToken
                        )
                    )
                }
            }
        }
    }

    private fun setUpRVs() {
        privateChatAdapter = PrivateChatAdapter(sharedPreferences.uid)
        binding.groupRv.adapter = privateChatAdapter
    }

    private fun handleClicks() {
        binding.addChatFabBtn.setOnClickListener {
            viewModel.addUserClick()
        }

        privateChatAdapter.setOnStudentClickListener { gid, username, userImage, otherUserUID,notificationToken ->
            viewModel.privateChatClick(gid, username, userImage, otherUserUID,notificationToken)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.home_action_search -> {
            // TODO: go to search
            true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        viewModel.viewCreated()
    }

    override fun onPause() {
        super.onPause()
        viewModel.viewDestroyed()
    }

}