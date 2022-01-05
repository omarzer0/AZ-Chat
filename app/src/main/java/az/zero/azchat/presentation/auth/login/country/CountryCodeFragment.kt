package az.zero.azchat.presentation.auth.login.country

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import az.zero.azchat.R
import az.zero.azchat.common.extension.onQueryTextChanged
import az.zero.azchat.common.logMe
import az.zero.azchat.core.BaseFragment
import az.zero.azchat.databinding.FragmentCountryCodeBinding
import az.zero.azchat.presentation.auth.adapter.country_code.CountryCodeAdapter
import az.zero.azchat.presentation.auth.login.country.CountryCodeViewModel.Companion.START_SEARCH_QUERY
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CountryCodeFragment : BaseFragment(R.layout.fragment_country_code) {

    private val viewModel: CountryCodeViewModel by viewModels()
    private lateinit var binding: FragmentCountryCodeBinding
    private lateinit var searchView: SearchView
    private val countryCodeAdapter = CountryCodeAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCountryCodeBinding.bind(view)

        setUpRV()
        observeData()
        handleClicks()
        observeViewEvents()
        setHasOptionsMenu(true)
    }

    private fun setUpRV() {
        binding.countriesRv.adapter = countryCodeAdapter
    }

    private fun handleClicks() {
        countryCodeAdapter.setOnCountryCodeItemClickListener {
            logMe("$it")
            setFragmentResult(COUNTRY_CODE_REQUEST, bundleOf(COUNTRY_CODE_KEY to it))
            findNavController().navigateUp()
        }
    }

    private fun observeData() {
        viewModel.countryCode.observe(viewLifecycleOwner) { countryCodes ->
            countryCodeAdapter.changeItems(countryCodes)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.country_code_menu, menu)
        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView

        val pendingQuery = viewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty() && pendingQuery != START_SEARCH_QUERY) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        viewModel.searchQuery.observe(viewLifecycleOwner) {
            logMe("changed")
            viewModel.searchCountry(it)
        }

        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem?): Boolean = true
            override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
                logMe("collapse")
                return true
            }
        })

        searchView.onQueryTextChanged { country ->
            viewModel.setSearchQueryValue(country)
        }
    }


    private fun observeViewEvents() {
        viewModel.event.observeIfNotHandled { event ->
            when (event) {
                is CountryCodeEvent.SearchCountry -> {
                    countryCodeAdapter.changeItems(event.countries)
                }
            }
        }
    }

    companion object {
        const val COUNTRY_CODE_REQUEST = "COUNTRY_CODE_REQUEST"
        const val COUNTRY_CODE_KEY = "country"
    }

    override fun onDestroy() {
        super.onDestroy()
        searchView.setOnQueryTextListener(null)
    }
}