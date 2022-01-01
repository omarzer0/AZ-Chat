package az.zero.azchat.presentation.auth.login.country

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import az.zero.azchat.data.models.country_code.CountryCode
import az.zero.azchat.repository.AuthRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CountryCodeViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {

    private val _countryCode = MutableLiveData<List<CountryCode>>()
    val countryCode: LiveData<List<CountryCode>> = _countryCode

    init {
        getAllCountryCodes()
    }

    private fun getAllCountryCodes() {
        _countryCode.value = repository.getAllCountryCodes()
    }

}

