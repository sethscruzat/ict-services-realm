package com.example.ict_services_realm.screens.admin.completedTickets

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.repository.AdminSyncRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch

class CompletedTicketViewModel(private val repository: AdminSyncRepository,
                               val compTicketListState: SnapshotStateList<ticket> = mutableStateListOf()
) : ViewModel(){
    init{
        viewModelScope.launch {
            repository.getCompletedTickets().collect{event ->
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

    companion object {
        fun factory(
            repository: AdminSyncRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return CompletedTicketViewModel(repository) as T
                }
            }
        }
    }

}