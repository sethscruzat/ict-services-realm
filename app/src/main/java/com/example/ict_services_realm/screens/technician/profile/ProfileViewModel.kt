package com.example.ict_services_realm.screens.technician.profile

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.models.user
import com.example.ict_services_realm.repository.TechSyncRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

object TaskViewEvent

class ProfileViewModel(
    private val repository: TechSyncRepository,
    val compTicketListState: SnapshotStateList<ticket> = mutableStateListOf()
) : ViewModel() {

    private val _event: MutableSharedFlow<TaskViewEvent> = MutableSharedFlow()
    val event: Flow<TaskViewEvent>
        get() = _event

    private val _userState: MutableState<user?> = mutableStateOf(null)
    val userState: MutableState<user?>
        get() = _userState

    private val _adminName: MutableState<String> = mutableStateOf("")
    val adminName: MutableState<String>
        get() = _adminName

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

            viewModelScope.launch {
                repository.getCompTicketList().collect{event ->
                    when(event){
                        is InitialResults ->{
                            compTicketListState.clear()
                            compTicketListState.addAll(event.list)
                        }
                        is UpdatedResults -> {
                            if (event.deletions.isNotEmpty() && compTicketListState.isNotEmpty()) {
                                event.deletions.reversed().forEach {
                                    compTicketListState.removeAt(it)
                                }
                            }
                            if (event.insertions.isNotEmpty()) {
                                event.insertions.forEach {
                                    compTicketListState.add(it, event.list[it])
                                }
                            }
                            if (event.changes.isNotEmpty()) {
                                event.changes.forEach {
                                    compTicketListState.removeAt(it)
                                    compTicketListState.add(it, event.list[it])
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }

    fun getAdminName(userId: String){
        viewModelScope.launch {
            repository.getAdminName(userId).collect{
                adminName.value = it.list[0].firstName.toString() + it.list[0].lastName.toString()

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
