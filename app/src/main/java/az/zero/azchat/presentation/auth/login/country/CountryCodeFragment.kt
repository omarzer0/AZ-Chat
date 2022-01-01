package az.zero.azchat.presentation.auth.login.country

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import az.zero.azchat.R
import az.zero.azchat.common.logMe
import az.zero.azchat.databinding.FragmentCountryCodeBinding
import az.zero.azchat.presentation.auth.adapter.country_code.CountryCodeAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryCodeFragment : Fragment(R.layout.fragment_country_code) {

    private val viewModel: CountryCodeViewModel by viewModels()
    private lateinit var binding: FragmentCountryCodeBinding
    private val countryCodeAdapter = CountryCodeAdapter()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCountryCodeBinding.bind(view)

        setUpRV()
        observeData()
        handleClicks()


//        setFragmentResult(COUNTRY_CODE_REQUEST, bundleOf("data" to "Hi"))
//        findNavController().navigateUp()
    }


    private fun setUpRV() {
        binding.countriesRv.adapter = countryCodeAdapter
    }

    private fun handleClicks() {
        countryCodeAdapter.setOnCountryCodeItemClickListener {
            logMe("$it")
        }
    }

    private fun observeData() {
        viewModel.countryCode.observe(viewLifecycleOwner) { countryCodes ->
            countryCodeAdapter.changeItems(countryCodes)
        }
    }


    companion object {
        const val COUNTRY_CODE_REQUEST = "COUNTRY_CODE_REQUEST"
    }
}