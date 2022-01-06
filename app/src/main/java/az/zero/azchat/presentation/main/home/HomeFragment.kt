package az.zero.azchat.presentation.main.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentHomeBinding
import az.zero.azchat.presentation.main.adapter.group.PrivateChatAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private val privateChatAdapter = PrivateChatAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        handleClicks()
        setHasOptionsMenu(true)
        setUpRVs()
        observeViewEvents()
    }

    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                HomeFragmentEvent.AddChat -> {
                    navigateToAction(HomeFragmentDirections.actionHomeFragmentToAddChatFragment())
                }
                is HomeFragmentEvent.GetPrivateChats -> {
                    privateChatAdapter.submitList(event.privateChats)
                }
                is HomeFragmentEvent.PrivateChatsClick -> {
                    // go to chat screen
                    navigateToAction(
                        HomeFragmentDirections.actionHomeFragmentToPrivateChatRoomFragment(
                            event.gid
                        )
                    )
                }
            }
        }
    }

    private fun setUpRVs() {
        binding.groupRv.adapter = privateChatAdapter
    }

    private fun handleClicks() {
        binding.addChatFabBtn.setOnClickListener {
            viewModel.addUserClick()
        }

        privateChatAdapter.setOnStudentClickListener {
            viewModel.privateChatClick(it)
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

}