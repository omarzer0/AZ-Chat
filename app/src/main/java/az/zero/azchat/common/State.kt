package az.zero.azchat.common

sealed class State {
    data class Success(val message: String?=null) : State()
    data class Error(val message: String?) : State()
    object Loading : State()
}