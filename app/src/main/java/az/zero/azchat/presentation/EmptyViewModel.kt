package az.zero.azchat.presentation

import androidx.lifecycle.ViewModel
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmptyViewModel @Inject constructor(
    private val repository: MainRepositoryImpl
) : ViewModel() {

}

