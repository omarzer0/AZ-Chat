package az.zero.azchat.presentation.main.add_edit_info

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.GROUPS_ID
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.presentation.auth.extra_details.ExtraDetailsEvent
import az.zero.azchat.presentation.auth.extra_details.UploadImageUseCase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AddEditInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val firestore: FirebaseFirestore,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val selectedUsers = savedStateHandle.get<Array<String>>("selectedUsers") ?: emptyArray()
    val gid = firestore.collection(GROUPS_ID).document().id

    private val _event = MutableLiveData<Event<ExtraDetailsEvent>>()
    val event: LiveData<Event<ExtraDetailsEvent>> = _event


    private val _imageMLD = MutableLiveData<Uri>()
    val imageMLD: LiveData<Uri> = _imageMLD

    fun addNewGroup(
        groupName: String,
        aboutGroup: String,
        onSuccess: (newGroup: Group) -> Unit
    ) {
        val currentUserUID = sharedPreferenceManger.uid
        val image = imageMLD.value?.toString() ?: ""
        val newGroup = Group(
            gid,
            groupName,
            true,
            selectedUsers.toList(),
            Timestamp(Date()),
            Timestamp(Date()),
            currentUserUID,
            image,
            null,
            groupNotificationTopic = "/topics/$gid",
            about = aboutGroup
        )

        firestore.collection(GROUPS_ID).document(gid).set(newGroup)
        logMe("addNewGroup success= ", "addNewGroup")
        onSuccess(newGroup)
    }

    fun uploadGroupImage(uri: Uri) {
        _event.postValue(Event(ExtraDetailsEvent.UploadingImageLoading))
        uploadImageUseCase.invoke(uri, "chatRoomImages/$gid${System.currentTimeMillis()}",
            onUploadImageSuccess = {
                _imageMLD.postValue(it)
                _event.postValue(Event(ExtraDetailsEvent.UploadImageSuccess(it)))
            },
            onUploadImageFailed = {
                _event.postValue(Event(ExtraDetailsEvent.UploadImageFailed(it)))
            })
    }
}

