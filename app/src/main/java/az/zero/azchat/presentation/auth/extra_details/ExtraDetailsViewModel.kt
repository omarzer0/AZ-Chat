package az.zero.azchat.presentation.auth.extra_details

import androidx.lifecycle.ViewModel
import az.zero.azchat.common.TEST_GROUP
import az.zero.azchat.common.TEST_USER
import az.zero.azchat.data.models.message.Message
import az.zero.azchat.repository.AuthRepositoryImpl
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExtraDetailsViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl
) : ViewModel() {


    fun getAllGroupsByUserUID() {
        repository.getAllGroupsByUserUID()
    }

    fun addGroup() {
        repository.addGroup()
    }

    fun getMessagesByGroupId() {
        repository.getMessagesByGroupId(TEST_GROUP)
    }

    fun addMessage() {
        val message = Message("added msg", Timestamp(Date()), TEST_USER)
        repository.addMessage(message, TEST_GROUP)
    }

}

