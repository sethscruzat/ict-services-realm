package com.example.ict_services_realm.screens.technician.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.example.ict_services_realm.repository.RealmSyncRepository
import com.example.ict_services_realm.screens.login.LoginActivity
import com.example.ict_services_realm.screens.technician.ticketList.TicketListViewModel
import com.example.ict_services_realm.ui.theme.IctservicesrealmTheme
import kotlinx.coroutines.launch

class ProfileActivity: ComponentActivity() {
    private val repository = RealmSyncRepository{ _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
                Toast.makeText(this@ProfileActivity, "You Do Not Have Permission.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val profileViewModel: ProfileViewModel by viewModels {
        ProfileViewModel.factory(repository, this)
    }

    private val taskBarViewModel: TaskBarViewModel by viewModels {
        TaskBarViewModel.factory(repository, this)
    }

    private val ticketListViewModel: TicketListViewModel by viewModels {
        TicketListViewModel.factory(repository, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            profileViewModel.event.collect{
                Log.i("Error", "Tried to modify or remove a task that doesn't belong to the current user.")
                Toast.makeText(
                    this@ProfileActivity,
                    "Warning",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        lifecycleScope.launch {
            taskBarViewModel.toolbarEvent
                .collect { taskBarEvent ->
                    when (taskBarEvent) {
                        TaskBarEvent.LogOut -> {
                            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                            finish()
                        }
                        is TaskBarEvent.Info ->
                            Log.e("INFO", taskBarEvent.message)
                        is TaskBarEvent.Error ->
                            Log.e("ERROR", "${taskBarEvent.message}: ${taskBarEvent.throwable.message}")
                    }
                }
        }

        setContent {
            IctservicesrealmTheme {
                val navController = rememberNavController()
                TechNavGraph(navController = navController,
                    repository = repository,
                    profileViewModel = profileViewModel,
                    taskBarViewModel = taskBarViewModel,
                    ticketListViewModel = ticketListViewModel)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        repository.close()
    }
}