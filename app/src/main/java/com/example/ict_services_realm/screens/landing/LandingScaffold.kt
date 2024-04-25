package com.example.ict_services_realm.screens.landing

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
fun LandingScaffold(modifier: Modifier = Modifier) {
    Scaffold(
        content = {
            Column(
                modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally)
            {
                Text(text = "FILLER LANDING SCREEN", modifier = modifier
                    .padding(
                        vertical = 6.dp,
                        horizontal = 14.dp
                    ),
                    fontSize = 17.sp)
            }
        }
    )
}