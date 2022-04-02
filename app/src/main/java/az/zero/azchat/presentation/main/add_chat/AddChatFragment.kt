package az.zero.azchat.presentation.main.add_chat

import android.os.Bundle
import android.transition.TransitionManager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.onQueryTextChanged
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentAddChatBinding
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.main.adapter.user.UserAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddChatFragment : BaseFragment(R.layout.fragment_add_chat) {

    val viewModel: AddChatViewModel by viewModels()

    @Inject
    lateinit var sharedPreferences: SharedPreferenceManger

    private lateinit var binding: FragmentAddChatBinding
    private lateinit var searchView: SearchView
    private val userAdapter = UserAdapter(
        onUserChosenToJoinGroup = {
            onUserChosenToJoinGroup(it)
        },
        onUserClickListener = {
            onUserClick(it)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAddChatBinding.bind(view)
        handleClicks()
        setUpRvs()
        observeViewEvents()
        setHasOptionsMenu(true)
    }

    private fun setUpRvs() {
        binding.userRv.adapter = userAdapter
//        binding.userRv.itemAnimator = null
        userAdapter.updateSelectedUsers(viewModel.getSelectedUsers())
    }

    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                is AddChatEvent.GetUsersToChatDone -> {
                    userAdapter.submitList(event.users)
                    logMe("${event.users}")
                }
            }
        }

        viewModel.selectionMode.observe(viewLifecycleOwner) {
            TransitionManager.beginDelayedTransition(binding.root)
            userAdapter.setSelectedMode(true)
            binding.apply {
                addNewGroupRootCl.gone()
                selectedUsersViewRootCl.show()
            }
        }
    }

    private fun handleClicks() {
        binding.addNewGroupRootCl.setOnClickListener {
            viewModel.updateSelectionMode(true)
            hideKeyboard()
        }

        binding.addNewGroupFab.setOnClickListener {
            val selectedUsers = viewModel.getSelectedUsers().apply {
                add(0, sharedPreferences.uid)
            }.toTypedArray()

            navigateToAction(
                AddChatFragmentDirections.actionAddChatFragmentToAddEditInfoFragment(selectedUsers)
            )
        }
    }


    private fun onUserClick(user: User) {
        viewModel.checkIfGroupExists(viewModel.getUID(), user.uid!!) { gid ->
            val groupGID = gid.ifEmpty { viewModel.getGID() }
            val action =
                AddChatFragmentDirections.actionAddChatFragmentToPrivateChatRoomFragment(
                    PrivateChat(Group(groupGID, ofTypeGroup = false), user, groupGID),
                    gid.isEmpty()
                )
            navigateToAction(action)
        }
    }


    private fun onUserChosenToJoinGroup(selectedUsers: MutableList<String>) {
        logMe("users= $selectedUsers", "selectedUsers")
        if (selectedUsers.isEmpty()) {
            binding.apply {
                addNewGroupIv.gone()
                addNewGroupFab.gone()
            }
            return
        }
        binding.apply {
            addNewGroupIv.show()
            addNewGroupFab.show()
        }
        viewModel.updateSelectedUsers(selectedUsers)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_chat_fragment_menu, menu)

        val searchItem = menu.findItem(R.id.add_chat_action_search)
        searchView = searchItem.actionView as SearchView

        searchItem.expandActionView()
        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty() && pendingQuery != AddChatViewModel.START_SEARCH_QUERY) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) {
            viewModel.searchUserByNameOrPhone(it)
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                findNavController().navigateUp()
                return true
            }
        })

        searchView.onQueryTextChanged { country ->
            viewModel.setSearchQueryValue(country)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }

}