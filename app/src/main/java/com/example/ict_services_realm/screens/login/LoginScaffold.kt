package com.example.ict_services_realm.screens.login

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ict_services_realm.ui.theme.Purple80

private const val USABLE_WIDTH = 0.8F

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun LoginScaffold(loginViewModel: LoginViewModel) {
    Scaffold(
        content = {
            Column {
                // Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight(0.25f)
                        .fillMaxWidth()
                ) {
                }

                // Email, password, login/create account button and switch action
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier
                        .fillMaxHeight(0.75f)
                        .fillMaxWidth()
                ) {
                    Column {
                        // Email field
                        TextField(
                            enabled = loginViewModel.state.value.enabled,
                            modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                            value = loginViewModel.state.value.email,
                            maxLines = 2,
                            onValueChange = {
                                loginViewModel.setEmail(it)
                            },
                            label = { Text("Email") }
                        )

                        // Password field
                        TextField(
                            enabled = loginViewModel.state.value.enabled,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                            value = loginViewModel.state.value.password,
                            maxLines = 2,
                            onValueChange = {
                                loginViewModel.setPassword(it)
                            },
                            label = { Text("Password") })

                        Spacer(modifier = Modifier.height(40.dp))

                        // Login/create account button
                        Button(
                            enabled = loginViewModel.state.value.enabled,
                            colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                            modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                            onClick = {
                                val state = loginViewModel.state.value
                                when (state.action) {
                                    LoginAction.LOGIN -> loginViewModel.login(
                                        state.email,
                                        state.password
                                    )
                                    LoginAction.CREATE_ACCOUNT -> loginViewModel.createAccount(
                                        state.email,
                                        state.password
                                    )
                                }
                            }) {
                            val actionText = when (loginViewModel.state.value.action) {
                                LoginAction.CREATE_ACCOUNT -> "Create Account"
                                LoginAction.LOGIN -> "Log In"
                            }
                            Text(actionText)
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Switch between login and create user
                        TextButton(
                            onClick = {
                                val state = loginViewModel.state.value
                                when (state.action) {
                                    LoginAction.LOGIN -> loginViewModel.switchToAction(LoginAction.CREATE_ACCOUNT)
                                    LoginAction.CREATE_ACCOUNT -> loginViewModel.switchToAction(LoginAction.LOGIN)
                                }
                            }
                        ) {
                            val actionText = when (loginViewModel.state.value.action) {
                                LoginAction.CREATE_ACCOUNT -> "Already have an account? Log in"
                                LoginAction.LOGIN -> "Don\'t have an account? Create account"
                            }
                            Text(
                                text = actionText,
                                modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                                textAlign = TextAlign.Center,
                                color = Blue
                            )
                        }

                        // Text with clarification on Atlas Cloud account vs Device Sync account
                        Text(
                            text = "temp",
                            //text = stringResource(R.string.account_clarification),
                            modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    )
}