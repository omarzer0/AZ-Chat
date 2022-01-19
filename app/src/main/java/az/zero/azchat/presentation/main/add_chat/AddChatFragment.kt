package az.zero.azchat.presentation.main.add_chat

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.extension.onQueryTextChanged
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentAddChatBinding
import az.zero.azchat.presentation.auth.country.CountryCodeViewModel
import az.zero.azchat.presentation.main.adapter.user.UserAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddChatFragment : BaseFragment(R.layout.fragment_add_chat) {

    val viewModel: AddChatViewModel by viewModels()
    private lateinit var binding: FragmentAddChatBinding
    private lateinit var searchView: SearchView
    private val userAdapter = UserAdapter()


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
    }

    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                /*
                beware that this event called only once and not been used when rotate the screen
                but I use {viewModel.searchUserByNameOrPhone(it)} which called again and handles
                the rotation and configuration changes
                 */
                // TODO MAY_ERROR
                is AddChatEvent.GetUsersToChatDone -> {
                    userAdapter.submitList(event.users)
                    logMe("${event.users}")
                }
            }
        }
    }

    private fun handleClicks() {
        userAdapter.setOnUserClickListener {
            viewModel.checkIfGroupExists(viewModel.getUID(), it.uid!!) { gid ->
                val groupGID = if (gid.isEmpty()) viewModel.getGID()
                else gid

                val action =AddChatFragmentDirections.actionAddChatFragmentToPrivateChatRoomFragment(
                    groupGID,
                    it.name!!,
                    it.imageUrl!!,
                    it.uid!!,
                    gid.isEmpty()
                )
                navigateToAction(action)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_chat_fragment_menu, menu)

        val searchItem = menu.findItem(R.id.add_chat_action_search)
        searchView = searchItem.actionView as SearchView

        searchItem.expandActionView()
        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty() && pendingQuery != CountryCodeViewModel.START_SEARCH_QUERY) {
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
//https://firebasestorage.googleapis.com/v0/b/phonelogintest-a9626.appspot.com/o/
// profileImages%2FUhp7yfvA8HW4HIS49sQBoe7wovQ2.jpg?alt=media&token=8dc5b067-653e-4f97-8cd1-7d1b02902b91
//https://firebasestorage.googleapis.com/v0/b/phonelogintest-a9626.appspot.com/o/
// profileImages%2FEHx0FzfOtLQXda4vTyuZUxzoAS62.jpg?alt=media&token=1cfd0619-37c6-4bb7-b773-24318d102fa5