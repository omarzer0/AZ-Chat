package az.zero.azchat.presentation.main.chat_details

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.GROUPS_ID
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.presentation.auth.extra_details.UploadImageUseCase
import az.zero.azchat.presentation.main.chat_details.ChatDetailsFragment.Companion.ABOUT_CODE_KEY
import az.zero.azchat.presentation.main.chat_details.ChatDetailsFragment.Companion.NAME_CODE_KEY
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatDetailsViewModel @Inject constructor(
    private val stateHandler: SavedStateHandle,
    private val uploadImageUseCase: UploadImageUseCase,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val privateChat = stateHandler.get<PrivateChat>("privateChat")!!
//    private val group = privateChat.group
//    private val user = privateChat.user
//    private val isGroup = group.ofTypeGroup!!
//    private val name = if (isGroup) group.name!! else user.name!!
//    private val image = if (isGroup) group.image!! else user.imageUrl!!
//    private val about = if (isGroup) group.about!! else user.bio!!

    private val _event = MutableLiveData<Event<ChatDetailsEvent>>()
    val event: LiveData<Event<ChatDetailsEvent>> = _event


    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    fun getCurrentPrivateChat() = privateChat

    init {
        logMe("$privateChat", "ChatDetailsViewModel")
    }

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
    }

}

