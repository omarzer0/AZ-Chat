package az.zero.azchat.domain.models.about_me

import az.zero.azchat.domain.models.simple_info.SimpleInfo

data class AboutMe(
    val name: String = "",
    val about: String = "",
    val image: String = "",
    val links: List<SimpleInfo> = emptyList()
)