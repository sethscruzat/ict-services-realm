package com.example.ict_services_realm.screens.technician.ticketList

import android.os.Bundle
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.repository.SyncRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch

class TicketListViewModel(
    private val repository: SyncRepository,
    val ticketListState: SnapshotStateList<ticket> = mutableStateListOf()
): ViewModel(){

    init {
        viewModelScope.launch {
            repository.getTicketList().collect{event ->
                when(event){
                    is InitialResults ->{
                        ticketListState.clear()
                        ticketListState.addAll(event.list)
                    }
                    is UpdatedResults -> {
                        if (event.deletions.isNotEmpty() && ticketListState.isNotEmpty()) {
                            event.deletions.reversed().forEach {
                                ticketListState.removeAt(it)
                            }
                        }
                        if (event.insertions.isNotEmpty()) {
                            event.insertions.forEach {
                                ticketListState.add(it, event.list[it])
                            }
                        }
                        if (event.changes.isNotEmpty()) {
                            event.changes.forEach {
                                ticketListState.removeAt(it)
                                ticketListState.add(it, event.list[it])
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
            repository: SyncRepository,
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle? = null
        ): AbstractSavedStateViewModelFactory {
            return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
                override fun <T : ViewModel> create(
                    key: String,
                    modelClass: Class<T>,
                    handle: SavedStateHandle
                ): T {
                    return TicketListViewModel(repository) as T
                }
            }
        }
    }
}