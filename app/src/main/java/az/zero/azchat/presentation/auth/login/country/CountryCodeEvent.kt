package az.zero.azchat.presentation.auth.login.country

import az.zero.azchat.data.models.country_code.CountryCode

open class CountryCodeEvent {
    data class SearchCountry(val countries: List<CountryCode>) : CountryCodeEvent()
}