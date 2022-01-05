package az.zero.azchat.presentation.main.home

import androidx.lifecycle.ViewModel
import az.zero.azchat.data.models.group.Group
import az.zero.azchat.repository.MainRepositoryImpl
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl
) : ViewModel() {

    init {
//        repositoryImpl.getAllGroupsByUserUID()
//        repositoryImpl.addGroup()
    }

    fun getAdapterQuery(): FirestoreRecyclerOptions<Group> {
        val collection = repositoryImpl.getCollectionReference()
        val query: Query = collection.orderBy("modifiedAt", Query.Direction.ASCENDING)
        return FirestoreRecyclerOptions.Builder<Group>()
            .setQuery(query, Group::class.java)
            .build()
    }
}

