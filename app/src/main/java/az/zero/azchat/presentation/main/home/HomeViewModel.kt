package az.zero.azchat.presentation.main.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.data.models.private_chat.PrivateChat
import az.zero.azchat.data.models.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val sharedPreferenceManger: SharedPreferenceManger,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private var privateChatsListener: ListenerRegistration? = null

    private val _event = MutableLiveData<Event<HomeFragmentEvent>>()
    val event: LiveData<Event<HomeFragmentEvent>> = _event

    private val _privateChats = MutableLiveData<MutableList<PrivateChat>>()
    val privateChats: LiveData<MutableList<PrivateChat>>
        get() = _privateChats

    private fun getPrivateChatsForUser() {
        var localList: MutableList<PrivateChat>
        val uid = sharedPreferenceManger.uid
        privateChatsListener = firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
            .addSnapshotListener { value, error ->
                localList = mutableListOf()
                if (error != null || value == null) return@addSnapshotListener

//                value.documentChanges.forEach {
//                    logMe("${it.document.metadata.isFromCache}", "documentChanges")
//                }
                val groupsFromServer = value.documentChanges.filter {
                    !it.document.metadata.isFromCache
                }.map {
                    it.document.toObject<Group>()
                }


                logMe("$groupsFromServer", "documentChanges")
                tryAsyncNow(viewModelScope, action = {
                    value.forEach { document ->
                        val group = document.toObject<Group>()
                        if (group.ofTypeGroup == true) return@forEach
                        if (group.hasNullField()) return@forEach
                        val otherUserID = if (!group.user1!!.path.contains(uid)) group.user1!!.path
                        else group.user2!!.path

                        val fromServer = groupsFromServer.any { it.gid == group.gid }
                        val getFrom = if (fromServer) {
                            logMe("server ${group.gid}", "tryAsyncNow")
                            Source.SERVER
                        } else {
                            logMe("cache ${group.gid}", "tryAsyncNow")
                            Source.CACHE
                        }
                        val user = firestore.document(otherUserID).get(getFrom)
                            .await().toObject<User>() ?: return@forEach
                        if (user.hasNullField()) return@forEach
                        val privateChat = PrivateChat(group, user)
                        localList.add(privateChat)
                    }
                }, finally = {
                    localList.sortByDescending { it.group.lastSentMessage?.sentAt }
                    _privateChats.postValue(localList)
                })
            }
    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(gid: String, username: String, userImage: String, otherUserUID: String) {
        _event.postValue(
            Event(
                HomeFragmentEvent.PrivateChatsClick(
                    gid,
                    username,
                    userImage,
                    otherUserUID
                )
            )
        )
    }

    fun viewDestroyed() {
        privateChatsListener?.remove()
    }

    fun viewCreated() {
        getPrivateChatsForUser()
    }


    //    private val _chatsList = mutableListOf<PrivateChat>()

//    private fun getPrivateChatsForUser() {
//        viewModelScope.launch {
//            repositoryImpl.getPrivateChatsForUser { chat ->
//                val exist = _chatsList.find { it.group.gid == chat.group.gid }
//                if (exist != null) {
//                    //exists
//                    val index = _chatsList.indexOf(exist)
//                    _chatsList[index] = chat
//                } else {
//                    _chatsList.add(chat)
//                }
//                _chatsList.sortByDescending { it.group.lastSentMessage?.sentAt }
//                _privateChats.postValue(_chatsList)
//            }
//        }
//    }

}