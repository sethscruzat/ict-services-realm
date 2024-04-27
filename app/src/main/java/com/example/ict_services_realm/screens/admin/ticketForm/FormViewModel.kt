package com.example.ict_services_realm.screens.admin.ticketForm

import android.os.Bundle
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import com.example.ict_services_realm.repository.AdminSyncRepository
import com.example.ict_services_realm.screens.login.EventSeverity
import io.realm.kotlin.mongodb.exceptions.ConnectionException
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.InvalidObjectException

data class FormState(
    val equipmentID: String = "",
    val location: String = "",
    val remarks: String = "",
    val assignedTo: String = ""
){
    companion object {
        /**
         * Initial UI state of the login screen.
         */
        val initialState = FormState()
    }
}

sealed class FormEvent(val severity: EventSeverity, val message: String) {
    class ShowMessage(severity: EventSeverity, message: String) : FormEvent(severity, message)
    class ShowToast(severity: EventSeverity, message: String) : FormEvent(severity, message)
}

class FormViewModel(private val repository: AdminSyncRepository,
                    val techList: SnapshotStateList<user> = mutableStateListOf()) : ViewModel(){
    private val _state: MutableState<FormState> = mutableStateOf(FormState.initialState)
    val state: State<FormState>
        get() = _state

    private val _event: MutableSharedFlow<FormEvent> = MutableSharedFlow()
    val event: Flow<FormEvent>
        get() = _event

    fun setEquipmentID(equipmentID: String) {
        _state.value = state.value.copy(equipmentID = equipmentID)
    }

    fun setLocation(location: String) {
        _state.value = state.value.copy(location = location)
    }

    fun setRemarks(remarks: String) {
        _state.value = state.value.copy(remarks = remarks)
    }

    fun setAssignedTo(assignedTo: String) {
        _state.value = state.value.copy(assignedTo = assignedTo)
    }

    init{
        viewModelScope.launch {
            repository.getTechList().collect{event ->
                when(event){
                    is InitialResults ->{
                        techList.clear()
                        techList.addAll(event.list)
                    }
                    is UpdatedResults -> {
                        if (event.deletions.isNotEmpty() && techList.isNotEmpty()) {
                            event.deletions.reversed().forEach {
                                techList.removeAt(it)
                            }
                        }
                        if (event.insertions.isNotEmpty()) {
                            event.insertions.forEach {
                                techList.add(it, event.list[it])
                            }
                        }
                        if (event.changes.isNotEmpty()) {
                            event.changes.forEach {
                                techList.removeAt(it)
                                techList.add(it, event.list[it])
                            }
                        }
                    }
                    else -> Unit
                }

            }
        }
    }

    fun addTicket(ticket: ticket){
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addTicket(ticket)
            }.onSuccess {
                _event.emit(FormEvent.ShowToast(EventSeverity.INFO, "Ticket Added"))
                _event.emit(FormEvent.ShowMessage(EventSeverity.INFO, "Ticket Successfully created."))
            }.onFailure { ex: Throwable ->
                val message = when (ex) {
                    is InvalidObjectException -> "Form details Invalid."
                    is ConnectionException -> "Could not connect to the authentication provider. Check your internet connection and try again."
                    else -> "Error: $ex"
                }
                _event.emit(FormEvent.ShowMessage(EventSeverity.ERROR, message))
            }
        }
    }

    fun validateTicketForm(ticket: ticket): FormValidationResult{
        if(ticket.equipmentID.isBlank()){
            return FormValidationResult.Invalid("No ID for equipment Inputted!")
        }
        if(ticket.location!!.isBlank()){
            return FormValidationResult.Invalid("Location cannot be empty")
        }
        return FormValidationResult.Valid
    }

    sealed class FormValidationResult {
        data object Valid : FormValidationResult()
        data class Invalid(val errorMessage: String) : FormValidationResult()
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
                    return FormViewModel(repository) as T
                }
            }
        }
    }
}