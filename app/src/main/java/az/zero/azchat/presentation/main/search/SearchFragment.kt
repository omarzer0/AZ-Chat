package az.zero.azchat.presentation.main.search

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : BaseFragment(R.layout.fragment_search) {

    val viewModel: SearchViewModel by viewModels()
    private lateinit var binding: FragmentSearchBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSearchBinding.bind(view)
        handleClicks()

    }

    private fun handleClicks() {

    }


}