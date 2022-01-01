package az.zero.phoneloginmvvm.presentation.verify

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.phoneloginmvvm.R
import az.zero.phoneloginmvvm.databinding.FragmentVerificationBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VerificationFragment : Fragment(R.layout.fragment_verification) {

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
            viewModel.sendVerificationCode(requireActivity(), "123456")
        }
    }

    private fun observeData() {
        viewModel.state.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    is VerifyState.VerificationSuccess -> {
                        val action = VerificationFragmentDirections
                            .actionVerificationFragmentToExtraDetailsFragment(state.uid)
                        findNavController().navigate(action)
                        Log.e("TAG", ":VerificationFragment\nUID= ${state.uid}")
                    }
                    is VerifyState.VerificationFailed -> {
                        Log.e("TAG", ":VerificationFragment\nerror= ${state.msg}")
                    }
                }
            }
        }
    }


}