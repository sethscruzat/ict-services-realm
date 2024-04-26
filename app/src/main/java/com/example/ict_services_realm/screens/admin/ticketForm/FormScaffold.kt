package com.example.ict_services_realm.screens.admin.ticketForm

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.ict_services_realm.app
import com.example.ict_services_realm.models.ticket
import com.example.ict_services_realm.models.user
import com.example.ict_services_realm.navigation.NavRoutes
import com.example.ict_services_realm.repository.AdminSyncRepository
import com.example.ict_services_realm.ui.theme.Purple80
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val USABLE_WIDTH = 0.8F
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun FormScaffold(modifier: Modifier = Modifier, formViewModel: FormViewModel, taskBarViewModelAdmin: TaskBarViewModelAdmin,techList: SnapshotStateList<user>){
    Scaffold(
        content = {
            Column {
                // Title
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        modifier = modifier.padding(12.dp).align(Alignment.TopEnd),
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
                }

            // Email, password, login/create account button and switch action
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.Start
                ) {
                    // equipmentID field
                    TextField(
                        modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                        value = formViewModel.state.value.equipmentID,
                        maxLines = 1,
                        onValueChange = {
                            formViewModel.setEquipmentID(it)
                        },
                        label = { Text("EquipmentID") }
                    )

                    // Password field
                    TextField(
                        modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                        value = formViewModel.state.value.location,
                        maxLines = 1,
                        onValueChange = {
                            formViewModel.setLocation(it)
                        },
                        label = { Text("Location") })

                    TextField(
                        modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                        value = formViewModel.state.value.remarks,
                        maxLines = 5,
                        onValueChange = {
                            formViewModel.setRemarks(it)
                        },
                        label = { Text("Remarks") })

                    var isExpanded by remember { mutableStateOf(false) }
                    var assignedToName by remember { mutableStateOf("") }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = isExpanded,
                            onExpandedChange = { isExpanded = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 12.dp, top = 24.dp, end = 12.dp, bottom = 6.dp)
                        ) {
                            TextField(
                                value = assignedToName, onValueChange = {}, readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = isExpanded
                                    )
                                },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                                modifier = Modifier.menuAnchor()
                            )
                            DropdownMenu(
                                expanded = isExpanded,
                                onDismissRequest = { isExpanded = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                techList.forEach { item ->
                                    val name = "${item.firstName} ${item.lastName}"
                                    DropdownMenuItem(
                                        text = { Text(text = name) },
                                        onClick = {
                                            assignedToName = name
                                            formViewModel.setAssignedTo(item.user_id)
                                            isExpanded = false
                                        })
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                    val ctx = LocalContext.current
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Purple80),
                        modifier = Modifier.fillMaxWidth(USABLE_WIDTH),
                        onClick = {
                            val ticketToAdd = ticket()
                            ticketToAdd.assignedTo = formViewModel.state.value.assignedTo
                            ticketToAdd.equipmentID = formViewModel.state.value.equipmentID
                            ticketToAdd.remarks = formViewModel.state.value.remarks
                            ticketToAdd.location = formViewModel.state.value.location
                            ticketToAdd.issuedBy = app.currentUser?.id.toString()
                            when(val validationResult = formViewModel.validateTicketForm(ticketToAdd)){
                                is FormViewModel.FormValidationResult.Valid -> formViewModel.addTicket(ticketToAdd)
                                is FormViewModel.FormValidationResult.Invalid -> {
                                    Toast.makeText(ctx, validationResult.errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                            formViewModel.setEquipmentID("")
                            formViewModel.setRemarks("")
                            formViewModel.setLocation("")
                            formViewModel.setAssignedTo("")
                            assignedToName = ""
                        }) {
                        Text("Submit")
                    }
                }
            }
        }
    )
}


@Composable
fun AdminNavGraph(modifier: Modifier = Modifier,
                  repository: AdminSyncRepository,
                  navController: NavHostController,
                  startDestination: String = NavRoutes.AdminTicketsForm.screenroute,
                  formViewModel: FormViewModel,
                  taskBarViewModelAdmin: TaskBarViewModelAdmin,
                  techList: SnapshotStateList<user>,
)
{
    NavHost(modifier = modifier,navController = navController, startDestination = startDestination) {
        // ADMIN ROUTES
        composable(NavRoutes.AdminTicketsForm.screenroute) {
            FormScaffold(formViewModel = formViewModel, taskBarViewModelAdmin = taskBarViewModelAdmin,techList = techList)
        }
        composable(NavRoutes.AdminTicketsList.screenroute) {}
        composable(NavRoutes.AdminRate.screenroute) {}
    }
}