package az.zero.azchat.presentation.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import az.zero.azchat.data.models.user.User
import az.zero.azchat.repository.MainRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    private val repositoryImpl: MainRepositoryImpl
) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User> = _user

    private fun getUserInfo() {
        viewModelScope.launch {
            repositoryImpl.getUserInfo { _user.postValue(it) }
        }
    }

    init {
        getUserInfo()
    }
}

