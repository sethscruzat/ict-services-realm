package com.example.ict_services_realm.screens.technician.ticketList

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
import com.example.ict_services_realm.screens.technician.profile.TaskBarEvent
import com.example.ict_services_realm.screens.technician.profile.TaskBarViewModel
import com.example.ict_services_realm.screens.technician.profile.TechBottomNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun TicketListScaffold(modifier: Modifier = Modifier, navController: NavHostController, ticketListViewModel: TicketListViewModel, taskBarViewModel: TaskBarViewModel) {
    Scaffold(
        bottomBar = { TechBottomNavigation(navController = navController) },
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
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1F)
                )
                {
                    val taskList = ticketListViewModel.ticketListState
                    items(taskList) {item ->
                        item.ticketID?.let { it1 -> TicketListItem(
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
fun TicketListItem(modifier: Modifier = Modifier, navController: NavHostController,
                           equipmentID: String, ticketID: Int) {
    Row(modifier = modifier
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(modifier = modifier
            .weight(1f)
            .padding(horizontal = 9.dp),text = equipmentID, fontSize = 18.sp)
        IconButton(onClick = {
            navController.navigate("techTicketInfo/{ticketID}"
                .replace(oldValue = "{ticketID}", newValue = ticketID.toString())) {
                launchSingleTop = true
                restoreState = false
            }
        }) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = "Open", modifier = modifier.size(16.dp))
        }
    }
}