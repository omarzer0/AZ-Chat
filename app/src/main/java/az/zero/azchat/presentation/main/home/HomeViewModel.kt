package az.zero.azchat.presentation.main.home

import androidx.lifecycle.*
import az.zero.azchat.common.*
import az.zero.azchat.common.event.Event
import az.zero.azchat.domain.models.group.Group
import az.zero.azchat.domain.models.private_chat.PrivateChat
import az.zero.azchat.domain.models.user.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
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
    private var localList: MutableList<PrivateChat> = mutableListOf()

    private fun getPrivateChatsForUser() {
        val uid = sharedPreferenceManger.uid
        privateChatsListener = firestore.collection(GROUPS_ID).whereArrayContains("members", uid)
            .addSnapshotListener { value, error ->
                localList = mutableListOf()
                if (error != null || value == null) return@addSnapshotListener

                tryAsyncNow(viewModelScope, action = {
                    getPrivateChats(value, uid, Source.CACHE)
                }, error = {
                    if (it.localizedMessage?.contains(FAILED_TO_GET_FROM_CACHE) == true)
                        getPrivateChats(value, uid, Source.SERVER)
                }, finally = {
                    localList.sortByDescending { it.group.lastSentMessage?.sentAt }
                    _privateChats.postValue(localList)
                })
            }
    }

    private suspend fun getPrivateChats(query: QuerySnapshot, uid: String, from: Source) {
        query.forEach { document ->
            val group = document.toObject<Group>()
            if (group.ofTypeGroup == true) return@forEach
            if (group.hasNullField()) return@forEach
            val otherUserID = if (!group.user1!!.path.contains(uid)) group.user1!!.path
            else group.user2!!.path

            val user = firestore.document(otherUserID).get(from).await().toObject<User>()
                ?: return@forEach
            if (user.hasNullField()) return@forEach
            val privateChat = PrivateChat(group, user, group.gid!!)
            localList.add(privateChat)
        }
    }

    fun addUserClick() {
        _event.postValue(Event(HomeFragmentEvent.AddChat))
    }

    fun privateChatClick(
        gid: String,
        username: String,
        userImage: String,
        otherUserUID: String,
        notificationToken: String
    ) {
        _event.postValue(
            Event(
                HomeFragmentEvent.PrivateChatsClick(
                    gid, username, userImage, otherUserUID, notificationToken
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

//private fun getPrivateChats(){
//        tryAsyncNow(viewModelScope, action = {
//            value.forEach { document ->
//                val group = document.toObject<Group>()
//                if (group.ofTypeGroup == true) return@forEach
//                if (group.hasNullField()) return@forEach
//                val otherUserID = if (!group.user1!!.path.contains(uid)) group.user1!!.path
//                else group.user2!!.path
//
////                        val user = firestore.document(otherUserID).get().await().toObject<User>()
////                            ?: return@forEach
//
////                        // TODO 2: The user is not gonna update in the home screen as we are getting it form the cache first
//
//                val user = firestore.document(otherUserID).get(Source.CACHE)
//                    .await().toObject<User>() ?: firestore.document(otherUserID)
//                    .get(Source.SERVER).await().toObject<User>() ?: return@forEach
//
//                if (user.hasNullField()) return@forEach
//                val privateChat = PrivateChat(group, user, group.gid!!)
//                localList.add(privateChat)
//
////                        var test = ""
////                        localList.forEach {
////                            test += "$it\n\n\n\n"
////                        }
////                        logMe("${localList.size}\n\n\n\n$test", "getPrivateChatsForUser222222")
////
////                        localList.add()
////                        localList.add(privateChat)
////                        _privateChats.value = localList
//
//            }
//        }, error = {
//            if (it.localizedMessage?.contains(FAILED_TO_GET_FROM_CACHE) == true) {
//
//            }
//        }, finally = {
//            val x = localList.apply {
//                distinctBy { it.user.name }
//                sortByDescending { it.group.lastSentMessage?.sentAt }
//            }
//            logMe("${x.size}\n\n\n\n$x", "getPrivateChatsForUser")
//            _privateChats.postValue(localList)
//        })
//    }