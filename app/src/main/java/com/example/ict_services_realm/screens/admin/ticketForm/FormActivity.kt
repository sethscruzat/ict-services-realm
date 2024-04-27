package com.example.ict_services_realm.screens.admin.ticketForm

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.ict_services_realm.TAG
import com.example.ict_services_realm.repository.RealmSyncRepositoryAdmin
import com.example.ict_services_realm.screens.admin.completedTickets.CompletedTicketViewModel
import com.example.ict_services_realm.screens.login.EventSeverity
import com.example.ict_services_realm.screens.login.LoginActivity
import com.example.ict_services_realm.ui.theme.IctservicesrealmTheme
import kotlinx.coroutines.launch

class FormActivity: ComponentActivity() {
    private val repository = RealmSyncRepositoryAdmin{ _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
                Toast.makeText(this@FormActivity, "You Do Not Have Permission.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    private val taskBarViewModelAdmin: TaskBarViewModelAdmin by viewModels {
        TaskBarViewModelAdmin.factory(repository, this)
    }

    private val formViewModel: FormViewModel by viewModels {
        FormViewModel.factory(repository, this)
    }

    private val completedTicketViewModel: CompletedTicketViewModel by viewModels {
        CompletedTicketViewModel.factory(repository,this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            taskBarViewModelAdmin.toolbarEvent
                .collect { taskBarEvent ->
                    when (taskBarEvent) {
                        TaskBarEvent.LogOut -> {
                            repository.close()
                            startActivity(Intent(this@FormActivity, LoginActivity::class.java))
                            finish()
                        }
                        is TaskBarEvent.Info ->
                            Log.e("INFO", taskBarEvent.message)
                        is TaskBarEvent.Error ->
                            Log.e("ERROR", "${taskBarEvent.message}: ${taskBarEvent.throwable.message}")
                    }
                }
        }

        lifecycleScope.launch {
            // Subscribe to navigation and message-logging events
            formViewModel.event
                .collect { event ->
                    when (event) {
                        is FormEvent.ShowMessage -> event.process()
                        is FormEvent.ShowToast -> Toast.makeText(this@FormActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
        }

        setContent {
            IctservicesrealmTheme {
                val navController = rememberNavController()
                AdminNavGraph(
                    repository = repository,
                    navController = navController,
                    formViewModel = formViewModel,
                    taskBarViewModelAdmin = taskBarViewModelAdmin,
                    completedTicketViewModel = completedTicketViewModel,
                )
            }
        }
    }

    private fun FormEvent.process() {
        when (severity) {
            EventSeverity.INFO -> Log.i(TAG(), message)
            EventSeverity.ERROR -> {
                Log.e(TAG(), message)
                Toast.makeText(this@FormActivity, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}