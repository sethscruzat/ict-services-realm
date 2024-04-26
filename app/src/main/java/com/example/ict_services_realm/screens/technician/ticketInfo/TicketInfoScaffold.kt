package com.example.ict_services_realm.screens.technician.ticketInfo

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TicketInfoScaffold(modifier: Modifier = Modifier,
                       navController: NavHostController,
                       ticketInfo: TicketInfoViewModel)
{
    val ticketInfoState = ticketInfo.ticketInfoState.value
    val ctx = LocalContext.current
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
            ticketInfoState?.equipmentID?.let { it1 ->
                Text(text = it1, modifier = modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 10.dp
                    ),
                    fontSize = 17.sp)
            }
            ticketInfoState?.location?.let { it1 ->
                Text(text = it1, modifier = modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 10.dp
                    ),
                    fontSize = 17.sp)
            }
            ticketInfoState?.remarks?.let { it1 ->
                Text(text = it1, modifier = modifier
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

            Button(
                modifier = modifier
                    .padding(12.dp)
                    .align(Alignment.End),
                onClick = {
                    ticketInfoState?.ticketID?.let { it1 -> ticketInfo.markTicketAsDone(it1) }
                    Toast.makeText(ctx, "Ticket marked as Done", Toast.LENGTH_SHORT).show()
                    navController.navigateUp()
                    /* TODO: 1) IMPLEMENT NOTIFY*/
                }
            )
            {
                Text("Mark As Done")
            }
        }
    }
}