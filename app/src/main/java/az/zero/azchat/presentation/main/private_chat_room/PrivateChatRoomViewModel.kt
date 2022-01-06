package az.zero.azchat.presentation.main.private_chat_room

import androidx.lifecycle.ViewModel
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PrivateChatRoomViewModel @Inject constructor(
    private val repository: MainRepositoryImpl
) : ViewModel() {

}

