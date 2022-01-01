package az.zero.phoneloginmvvm.presentation.extra_details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.phoneloginmvvm.common.TEST_GROUP
import az.zero.phoneloginmvvm.common.TEST_USER
import az.zero.phoneloginmvvm.data.models.message.Message
import az.zero.phoneloginmvvm.repository.MainRepositoryImpl
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExtraDetailsViewModel @Inject constructor(
    private val repository: MainRepositoryImpl,
    private val state: SavedStateHandle
) : ViewModel() {

    private val uid = state.get<String>("uid") ?: ""


    fun getAllGroupsByUserUID() {
        repository.getAllGroupsByUserUID(uid)
    }

    fun addGroup() {
        repository.addGroup()
    }

    fun getMessagesByGroupId() {
        repository.getMessagesByGroupId(TEST_GROUP)
    }

    fun addMessage() {
        val message = Message("added msg", Timestamp(Date()), TEST_USER)
        repository.addMessage(message,TEST_GROUP)
    }

}

