package com.example.ict_services_realm.screens.technician.profile

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.ict_services_realm.app
import com.example.ict_services_realm.navigation.NavRoutes
import com.example.ict_services_realm.repository.SyncRepository
import com.example.ict_services_realm.screens.technician.TechnicianNavItem
import com.example.ict_services_realm.screens.technician.ticketInfo.TicketInfoScaffold
import com.example.ict_services_realm.screens.technician.ticketInfo.TicketInfoViewModel
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

@Composable
fun TechNavGraph(modifier: Modifier = Modifier,
                 repository: SyncRepository,
             navController: NavHostController,
             startDestination: String = NavRoutes.TechnicianProfile.screenroute,
             profileViewModel: ProfileViewModel,
             taskBarViewModel: TaskBarViewModel,
                 ticketListViewModel: TicketListViewModel)
{
    NavHost(modifier = modifier,navController = navController, startDestination = startDestination) {
        // TECHNICIAN ROUTES
        composable(NavRoutes.TechnicianProfile.screenroute) {
            //TODO: PUT BACK COMPLETED TICKET LIST
            ProfileScaffold(navController = navController, profileViewModel = profileViewModel, taskBarViewModel = taskBarViewModel)
        }
        composable(NavRoutes.TechnicianTickets.screenroute) {
            TicketListScaffold(navController = navController, ticketListViewModel = ticketListViewModel, taskBarViewModel = taskBarViewModel)
        }
        composable("${NavRoutes.TechnicianTicketInfo.screenroute}/{ticketID}") { navBackStackEntry ->
            val ticketID = navBackStackEntry.arguments?.getString("ticketID")
            val ticketInfoViewModel = ticketID?.let { TicketInfoViewModel(repository, it.toInt()) }
            if (ticketInfoViewModel != null) {
                TicketInfoScaffold(navController = navController, ticketInfo = ticketInfoViewModel)
            }
        }

        // ADMIN ROUTES
/*        composable(NavRoutes.AdminTicketsForm.screenroute) {

        }
        composable(NavRoutes.AdminTicketsList.screenroute) {

        }
        composable(NavRoutes.AdminRate.screenroute) {

        }*/
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