package com.example.ict_services_realm.screens.technician.profile

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ict_services_realm.models.user
import com.example.ict_services_realm.repository.TechSyncRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object TaskViewEvent

class ProfileViewModel(private val repository: TechSyncRepository) : ViewModel() {

    private val _event: MutableSharedFlow<TaskViewEvent> = MutableSharedFlow()
    val event: Flow<TaskViewEvent>
        get() = _event

    private val _userState: MutableState<user?> = mutableStateOf(null)
    val userState: MutableState<user?>
        get() = _userState

        init {
            viewModelScope.launch {
                repository.getUser().collect{event ->
                    when (event){
                        is InitialResults ->{
                            userState.value = event.list[0]
                        }
                        is UpdatedResults -> Unit
                    }

                }
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
                    return ProfileViewModel(repository) as T
                }
            }
        }
    }
}
