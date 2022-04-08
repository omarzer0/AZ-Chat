package az.zero.azchat.presentation.main.chat_details

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.*
import az.zero.azchat.common.GROUPS_ID
import az.zero.azchat.common.USERS_ID
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.common.tryAsyncNow
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import az.zero.azchat.presentation.auth.extra_details.UploadImageUseCase
import az.zero.azchat.presentation.main.chat_details.ChatDetailsFragment.Companion.ABOUT_CODE_KEY
import az.zero.azchat.presentation.main.chat_details.ChatDetailsFragment.Companion.NAME_CODE_KEY
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle,
    private val uploadImageUseCase: UploadImageUseCase,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    var haveUpdate: Boolean = false
    private var privateChat = stateHandler.get<PrivateChat>("privateChat")!!

    private val _event = MutableLiveData<Event<ChatDetailsEvent>>()
    val event: LiveData<Event<ChatDetailsEvent>> = _event


    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    private val _usersMLD = MutableLiveData<List<User>>()
    val usersMLD: LiveData<List<User>> = _usersMLD

    fun getCurrentPrivateChat() = privateChat


    fun updateChatRoomImage(uri: Uri) {
        _event.postValue(Event(ChatDetailsEvent.UploadingImageLoading))
        uploadImageUseCase(
            uri,
            "chatRoomImages/${privateChat.id}${System.currentTimeMillis()}",
            onUploadImageSuccess = {
                _imageMLD.value = it
                _event.postValue(Event(ChatDetailsEvent.UploadImageSuccess(it)))
                firestore.collection(GROUPS_ID).document(privateChat.id)
                    .update("image", it.toString())
                privateChat =
                    privateChat.copy(group = privateChat.group.copy(image = it.toString()))

            },
            onUploadImageFailed = {
                _event.postValue(Event(ChatDetailsEvent.UploadImageFailed(it)))
            })
    }

    fun updateNameOrAbout(bundle: Bundle) {
        if (bundle[NAME_CODE_KEY] == null && bundle[ABOUT_CODE_KEY] == null) return
        val isName = bundle[NAME_CODE_KEY] != null
        val fieldToUpdate = if (isName) "name" else "about"
        val value: String =
            if (isName) bundle[NAME_CODE_KEY].toString() else bundle[ABOUT_CODE_KEY].toString()
        firestore.collection(GROUPS_ID).document(privateChat.id).update(fieldToUpdate, value)
        _event.postValue(Event(ChatDetailsEvent.UpdateText(isName, value)))
        val editedGroup = if (fieldToUpdate == "name") privateChat.group.copy(name = value)
        else privateChat.group.copy(about = value)
        privateChat = privateChat.copy(group = editedGroup)
    }




    private fun getAllUsersForGroup(ids: List<String>) {
        val users = ArrayList<User>()
        tryAsyncNow(viewModelScope) {
            ids.forEach { id ->
                val user = firestore.collection(USERS_ID).document(id).get()
                    .await().toObject<User>() ?: return@forEach
                if (!user.hasNullField()) users.add(user)
            }
            _usersMLD.postValue(users)
        }
    }

    init {
        logMe("$privateChat", "ChatDetailsViewModel")
        privateChat.group.ofTypeGroup?.let { itIsGroup ->
            if (itIsGroup) getAllUsersForGroup(privateChat.group.members ?: emptyList())
        }
    }
}

