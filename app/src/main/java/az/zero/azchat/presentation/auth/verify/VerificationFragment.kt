package az.zero.azchat.presentation.auth.verify

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.extension.showKeyboard
import az.zero.azchat.common.logMe
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

//        binding.otpEd.requestFocus()
//        showKeyboard()
    }

    private fun handleClicks() {
        binding.otpEd.setOnCompleteListener { verificationCode ->
            hideKeyboard()
            viewModel.sendVerificationCode(requireActivity(), verificationCode)
        }
    }

    private fun observeData() {
        viewModel.event.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { event ->
                when (event) {
                    is VerificationEvent.VerificationSuccess -> {
                        viewModel.getIfUserExist()

                        binding.progressBarPb.progress.gone()
                        Log.e("TAG", ":VerificationFragment\nUID= ${event.uid}")
                        toastMy(getString(R.string.verified_successfully), true)

                    }
                    is VerificationEvent.VerificationFailed -> {
                        binding.progressBarPb.progress.gone()
                        Log.e("TAG", ":VerificationFragment\nerror= ${event.msg}")
                        binding.otpEd.triggerErrorAnimation()
                    }
                    VerificationEvent.VerifyButtonClick -> {
                        binding.progressBarPb.progress.show()
                    }

                    VerificationEvent.UserExist -> {
                        // go to main activity
                        loginInToActivity()
                    }

                    VerificationEvent.UserDoesNotExist -> {
                        navigateToAction(VerificationFragmentDirections.actionVerificationFragmentToExtraDetailsFragment())
                    }

                    is VerificationEvent.OnUserExistCallFail -> {
                        logMe(event.error)
                    }
                }
            }
        }
    }


}