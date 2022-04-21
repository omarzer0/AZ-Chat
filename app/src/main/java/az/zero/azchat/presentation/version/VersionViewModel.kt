package az.zero.azchat.presentation.version

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.VERSIONS_DOC_ID
import az.zero.azchat.common.VERSIONS_ID
import az.zero.azchat.common.tryAsyncNow
import az.zero.azchat.domain.models.versions.Versions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@HiltViewModel
class VersionViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _versionsMLD = MutableLiveData<Versions>()
    val versionsLD: LiveData<Versions> = _versionsMLD


    init {
        tryAsyncNow(viewModelScope) {
            val versions = firestore.collection(VERSIONS_ID).document(VERSIONS_DOC_ID).get().await()
                .toObject<Versions>() ?: return@tryAsyncNow

            _versionsMLD.value = versions
        }
    }
}

