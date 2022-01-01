package az.zero.azchat.presentation.extra_details

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.databinding.FragmentExtraDetailsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExtraDetailsFragment : Fragment(R.layout.fragment_extra_details) {

    val viewModel: ExtraDetailsViewModel by viewModels()
    private lateinit var binding: FragmentExtraDetailsBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentExtraDetailsBinding.bind(view)


        handleClicks()

    }

    private fun handleClicks() {
        binding.addGroupBtn.setOnClickListener {
            viewModel.addGroup()
        }

        binding.getAllGroupBtn.setOnClickListener {
            viewModel.getAllGroupsByUserUID()
        }

        binding.getMessagesBtn.setOnClickListener {
            viewModel.getMessagesByGroupId()
        }

        binding.addMessageBtn.setOnClickListener {
            viewModel.addMessage()
        }
    }


}