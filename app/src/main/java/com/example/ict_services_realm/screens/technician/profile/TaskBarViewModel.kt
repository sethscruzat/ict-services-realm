package com.example.ict_services_realm.screens.technician.profile

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ict_services_realm.repository.TechSyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

sealed class TaskBarEvent {
    object LogOut : TaskBarEvent()
    class Info(val message: String) : TaskBarEvent()
    class Error(val message: String, val throwable: Throwable) : TaskBarEvent()
}

class TaskBarViewModel(private val repository: TechSyncRepository
) : ViewModel() {

    private val _offlineMode: MutableState<Boolean> = mutableStateOf(false)
    val offlineMode: State<Boolean>
        get() = _offlineMode

    private val _toolbarEvent: MutableSharedFlow<TaskBarEvent> = MutableSharedFlow()
    val toolbarEvent: Flow<TaskBarEvent>
        get() = _toolbarEvent

    fun goOffline() {
        _offlineMode.value = true
        repository.pauseSync()
    }

    fun goOnline() {
        _offlineMode.value = false
        repository.resumeSync()
    }

    fun logOut() {
        viewModelScope.launch {
            _toolbarEvent.emit(TaskBarEvent.LogOut)
            repository.close()
        }
    }

    fun error(errorEvent: TaskBarEvent.Error) {
        viewModelScope.launch {
            _toolbarEvent.emit(errorEvent)
        }
    }

    companion object {
        fun factory(
            repository: TechSyncRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return TaskBarViewModel(repository) as T
                }
            }
        }
    }
}
