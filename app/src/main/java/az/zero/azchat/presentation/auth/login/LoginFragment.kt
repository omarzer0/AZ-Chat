package az.zero.azchat.presentation.auth.login

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.extension.gone
import az.zero.azchat.common.extension.hideKeyboard
import az.zero.azchat.common.extension.show
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentLoginBinding
import az.zero.azchat.domain.models.country_code.CountryCode
import az.zero.azchat.presentation.auth.country.CountryCodeFragment.Companion.COUNTRY_CODE_KEY
import az.zero.azchat.presentation.auth.country.CountryCodeFragment.Companion.COUNTRY_CODE_REQUEST
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : BaseFragment(R.layout.fragment_login) {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)
        handleClicks()
        getDataFromOtherFragmentIFExists()
        observeViewEvents()

    }

    private fun handleClicks() {
        binding.loginBtn.setOnClickListener {
            val code = binding.codeEd.text.toString()
            val number = binding.numberEd.text.toString()
            logMe(code)
            logMe(number)
            hideKeyboard()
            viewModel.login(code, number, requireActivity())
        }

        binding.countryCl.setOnClickListener {
            navigateToAction(LoginFragmentDirections.actionLoginFragmentToCountryCodeFragment())
        }

        binding.codeEd.doOnTextChanged { code, _, _, _ ->
            handleTextChanges(code)
        }
    }

    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                LoginEvent.CodeSent -> {
                    binding.progressBarPb.progress.gone()
                    navigateToAction(LoginFragmentDirections.actionLoginFragmentToVerificationFragment())
                }
                is LoginEvent.VerificationSuccess -> {
                    binding.progressBarPb.progress.gone()
                    viewModel.getIfUserExist()
                    logMe(":LoginFragment\nUID= ${event.uid}")
                }
                LoginEvent.VerificationTimeOut -> {
                    navigateToAction(LoginFragmentDirections.actionLoginFragmentToVerificationFragment())
                }

                LoginEvent.UserExist -> {
                    // go to main activity
                    loginInToActivity()
                }

                LoginEvent.UserDoesNotExist -> {
                    navigateToAction(
                        LoginFragmentDirections.actionLoginFragmentToExtraDetailsFragment()
                    )
                }

                is LoginEvent.OnUserExistCallFail -> {
                    logMe(event.error)
                }

                is LoginEvent.VerificationFailed -> {
                    binding.progressBarPb.progress.gone()
                    event.msg?.let { toastMy(it) }
                }
                LoginEvent.LoginBtnClick -> {
                    binding.progressBarPb.progress.show()
                    hideKeyboard()
                }
                is LoginEvent.InvalidInputs -> {
                    toastMy(event.msg, false)
                }
                is LoginEvent.CountryCodeExists -> {
                    binding.countryTextTv.text = event.countryName
                    binding.plusTv.text = "+"
                }
                is LoginEvent.CountryCodeInvalid -> {
                    binding.countryTextTv.text = getString(R.string.invalid_code)
                    binding.plusTv.text = "+"
                }
                LoginEvent.CountryCodeNull -> {
                    logMe("_countryCode is null")
                }
                is LoginEvent.ReceivedCountryCodeFormOtherFragment -> {
                    binding.codeEd.setText(event.callingCode)
                    binding.countryTextTv.text = event.countryName
                    binding.plusTv.text = "+"
                }
            }
        }
    }

    private fun handleTextChanges(code: CharSequence?) {
        if (code.isNullOrEmpty()) {
            binding.countryTextTv.text = getString(R.string.choose_a_country)
            binding.plusTv.text = ""
            return
        }

        binding.plusTv.text = "+"
        viewModel.getCountryCodeByCode(code.toString())
    }

    private fun getDataFromOtherFragmentIFExists() {
        setFragmentResultListener(COUNTRY_CODE_REQUEST) { key, bundle ->
            if (key == COUNTRY_CODE_REQUEST) {
                val countryCode = bundle.getParcelable<CountryCode>(COUNTRY_CODE_KEY)
                viewModel.getFragmentResult(countryCode)
            } else logMe("error $key")
        }
    }
}