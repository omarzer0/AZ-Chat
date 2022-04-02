package az.zero.azchat.presentation.main.chat_details.chat_details_bottom_sheet

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatDetailsBottomSheetViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle
) : ViewModel() {

    val code = stateHandle.get<String>("code") ?: ""
    val text = stateHandle.get<String>("text") ?: ""
}

