package az.zero.azchat.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl
) : ViewModel() {


    private val _event = MutableLiveData<Event<HomeFragmentEvent>>()
    val event: LiveData<Event<HomeFragmentEvent>> = _event

    private val _chatsList = mutableListOf<PrivateChat>()

    private val _privateChats = MutableLiveData<MutableList<PrivateChat>>()
    val privateChats: LiveData<MutableList<PrivateChat>>
        get() = _privateChats

    private fun getPrivateChatsForUser() {
        viewModelScope.launch {
            repositoryImpl.getPrivateChatsForUser { chat ->
                val exist = _chatsList.find { it.group.gid == chat.group.gid }
                if (exist != null) {
                    //exists
                    val index = _chatsList.indexOf(exist)
                    _chatsList[index] = chat
                } else {
                    _chatsList.add(chat)
                }
                _chatsList.sortByDescending { it.group.lastSentMessage?.sentAt }
                _privateChats.postValue(_chatsList)
            }
        }
    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(gid: String, username: String) {
        _event.postValue(Event(HomeFragmentEvent.PrivateChatsClick(gid, username)))
    }

    init {
//        getPrivateChatsForUser()
    }

    override fun onCleared() {
        super.onCleared()
        logMe("cleared")
    }

    fun viewDestroyed() {
        repositoryImpl.removePrivateChatsListener()
    }

    fun viewCreated() {
        getPrivateChatsForUser()
    }

}