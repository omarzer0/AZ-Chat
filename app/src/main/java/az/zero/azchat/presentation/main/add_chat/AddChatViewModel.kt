package az.zero.azchat.presentation.main.add_chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddChatViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl,
    state: SavedStateHandle
) : ViewModel() {

    private val _event = MutableLiveData<Event<AddChatEvent>>()
    val event: LiveData<Event<AddChatEvent>> = _event

    private val _users = MutableLiveData<List<User>>()
    val user: LiveData<List<User>> = _users

    private val _searchQuery: MutableLiveData<String> = state.getLiveData(
        SEARCH_QUERY,
        START_SEARCH_QUERY
    )
    val searchQuery: LiveData<String> = _searchQuery

    init {
        getAllUsers()
    }

    private fun getAllUsers() {
        repositoryImpl.getAllUsers(
            onGetUsersDone = {
                _users.postValue(it)
                _event.postValue(Event(AddChatEvent.GetUsersToChatDone(it)))
            })
    }

    fun getGID() = repositoryImpl.getRandomFirebaseGID()
    fun getUID() = repositoryImpl.getUID()

    fun checkIfGroupExists(uID: String, otherUID: String, onSuccess: (String) -> Unit) {
        repositoryImpl.checkIfGroupExists(uID, otherUID, onSuccess)
    }

    fun searchUserByNameOrPhone(query: String) {
        _users.value?.let { users ->
            val searchedUsers = users.filter {
                it.name!!.toString().lowercase().trim().contains(query) ||
                        it.phoneNumber!!.toString().lowercase().trim().contains(query)
            }
            _event.postValue(Event(AddChatEvent.GetUsersToChatDone(searchedUsers)))
        }
    }

    fun setSearchQueryValue(country: String) {
        _searchQuery.postValue(country)
    }

    companion object {
        const val SEARCH_QUERY = "AddChatViewModel search query"
        const val START_SEARCH_QUERY = ""
    }
}

