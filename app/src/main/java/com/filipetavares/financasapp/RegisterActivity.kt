package com.filipetavares.financasapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.filipetavares.financasapp.model.Usuario
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterContent(onBackPressed = { finish() })
        }
    }
}

private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firebaseStore by lazy { FirebaseFirestore.getInstance() }


@Composable
private fun RegisterContent(onBackPressed: () -> Unit) {
    Scaffold(
        topBar = { RegisterTopBar(onBackPressed) },
    ) { paddingValues ->
        FormFields(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
private fun FormFields(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nameState by remember { mutableStateOf("") }
    var loginState by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    var passWordState by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Image(painter = painterResource(R.drawable.financaslogo), contentDescription = null)
        OutlinedTextField(
            value = nameState,
            label = { Text(text = stringResource(R.string.name_field)) },
            onValueChange = { nameState = it },
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = loginState,
            label = { Text(text = stringResource(R.string.email_field)) },
            onValueChange = {
                loginState = it
                isEmailValid = validarEmail(loginState)
            },
            modifier = Modifier.padding(top = 8.dp),
            isError = !isEmailValid
        )
        OutlinedTextField(
            value = passWordState,
            label = { Text(text = stringResource(R.string.senha)) },
            onValueChange = { passWordState = it },
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(
            onClick = {
                firebaseAuth.createUserWithEmailAndPassword(
                    loginState, passWordState
                ).addOnCompleteListener { resultado ->
                    if (resultado.isSuccessful) {
                        val idusuario = resultado.result.user?.uid
                        if (idusuario != null) {
                            val usuario = Usuario(
                                idusuario, nameState, loginState
                            )
                            salvarUsuarioFirestore(usuario, context)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        context,
                        "Erro ao realizar o cadastro: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.register_button))
        }
    }
}

fun salvarUsuarioFirestore(usuario: Usuario, context: Context) {
    firebaseStore
        .collection("usuarios")
        .document(usuario.id)
        .set(usuario)
        .addOnSuccessListener {
            Toast.makeText(context, "Cadastro realizado com sucesso", Toast.LENGTH_LONG)
            showLogin(context)
        }.addOnFailureListener {
            Toast.makeText(context, "Erro ao realizado o cadastro", Toast.LENGTH_LONG)
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RegisterTopBar(onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)
    TopAppBar(
        title = { Text(text = stringResource(R.string.register_title)) },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        )
    )
}

private fun validarEmail(email: String): Boolean {
    val regexEmail = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
    return regexEmail.matches(email)
}

private fun showLogin(context: Context) {
    context.startActivity(Intent(context, LoginActivity::class.java))
}