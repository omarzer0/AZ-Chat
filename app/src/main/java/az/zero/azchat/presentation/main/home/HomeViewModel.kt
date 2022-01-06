package az.zero.azchat.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.Event
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl
) : ViewModel() {

    init {
        getPrivateChatsForUser()
    }

    private val _event = MutableLiveData<Event<HomeFragmentEvent>>()
    val event: LiveData<Event<HomeFragmentEvent>> = _event

    private fun getPrivateChatsForUser() {
        viewModelScope.launch {
            repositoryImpl.getPrivateChatsForUser().collect {
                _event.postValue(Event(HomeFragmentEvent.GetPrivateChats(it)))
            }
        }

    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(gid: String) {
        _event.postValue(Event(HomeFragmentEvent.PrivateChatsClick(gid)))

    }
}

