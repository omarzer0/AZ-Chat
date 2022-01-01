package az.zero.phoneloginmvvm.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import az.zero.phoneloginmvvm.R
import az.zero.phoneloginmvvm.databinding.FragmentEmptyBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EmptyFragment : Fragment(R.layout.fragment_empty) {

    val viewModel: EmptyViewModel by viewModels()
    private lateinit var binding: FragmentEmptyBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentEmptyBinding.bind(view)
        handleClicks()

    }

    private fun handleClicks() {

    }


}