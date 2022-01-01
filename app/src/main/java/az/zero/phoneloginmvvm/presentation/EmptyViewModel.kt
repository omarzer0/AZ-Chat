package az.zero.phoneloginmvvm.presentation

import android.app.Activity
import androidx.lifecycle.ViewModel
import az.zero.phoneloginmvvm.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EmptyViewModel @Inject constructor(
    private val repository: MainRepositoryImpl
) : ViewModel() {

}

