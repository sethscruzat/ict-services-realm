package com.example.ict_services_realm.screens.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.ict_services_realm.TAG
import com.example.ict_services_realm.app
import com.example.ict_services_realm.screens.landing.LandingActivity
import com.example.ict_services_realm.ui.theme.IctservicesrealmTheme
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Fast-track task list screen if we are logged in
        if (app.currentUser != null) {
            startActivity(Intent(this, LandingActivity::class.java))
            finish()
            return
        }

        lifecycleScope.launch {
            // Subscribe to navigation and message-logging events
            loginViewModel.event
                .collect { event ->
                    when (event) {
                        is LoginEvent.GoToLanding -> {
                            event.process()

                            val intent = Intent(this@LoginActivity, LandingActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        is LoginEvent.ShowMessage -> event.process()
                    }
                }
        }

        setContent {
            IctservicesrealmTheme {
                LoginScaffold(loginViewModel)
            }
        }
    }

    private fun LoginEvent.process() {
        when (severity) {
            EventSeverity.INFO -> Log.i(TAG(), message)
            EventSeverity.ERROR -> {
                Log.e(TAG(), message)
                Toast.makeText(this@LoginActivity, message, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
