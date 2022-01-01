package az.zero.phoneloginmvvm.presentation.login

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.phoneloginmvvm.R
import az.zero.phoneloginmvvm.common.TEST_USER
import az.zero.phoneloginmvvm.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        handleClicks()
        observeData()

    }

    private fun handleClicks() {
        binding.loginBtn.setOnClickListener {
            //viewModel.login("+201234567890", requireActivity())

            // for testing
            val action =
                LoginFragmentDirections.actionLoginFragmentToExtraDetailsFragment(TEST_USER)
            findNavController().navigate(action)
        }
    }

    private fun observeData() {
        viewModel.state.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    LoginState.CodeSent -> {
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToVerificationFragment()
                        findNavController().navigate(action)
                    }
                    is LoginState.VerificationSuccess -> {
                        val action =
                            LoginFragmentDirections.actionLoginFragmentToExtraDetailsFragment(state.uid)
                        findNavController().navigate(action)
                        Log.e("TAG", ":LoginFragment\nUID= ${state.uid}")
                    }
                    is LoginState.VerificationFailed -> {
                        Log.e("TAG", ":LoginFragment\nerror= ${state.msg}")
                    }
                }
            }
        }
    }


}