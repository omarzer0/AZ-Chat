package az.zero.azchat.presentation.auth.country

import androidx.lifecycle.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.data.models.country_code.CountryCode
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryCodeViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl,
    private val state: SavedStateHandle
) : ViewModel() {

    private val _event = MutableLiveData<Event<CountryCodeEvent>>()
    val event: LiveData<Event<CountryCodeEvent>> = _event

    private val _countryCode = MutableLiveData<List<CountryCode>>()
    val countryCode: LiveData<List<CountryCode>> = _countryCode

    private val _searchQuery: MutableLiveData<String> =
        state.getLiveData(SEARCH_QUERY, START_SEARCH_QUERY)
    val searchQuery: LiveData<String> = _searchQuery


    init {
        getAllCountryCodes()
    }

    private fun getAllCountryCodes() {
        viewModelScope.launch {
            repository.getAllCountryCodes(onSuccess = {
                _countryCode.postValue(it)
            }, onFailure = {
                _countryCode.postValue(it)
            })
        }
    }

    fun searchCountry(country: String) {
        _countryCode.value?.let { countries ->
            val searchedCountries =
                countries.filter { it.name.lowercase().contains(country.trim().lowercase()) }
            _event.postValue(Event(CountryCodeEvent.SearchCountry(searchedCountries)))
        }
    }

    fun setSearchQueryValue(country: String) {
        _searchQuery.postValue(country)
    }

    companion object {
        const val SEARCH_QUERY = "searchQuery"
        const val START_SEARCH_QUERY = ""
    }
}

