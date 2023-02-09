package com.programmersbox.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.realm.kotlin.mongodb.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LoginScreen(onLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    val db = LocalQuoteDb.current
    val vm = remember { LoginViewModel(scope, db) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Login") }) },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = vm.username,
                    onValueChange = { vm.username = it },
                    label = { Text("Email") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        capitalization = KeyboardCapitalization.None
                    )
                )

                OutlinedTextField(
                    value = vm.password,
                    onValueChange = { vm.password = it },
                    label = { Text("Password") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        capitalization = KeyboardCapitalization.None
                    )
                )

                Button(onClick = { vm.login(onLogin) }) { Text("Login") }
                Button(onClick = { vm.register() }) { Text("Register") }
            }
        }
    }
}

internal class LoginViewModel(
    private val scope: CoroutineScope,
    private val db: QuoteDatabase
) {
    var username by mutableStateOf("")
    var password by mutableStateOf("")

    fun login(onLogin: () -> Unit) {
        scope.launch {
            when (db.login(username, password)?.state) {
                User.State.LOGGED_OUT -> println("logged out")
                User.State.LOGGED_IN -> {
                    println("logged in")
                    onLogin()
                }

                User.State.REMOVED -> println("removed")
                null -> println("nothing")
            }
        }
    }

    fun register() {
        scope.launch { db.registration(password, username) }
    }
}