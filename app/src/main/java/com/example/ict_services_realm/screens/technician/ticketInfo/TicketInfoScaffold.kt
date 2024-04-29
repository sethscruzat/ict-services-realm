package com.example.ict_services_realm.screens.technician.ticketInfo

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.ict_services_realm.screens.technician.profile.LoadingIndicator

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "StateFlowValueCalledInComposition")
@Composable
fun TicketInfoScaffold(modifier: Modifier = Modifier,
                       navController: NavHostController,
                       ticketInfo: TicketInfoViewModel)
{
    val ticketInfoState = ticketInfo.ticketInfoState.value
    val ctx = LocalContext.current
    var showConfirmDialog = remember { mutableStateOf(false)}
    if(ticketInfoState!= null){
        Column(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight())
        {
            Button(
                modifier = modifier
                    .padding(12.dp)
                    .align(Alignment.End),
                onClick = {
                    navController.navigateUp()
                }
            ) { Text("Back") }
            Spacer(modifier = Modifier.weight(0.1f))
            Column(modifier = modifier.weight(2f)){
                Text(text = "(PLACEHOLDER): ${ticketInfoState.equipmentID}", modifier = modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 10.dp
                    ),
                    fontSize = 17.sp)
                ticketInfoState.location?.let {
                    Text(text = "Location: $it", modifier = modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 10.dp
                        ),
                        fontSize = 17.sp)
                }
                ticketInfoState.remarks?.let {
                    Text(text = "Condition: $it", modifier = modifier
                        .padding(
                            vertical = 6.dp,
                            horizontal = 10.dp
                        ),
                        fontSize = 17.sp)
                }
                /*Text(text = issuedBy, modifier = modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 10.dp
                    ),
                    fontSize = 17.sp)*/

                if(ticketInfoState.status == "In Progress"){
                    Button(
                        modifier = modifier
                            .padding(12.dp)
                            .align(Alignment.End),
                        onClick = {
                            showConfirmDialog.value = true
                            /* TODO: 1) IMPLEMENT NOTIFY*/
                        }
                    )
                    {
                        Text("Mark As Done")
                    }
                }
            }
            if(showConfirmDialog.value){
                AlertDialog(
                    onDismissRequest = { showConfirmDialog.value = false },
                    confirmButton = {
                        Button(onClick = {
                            ticketInfoState.ticketID?.let { ticketInfo.markTicketAsDone(it) }
                            showConfirmDialog.value = false
                            Toast.makeText(ctx, "Ticket marked as Done", Toast.LENGTH_SHORT).show()
                            navController.navigateUp()
                        }) {
                            Text(text = "Confirm")
                        }
                    },
                    dismissButton = {
                        Button(onClick = {
                            showConfirmDialog.value = false
                        }) {
                            Text(text = "Dismiss")
                        }
                    },
                    title = { Text(text = "Confirmation")},
                    text = { Text(text = "Do you want to mark the task as done?")},
/*                    containerColor = Color.LightGray,
                    modifier = modifier.border(BorderStroke(4.dp, Color.Black), RectangleShape)*/
                )
            }
        }
    }else{
        LoadingIndicator()
    }
}