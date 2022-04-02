package az.zero.azchat.presentation.main.user.user_bottom_sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserBottomSheetViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle
) : ViewModel() {
    val code = stateHandle.get<String>("code") ?: ""
    val text = stateHandle.get<String>("text") ?: ""
}

