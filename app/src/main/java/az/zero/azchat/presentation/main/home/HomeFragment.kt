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
import az.zero.azchat.presentation.main.adapter.group.GroupAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : BaseFragment(R.layout.fragment_home) {

    val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var groupAdapter: GroupAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        handleClicks()
        setHasOptionsMenu(true)
        setUpRVs()
    }

    private fun setUpRVs() {
        val options = viewModel.getAdapterQuery()
        groupAdapter = GroupAdapter(options, sharedPreferences.uid)
        binding.groupRv.adapter = groupAdapter
    }

    private fun handleClicks() {
        binding.addChatFabBtn.setOnClickListener {
            navigateToAction(HomeFragmentDirections.actionHomeFragmentToAddChatFragment())
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

    override fun onStart() {
        super.onStart()
        groupAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        groupAdapter.stopListening()
    }
}