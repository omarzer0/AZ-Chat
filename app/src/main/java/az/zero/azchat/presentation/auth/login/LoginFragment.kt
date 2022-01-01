package az.zero.azchat.presentation.auth.login

import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.TEST_USER
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentLoginBinding
import az.zero.azchat.presentation.auth.login.country.CountryCodeFragment
import az.zero.azchat.presentation.auth.login.country.CountryCodeFragment.Companion.COUNTRY_CODE_REQUEST
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        setFragmentResultListener(COUNTRY_CODE_REQUEST) { key, bundle ->
            // read from the bundle
            if (key == COUNTRY_CODE_REQUEST) logMe("${bundle.getString("data")}")
            else logMe("error $key ${bundle.getString("data")}")
        }

        handleClicks()
        observeData()

    }

    private fun handleClicks() {
        binding.loginBtn.setOnClickListener {
            //viewModel.login("+201234567890", requireActivity())

            // for testing
            navigateToAction(
                LoginFragmentDirections.actionLoginFragmentToExtraDetailsFragment(
                    TEST_USER
                )
            )
        }

        binding.countryCl.setOnClickListener {
            navigateToAction(LoginFragmentDirections.actionLoginFragmentToCountryCodeFragment())
        }
    }

    private fun observeData() {
        viewModel.state.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { state ->
                when (state) {
                    LoginState.CodeSent -> {
                        navigateToAction(LoginFragmentDirections.actionLoginFragmentToVerificationFragment())
                    }
                    is LoginState.VerificationSuccess -> {
                        navigateToAction(
                            LoginFragmentDirections.actionLoginFragmentToExtraDetailsFragment(
                                state.uid
                            )
                        )
                        logMe(":LoginFragment\nUID= ${state.uid}")
                    }
                    is LoginState.VerificationFailed -> {
                        logMe(":LoginFragment\nUID= ${state.msg}")
                    }
                }
            }
        }
    }


}