package com.example.ict_services_realm.screens.technician.ticketInfo

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.repository.TechSyncRepository
import com.example.ict_services_realm.screens.admin.rateTech.RateTechViewModel
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch

class TicketInfoViewModel(
    private val repository: TechSyncRepository,
    ticketID: Int
): ViewModel(){

    private val _ticketInfoState: MutableState<ticket?> = mutableStateOf(null)
    val ticketInfoState: MutableState<ticket?>
        get() = _ticketInfoState


    init {
        viewModelScope.launch {
            repository.getTicketInfo(ticketID).collect{event ->
                when(event){
                    is InitialResults ->{
                        event.runCatching {
                            _ticketInfoState.value = this.list[0]
                        }.onSuccess {
                            Log.i("INFO", "Ticket loaded")
                        }.onFailure {
                            Log.i("Error", "Ticket failed to load")
                        }
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

class TicketInfoViewModelFactory(
    private val repository: TechSyncRepository,
    private val ticketId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TicketInfoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TicketInfoViewModel(repository, ticketId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}