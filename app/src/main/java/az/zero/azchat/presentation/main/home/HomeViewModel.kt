package az.zero.azchat.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore,
    private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {

    private var privateChatsListener: ListenerRegistration? = null

    private val _event = MutableLiveData<Event<HomeFragmentEvent>>()
    val event: LiveData<Event<HomeFragmentEvent>> = _event

    private val _privateChats = MutableLiveData<MutableList<PrivateChat>>()
    val privateChats: LiveData<MutableList<PrivateChat>>
        get() = _privateChats

    private fun getPrivateChatsForUser() {
        val uid = sharedPreferenceManger.uid
        val blockList = sharedPreferenceManger.blockList
        privateChatsListener = firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
            .addSnapshotListener { value, error ->
                if (error != null || value == null) return@addSnapshotListener

                tryAsyncNow(viewModelScope) {
                    val privateChats = getPrivateChats(value, uid, Source.DEFAULT, blockList)
                    privateChats.sortByDescending { it.group.lastSentMessage?.sentAt }
                    _privateChats.value = privateChats
                }
            }
    }

    private suspend fun getPrivateChats(
        query: QuerySnapshot,
        uid: String,
        from: Source,
        blockList: List<String>
    ): MutableList<PrivateChat> {
        val privateChatList = mutableListOf<PrivateChat>()
        query.forEach { document ->
            val group = document.toObject<Group>()
            if (group.hasNullField()) return@forEach

            val user = if (!group.ofTypeGroup!!) { // not a group
                val otherUserID = if (!group.user1!!.contains(uid)) group.user1!!
                else group.user2!!
                val firebaseUser =
                    firestore.document(otherUserID).get(from).await().toObject<User>()
                        ?: return@forEach
                if (firebaseUser.hasNullField()) return@forEach
                firebaseUser
            } else User()

            // user or group is blocked
            if (!group.ofTypeGroup!!) {
                if (blockList.any { it == user.uid!! }) return@forEach
            } else {
                if (blockList.any { it == group.gid!! }) return@forEach
            }

            val privateChat = PrivateChat(group, user, group.gid!!)
            privateChatList.add(privateChat)
        }
        return privateChatList
    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(privateChat: PrivateChat) {
        _event.postValue(Event(HomeFragmentEvent.PrivateChatsClick(privateChat)))
    }

    fun viewDestroyed() {
        privateChatsListener?.remove()
    }

    fun viewCreated() {
        getPrivateChatsForUser()
    }

    fun blockUser(privateChatID: String) {
        val blockList = sharedPreferenceManger.blockList.toMutableList()
            .apply { add(privateChatID) }.toSet().toList()
        sharedPreferenceManger.blockList = blockList
        logMe("${sharedPreferenceManger.blockList}", "blockUserBlockList")
        val uid = sharedPreferenceManger.uid
        firestore.collection(USERS_ID).document(uid)
            .update("blockList", FieldValue.arrayUnion(privateChatID))
            .addOnSuccessListener { getPrivateChatsForUser() }


    }

    fun leaveGroup(privateChatID: String) {
        val uid = sharedPreferenceManger.uid
        firebaseMessaging.unsubscribeFromTopic("/topics/$privateChatID")
        firestore.collection(GROUPS_ID).document(privateChatID)
            .update("members", FieldValue.arrayRemove(uid)).addOnSuccessListener {
                getPrivateChatsForUser()
            }
    }
}