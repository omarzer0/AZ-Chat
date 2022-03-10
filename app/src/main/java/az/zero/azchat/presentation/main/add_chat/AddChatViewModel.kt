package az.zero.azchat.presentation.main.add_chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import az.zero.azchat.common.GROUPS_ID
import az.zero.azchat.common.SharedPreferenceManger
import az.zero.azchat.common.USERS_ID
import az.zero.azchat.common.event.Event
import az.zero.azchat.common.logMe
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddChatViewModel @Inject constructor(
    private val state: SavedStateHandle,
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _event = MutableLiveData<Event<AddChatEvent>>()
    val event: LiveData<Event<AddChatEvent>> = _event

    private val _users = MutableLiveData<List<User>>()

    private val _searchQuery: MutableLiveData<String> = state.getLiveData(
        SEARCH_QUERY,
        START_SEARCH_QUERY
    )
    val searchQuery: LiveData<String> = _searchQuery

    init {
        getAllUsers()
    }

    private fun getAllUsers() {
        val uid = sharedPreferenceManger.uid
        val users = ArrayList<User>()
        firestore.collection(USERS_ID).get().addOnSuccessListener { documents ->
            documents.forEach { document ->
                val user = document.toObject<User>()
                if (!user.hasNullField() && user.uid != uid) {
                    users.add(user)
                } else {
                    logMe("Has null: $user")
                }
            }
            _users.postValue(users)
            _event.postValue(Event(AddChatEvent.GetUsersToChatDone(users)))

        }.addOnFailureListener {
            logMe("getAllUsersByPhoneNumber ${it.localizedMessage}")
        }

    }

    fun getGID() = firestore.collection(GROUPS_ID).document().id
    fun getUID() = sharedPreferenceManger.uid

    fun checkIfGroupExists(uID: String, otherUID: String, onSuccess: (String) -> Unit) {
        firestore.collection(GROUPS_ID).whereArrayContains("members", uID).get()
            .addOnSuccessListener {
                val group = it.find { document ->
                    val group = document.toObject<Group>()
                    if (group.hasNullField()) return@addOnSuccessListener
                    group.members!!.contains(otherUID)
                }
                group?.let { document ->
                    val existGroup = document.toObject<Group>()
                    if (existGroup.hasNullField()) return@addOnSuccessListener
                    onSuccess(existGroup.gid!!)
                } ?: onSuccess("")
            }
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

    fun setSearchQueryValue(searchQuery: String) {
        _searchQuery.postValue(searchQuery)
    }

    companion object {
        const val SEARCH_QUERY = "AddChatViewModel search query"
        const val START_SEARCH_QUERY = ""
    }
}

