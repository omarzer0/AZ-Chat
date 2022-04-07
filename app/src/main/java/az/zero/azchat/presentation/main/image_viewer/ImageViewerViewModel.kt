package az.zero.azchat.presentation.main.image_viewer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageViewerViewModel @Inject constructor(
    private val stateHandle: SavedStateHandle
) : ViewModel() {
    val image = stateHandle.get<String>("image") ?: ""
}

