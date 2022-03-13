package az.zero.azchat.domain.models.status

data class Status(
    var writing: Boolean? = null,
    var online: Boolean? = null
) {
    fun hasNullField() =
        listOf(writing, online).any { it == null }
}
