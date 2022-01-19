package az.zero.azchat.data.models.status

data class Status(
    var writing: Boolean? = null,
    var online: Boolean? = null
) {
    fun hasNullField() =
        listOf(writing, online).any { it == null }
}
