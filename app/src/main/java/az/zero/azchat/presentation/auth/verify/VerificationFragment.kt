package az.zero.azchat.presentation.auth.verify

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.TEST_VERIFICATION_CODE
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.show
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationFragment : BaseFragment(R.layout.fragment_verification) {

    val viewModel: VerificationViewModel by viewModels()
    private lateinit var binding: FragmentVerificationBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentVerificationBinding.bind(view)
        handleClicks()
        observeData()
    }

    private fun handleClicks() {
        binding.verifyBtn.setOnClickListener {
            viewModel.sendVerificationCode(requireActivity(), TEST_VERIFICATION_CODE)
        }
    }

    private fun observeData() {
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is VerificationEvent.VerificationSuccess -> {
                        binding.progressBarPb.progress.gone()
                        val action =
                            VerificationFragmentDirections.actionVerificationFragmentToExtraDetailsFragment()
                        findNavController().navigate(action)
                        Log.e("TAG", ":VerificationFragment\nUID= ${state.uid}")
                    }
                    is VerificationEvent.VerificationFailed -> {
                        binding.progressBarPb.progress.gone()
                        Log.e("TAG", ":VerificationFragment\nerror= ${state.msg}")
                        state.msg?.let { it1 -> toastMy(it1) }
                    }
                    VerificationEvent.VerifyButtonClick -> {
                        binding.progressBarPb.progress.show()
                    }
                }
            }
        }
    }


}