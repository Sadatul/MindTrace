package com.example.frontend.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.frontend.api.RetrofitInstance

@Composable
fun MainScreen(
    toChatScreen: () -> Unit,
    toRegisterCaregiverScreen: () -> Unit
) {

    var serverIsUp by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = null) {
        val body = RetrofitInstance.dementiaAPI.getHealth().body()
        body?.let {
            if (body.status == "UP") {
                serverIsUp = true;
            }
        }
    }

    Column {
        Text("Server is " + if(serverIsUp) "UP" else "DOWN")

        Button(onClick = toChatScreen) {
            Text("Chat screen")
        }

        Button(onClick = toRegisterCaregiverScreen) {
            Text("Register caregiver screen")
        }
    }
}