package az.zero.azchat.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.event.Event
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl
) : ViewModel() {

    init {
        getPrivateChatsForUser()
    }

    private val _event = MutableLiveData<Event<HomeFragmentEvent>>()
    val event: LiveData<Event<HomeFragmentEvent>> = _event

    private val _privateChats = MutableLiveData<List<PrivateChat>>()
    val privateChat: LiveData<List<PrivateChat>>
        get() = _privateChats

    private fun getPrivateChatsForUser() = viewModelScope.launch {
        repositoryImpl.getPrivateChatsForUser {
            _privateChats.postValue(it)
        }
    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(gid: String, username: String) {
        _event.postValue(Event(HomeFragmentEvent.PrivateChatsClick(gid, username)))
    }
}