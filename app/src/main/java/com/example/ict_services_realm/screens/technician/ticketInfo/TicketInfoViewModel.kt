package com.example.ict_services_realm.screens.technician.ticketInfo

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.repository.SyncRepository
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch


class TicketInfoViewModel(
    private val repository: SyncRepository, ticketID: Int
): ViewModel(){

    private val _ticketInfoState: MutableState<ticket?> = mutableStateOf(null)
    val ticketInfoState: MutableState<ticket?>
        get() = _ticketInfoState

    init {
        viewModelScope.launch {
            repository.getTicketInfo(ticketID).collect{event ->
                when(event){
                    is InitialResults ->{
                        _ticketInfoState.value = event.list[0]
                    }
                    is UpdatedResults -> Unit
                }
            }
        }
    }

    fun markTicketAsDone(ticketID: Int){
        viewModelScope.launch {
            repository.markAsDone(ticketID)
        }

    }
}