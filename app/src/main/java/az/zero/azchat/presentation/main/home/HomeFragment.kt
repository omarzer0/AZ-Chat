package az.zero.azchat.presentation.main.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentHomeBinding
import az.zero.azchat.presentation.main.adapter.private_chat.PrivateChatAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    val viewModel: HomeViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferenceManger

    private lateinit var binding: FragmentHomeBinding
    private lateinit var privateChatAdapter: PrivateChatAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        setUpRVs()
        observeViewEvents()
        handleClicks()
    }


    private fun observeViewEvents() {
        viewModel.privateChats.observe(viewLifecycleOwner) {
            it.forEach { chat ->
                logMe("$chat\n\n\n", "Home observeViewEvents")
            }
            privateChatAdapter.submitList(it)
        }

        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                HomeFragmentEvent.AddChat -> {
                    navigateToAction(HomeFragmentDirections.actionHomeFragmentToAddChatFragment())
                }
                is HomeFragmentEvent.PrivateChatsClick -> {
                    // go to chat screen
                    logMe("home ${event.privateChat.group.gid}")
                    navigateToAction(
                        HomeFragmentDirections.actionHomeFragmentToPrivateChatRoomFragment(
                            event.privateChat,
                            false
                        )
                    )
                }
            }
        }
    }

    private fun setUpRVs() {
        privateChatAdapter = PrivateChatAdapter(sharedPreferences.uid,
            onUserClick = {
                viewModel.privateChatClick(it)
            }, onUserLongClick = { privateChatID, isGroup, view ->
                showMenu(privateChatID, isGroup, view)
            })
        binding.groupRv.adapter = privateChatAdapter
    }

    private fun showMenu(privateChatID: String, isGroup: Boolean, view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.leave_group_action -> {
                    viewModel.leaveGroup(privateChatID)
                }

                R.id.block_action -> {
                    viewModel.blockUser(privateChatID)
                }
            }
            true
        }
        val inflateMenu = if (isGroup) R.menu.home_group_action_menu
        else R.menu.home_user_action_menu

        popup.inflate(inflateMenu)
        popup.setForceShowIcon(true)
        popup.show()
    }

    private fun handleClicks() {
        binding.addChatFabBtn.setOnClickListener {
            viewModel.addUserClick()
        }
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