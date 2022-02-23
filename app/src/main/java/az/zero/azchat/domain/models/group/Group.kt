package az.zero.azchat.domain.models.group

import az.zero.azchat.domain.models.message.Message
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference

data class Group(
    var gid: String? = null,
    var name: String? = null,
    var ofTypeGroup: Boolean? = null,
    var members: List<String>? = null,
    var createdAt: Timestamp? = null,
    var modifiedAt: Timestamp? = null,
    var createdBy: String? = null,
    var image: String? = null,
    var lastSentMessage: Message? = null,
    var user1: DocumentReference? = null,
    var user2: DocumentReference? = null
) {
    fun hasNullField() =
        listOf(
            gid,
            name,
            ofTypeGroup,
            members,
            createdAt,
            modifiedAt,
            createdBy,
            image,
            user1,
            user2
        ).any { it == null }

}
