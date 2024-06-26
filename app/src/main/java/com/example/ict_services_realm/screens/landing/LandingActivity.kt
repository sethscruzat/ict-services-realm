package com.example.ict_services_realm.screens.landing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.ict_services_realm.repository.RealmSyncRepositoryTech
import com.example.ict_services_realm.screens.admin.ticketForm.FormActivity
import com.example.ict_services_realm.screens.technician.profile.LoadingIndicator
import com.example.ict_services_realm.screens.technician.profile.ProfileActivity
import com.example.ict_services_realm.ui.theme.IctservicesrealmTheme
import io.realm.kotlin.notifications.InitialResults
import io.realm.kotlin.notifications.UpdatedResults
import kotlinx.coroutines.launch

class LandingActivity : ComponentActivity() {
    private val repository = RealmSyncRepositoryTech{ _, error ->
        // Sync errors come from a background thread so route the Toast through the UI thread
        lifecycleScope.launch {
            // Catch write permission errors and notify user. This is just a 2nd line of defense
            // since we prevent users from modifying someone else's tasks
            // TODO the SDK does not have an enum for this type of error yet so make sure to update this once it has been added
            if (error.message?.contains("CompensatingWrite") == true) {
                Toast.makeText(this@LandingActivity, "You Do Not Have Permission.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repository.updateChanges()
            repository.getUser().collect{ event->
                when(event){
                    is InitialResults ->{
                        val userRes = event.list
                        if(userRes[0].role=="admin"){
                            val intent = Intent(this@LandingActivity, FormActivity::class.java)
                            startActivity(intent)
                            repository.close()
                            finish()
                        }else if(userRes[0].role=="technician"){
                            val intent = Intent(this@LandingActivity, ProfileActivity::class.java)
                            startActivity(intent)
                            repository.close()
                            finish()
                        }
                    }
                    is UpdatedResults -> Unit
                }
            }
        }

        setContent {
            IctservicesrealmTheme {
                LoadingIndicator()
            }
        }
    }
}
