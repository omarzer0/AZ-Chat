package az.zero.azchat.presentation.auth.country

import az.zero.azchat.domain.models.country_code.CountryCode

open class CountryCodeEvent {
    data class SearchCountry(val countries: List<CountryCode>) : CountryCodeEvent()
}