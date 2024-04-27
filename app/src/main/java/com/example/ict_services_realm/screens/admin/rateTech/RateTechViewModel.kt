package com.example.ict_services_realm.screens.admin.rateTech

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.models.user_remarks
import com.example.ict_services_realm.repository.AdminSyncRepository
import io.realm.kotlin.mongodb.exceptions.ConnectionException
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InvalidObjectException

data class RemarkState(
    val rating: Double = 0.0,
    val comment: String = "",
    val ticketID: Int = 0,
    val ratedBy: String = ""
){
    companion object {
        /**
         * Initial UI state of the login screen.
         */
        val initialState = RemarkState()
    }
}

class RateTechViewModel(
    private val repository: AdminSyncRepository, ticketID: Int
): ViewModel(){

    private val _ticketInfoState: MutableState<ticket?> = mutableStateOf(null)
    val ticketInfoState: MutableState<ticket?>
        get() = _ticketInfoState

    private val _state: MutableState<RemarkState> = mutableStateOf(RemarkState.initialState)
    val state: State<RemarkState>
        get() = _state

    fun setRating(rating: Double) {
        _state.value = state.value.copy(rating = rating)
    }

    fun setComment(comment: String) {
        _state.value = state.value.copy(comment = comment)
    }



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

    fun addRating(userRemarks: user_remarks, techID: String){
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                repository.addRemark(userRemarks, techID)
            }.onSuccess {
                Log.d("SUCCESS", "Rating has been pushed")
            }.onFailure { ex: Throwable ->
                val message = when (ex) {
                    is InvalidObjectException -> "Form details Invalid."
                    is ConnectionException -> "Could not connect to the authentication provider. Check your internet connection and try again."
                    else -> "Error: $ex"
                }
                Log.d("ERROR", message)
            }
        }
    }

    fun validateRemarkForm(userRemarks: user_remarks): FormValidationResult {
        if(userRemarks.comment!!.isBlank()){
            return FormValidationResult.Invalid("No comments inputted!")
        }
        if(userRemarks.rating!!.equals(0.0)){
            return FormValidationResult.Invalid("Rating should be at least 1.0!")
        }
        return FormValidationResult.Valid
    }

    sealed class FormValidationResult {
        data object Valid : FormValidationResult()
        data class Invalid(val errorMessage: String) : FormValidationResult()
    }

}