package com.example.frontend.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.frontend.api.RetrofitInstance

@Composable
fun MainScreen(
    toChatScreen: (token: String) -> Unit,
    toRegisterCaregiverScreen: () -> Unit
) {

    var serverStatus by remember { mutableStateOf("<Loading...>") }
    var token by remember { mutableStateOf("") }

    LaunchedEffect(key1 = null) {
        val body = RetrofitInstance.dementiaAPI.getHealth().body()
        println(body)
        body?.let {
            serverStatus = body.status
        }
    }

    Column {
        Text("Server is $serverStatus")

        TextField(
            value = token,
            onValueChange = { token = it },
            label = {
                Text("Firebase token")
            }
        )
        Button(onClick = { toChatScreen(token) } ) {
            Text("Chat screen")
        }

        Button(onClick = toRegisterCaregiverScreen) {
            Text("Register caregiver screen")
        }
    }
}