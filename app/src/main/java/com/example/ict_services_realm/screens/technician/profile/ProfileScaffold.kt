package com.example.ict_services_realm.screens.technician.profile

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ict_services_realm.app
import com.example.ict_services_realm.screens.landing.LandingActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun ProfileScaffold(modifier: Modifier = Modifier, profileViewModel: ProfileViewModel, taskBarViewModel: TaskBarViewModel) {
    val userData = profileViewModel.userState.value
    val name ="${userData?.firstName} ${userData?.lastName}"
    Scaffold(
        content = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Button(
                    modifier = modifier
                        .padding(12.dp)
                        .align(Alignment.End),
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                app.currentUser?.logOut()
                            }.onSuccess {
                                taskBarViewModel.logOut()
                            }.onFailure {
                                taskBarViewModel.error(TaskBarEvent.Error("Log out failed", it))
                            }
                        }
                    }
                )
                {
                    Text("Logout")
                }
                Spacer(modifier.weight(0.1f))
                Column(modifier = modifier.weight(2f)){
                    Text(text = "Name: $name", modifier = modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 14.dp
                        ),
                        fontSize = 17.sp)
                }
            }
        }
    )
}