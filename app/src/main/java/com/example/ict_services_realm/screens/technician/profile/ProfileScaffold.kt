package com.example.ict_services_realm.screens.technician.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForwardIos
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ict_services_realm.app
import com.example.ict_services_realm.models.user_remarks
import com.example.ict_services_realm.navigation.NavRoutes
import com.example.ict_services_realm.repository.TechSyncRepository
import com.example.ict_services_realm.screens.technician.TechnicianNavItem
import com.example.ict_services_realm.screens.technician.ticketInfo.TicketInfoScaffold
import com.example.ict_services_realm.screens.technician.ticketInfo.TicketInfoViewModel
import com.example.ict_services_realm.screens.technician.ticketInfo.TicketInfoViewModelFactory
import com.example.ict_services_realm.screens.technician.ticketList.TicketListItem
import com.example.ict_services_realm.screens.technician.ticketList.TicketListScaffold
import com.example.ict_services_realm.screens.technician.ticketList.TicketListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun ProfileScaffold(modifier: Modifier = Modifier, navController: NavHostController, profileViewModel: ProfileViewModel, taskBarViewModel: TaskBarViewModel) {
    val userData = profileViewModel.userState.value
    val name ="${userData?.firstName} ${userData?.lastName}"
    Scaffold(
        bottomBar = { TechBottomNavigation(navController = navController)},
        content = {
           Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState(), true)
           )
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
                // name + pfp ideally
                Box(modifier= modifier
                    .weight(30f)
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
                    .fillMaxWidth(0.6f)
                    .border(border = BorderStroke(3.dp, Color.Black), shape = RectangleShape)
                ){
                    Text(text = "Name: $name", modifier = modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 14.dp
                        )
                        .align(Alignment.BottomCenter),
                        fontSize = 17.sp)
                }
                //recently completed tasks
                val completeTicketList = profileViewModel.compTicketListState
                LazyColumn(
                    state = rememberLazyListState(),
                    modifier = modifier
                        .weight(30f)
                        .align(Alignment.CenterHorizontally)
                        .padding(5.dp)
                        .fillMaxWidth(0.9f)
                        .border(border = BorderStroke(3.dp, Color.Black), shape = RectangleShape)
                ){
                    items(completeTicketList) {item ->
                        item.ticketID?.let { it1 -> TicketListItem(
                            navController = navController,
                            equipmentID = item.equipmentID,
                            ticketID = it1) }
                        Divider(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(3.dp),
                            thickness = 3.dp
                        )
                    }
                }

                // remarks box
               // TODO: PAAYOS RIN NITO, may nakatago sa likod ng bottom nav bar
               val remarksList = profileViewModel.userState.value?.remarks?.toList()
               LazyColumn(
                   state = rememberLazyListState(),
                   modifier = modifier
                       .weight(30f)
                       .align(Alignment.CenterHorizontally)
                       .padding(5.dp)
                       .fillMaxWidth(0.9f)
                       .border(border = BorderStroke(3.dp, Color.Black), shape = RectangleShape)
               ){
                   if(remarksList!=null){
                       items(remarksList) {item ->
                           RemarkListItem(remark = item, profileViewModel = profileViewModel)
                           Divider(
                               modifier = modifier
                                   .fillMaxWidth()
                                   .padding(3.dp),
                               thickness = 3.dp
                           )
                       }
                   }
               }
            }
        }
    )
}

@Composable
fun TechNavGraph(modifier: Modifier = Modifier,
                 repository: TechSyncRepository,
                 navController: NavHostController,
                 startDestination: String = NavRoutes.TechnicianProfile.screenroute,
                 profileViewModel: ProfileViewModel,
                 taskBarViewModel: TaskBarViewModel,
                 ticketListViewModel: TicketListViewModel)
{
    val scaffoldLoaded = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        scaffoldLoaded.value = true
    }
    if(scaffoldLoaded.value){
        NavHost(modifier = modifier,navController = navController, startDestination = startDestination) {
            // TECHNICIAN ROUTES
            composable(NavRoutes.TechnicianProfile.screenroute) {
                ProfileScaffold(navController = navController, profileViewModel = profileViewModel, taskBarViewModel = taskBarViewModel)
            }
            composable(NavRoutes.TechnicianTickets.screenroute) {
                TicketListScaffold(navController = navController, ticketListViewModel = ticketListViewModel, taskBarViewModel = taskBarViewModel)
            }
            composable("${NavRoutes.TechnicianTicketInfo.screenroute}/{ticketID}") { navBackStackEntry ->
                val ticketID = navBackStackEntry.arguments?.getString("ticketID")!!.toInt()
                val ticketInfoViewModel: TicketInfoViewModel = viewModel(
                    factory = TicketInfoViewModelFactory(repository, ticketID)
                )
                TicketInfoScaffold(navController = navController, ticketInfo = ticketInfoViewModel)
            }
        }
    } else{
        LoadingIndicator()
    }
}
@Composable
fun LoadingIndicator() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}



@Composable
fun RemarkListItem(modifier: Modifier = Modifier, remark: user_remarks, profileViewModel: ProfileViewModel) {
    var showDialog by remember { mutableStateOf(false) }
    Row(modifier = modifier
        .fillMaxWidth()
        .height(65.dp)
        .padding(top = 9.dp)
    ) {
        Column(
            modifier = modifier.weight(15f),
            horizontalAlignment = Alignment.Start
        ) {
            remark.rating?.let {
                Text(
                    modifier = modifier
                        .padding(
                            start = 12.dp,
                            top = 6.dp,
                            end = 12.dp
                        ),
                    text = it.toString(),
                    fontSize = 21.sp
                )
            }
            remark.comment?.let {
                /*profileViewModel.getAdminName(userId = it)
                val adminName = profileViewModel.adminName.value*/
                Text(
                    modifier = modifier
                        .padding(horizontal = 12.dp),
                    text = it,
                    fontSize = 15.sp
                )
            }
        }
        IconButton(onClick = {
            showDialog = true
        }) {
            Icon(Icons.AutoMirrored.Outlined.ArrowForwardIos, contentDescription = "Open", modifier = modifier.size(16.dp))
        }
    }

    if (showDialog) {
        Dialog(
            onDismissRequest = { showDialog = false },
            content = {
                Box(
                    modifier = modifier
                        .background(Color.LightGray, RectangleShape)
                        .height(225.dp)
                ) {
                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .border(
                                border = BorderStroke(4.dp, Color.Black),
                                shape = RectangleShape
                            )
                    ){
                        Text(
                            modifier = modifier.padding(7.dp),
                            text = "Detail",
                            fontSize = 20.sp
                        )
                    }
                    Column(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        remark.ticketID?.let {
                            Text(
                                modifier = modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = it.toString(),
                                fontSize = 18.sp
                            )
                        }
                        remark.rating?.let {
                            Text(
                                modifier = modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = it.toString(),
                                fontSize = 18.sp
                            )
                        }

                        remark.comment?.let {
                            Text(
                                modifier = modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = it,
                                fontSize = 18.sp
                            )
                        }

                        remark.ratedBy?.let {
                            /*profileViewModel.getAdminName(userId = it)
                            val adminName = profileViewModel.adminName.value*/
                            Text(
                                modifier = modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                text = it,
                                fontSize = 18.sp
                            )
                        }
                        Button(modifier = modifier
                            .align(Alignment.End),
                            onClick = { showDialog = false })
                        {
                            Text(
                                modifier = modifier,
                                text = "Okay",
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun TechBottomNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val items = listOf(
        TechnicianNavItem.Profile,
        TechnicianNavItem.TechTicketList,
    )
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(painterResource(id = item.icon), contentDescription = item.title) },
                label = { Text(text = item.title,
                    fontSize = 15.sp) },
                alwaysShowLabel = true,
                selected = currentRoute == item.screenroute,
                onClick = {
                    navController.navigate(item.screenroute) {
                        navController.graph.startDestinationRoute?.let { screenroute ->
                            popUpTo(screenroute) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}