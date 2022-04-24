package az.zero.azchat.presentation.main.about_me

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.common.ABOUT_ME_DOC_ID
import az.zero.azchat.common.ABOUT_ME_ID
import az.zero.azchat.common.tryAsyncNow
import az.zero.azchat.domain.models.about_me.AboutMe
import az.zero.azchat.domain.models.simple_info.SimpleInfo
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AboutMeViewModel @Inject constructor(
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _aboutMeMLD = MutableLiveData<AboutMe>()
    val aboutMesLD: LiveData<AboutMe> = _aboutMeMLD


    init {
        tryAsyncNow(viewModelScope) {
            val aboutMe = firestore.collection(ABOUT_ME_ID).document(ABOUT_ME_DOC_ID).get().await()
                .toObject<AboutMe>() ?: return@tryAsyncNow

            _aboutMeMLD.value = aboutMe
        }
    }

}

