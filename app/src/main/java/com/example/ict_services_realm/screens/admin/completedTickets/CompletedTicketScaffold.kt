package com.example.ict_services_realm.screens.admin.completedTickets

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ict_services_realm.app
import com.example.ict_services_realm.screens.admin.ticketForm.AdminBottomNavigation
import com.example.ict_services_realm.screens.admin.ticketForm.TaskBarEvent
import com.example.ict_services_realm.screens.admin.ticketForm.TaskBarViewModelAdmin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun CompletedTicketScaffold(modifier: Modifier = Modifier, navController: NavHostController, completedTicketViewModel: CompletedTicketViewModel, taskBarViewModelAdmin: TaskBarViewModelAdmin) {
    Scaffold(
        bottomBar = { AdminBottomNavigation(navController = navController) },
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
                                taskBarViewModelAdmin.logOut()
                            }.onFailure {
                                taskBarViewModelAdmin.error(TaskBarEvent.Error("Log out failed", it))
                            }
                        }
                    }
                )
                {
                    Text("Logout")
                }
                Spacer(modifier.weight(0.1f))
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                )
                {
                    val taskList = completedTicketViewModel.compTicketListState
                    items(taskList) {item ->
                        item.ticketID?.let { it1 -> CompletedTicketItem(
                            navController = navController,
                            equipmentID = item.equipmentID,
                            ticketID = it1) }
                        Divider()
                    }
                }
            }
        }
    )
}


@Composable
fun CompletedTicketItem(modifier: Modifier = Modifier, navController: NavHostController,
                   equipmentID: String, ticketID: Int) {
    Row(modifier = modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(modifier = modifier
            .weight(1f)
            .padding(horizontal = 9.dp),text = equipmentID, fontSize = 18.sp)
        IconButton(onClick = {
            navController.navigate("adminRate/{ticketID}"
                .replace(oldValue = "{ticketID}", newValue = ticketID.toString()))
        }) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = "Open", modifier = modifier.size(16.dp))
        }
    }
}