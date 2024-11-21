package com.filipetavares.financasapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginContent()
        }
    }
}

private val firebaseAuth by lazy { FirebaseAuth.getInstance() }



@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun LoginContent() {
    val context = LocalContext.current
    if(firebaseAuth.currentUser == null) {
        Scaffold {
            FormFields(context,modifier = Modifier.padding(top = 80.dp))
        }
    }else{
        showMovmentScreen(context)
    }
}

@Composable
private fun FormFields(context: Context, modifier: Modifier = Modifier) {
    var loginState by remember { mutableStateOf("") }
    var passWordState by remember { mutableStateOf("") }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Image(painter = painterResource(R.drawable.financaslogo), null)
        OutlinedTextField(
            value = loginState,
            label = { Text(text = stringResource(R.string.login)) },
            onValueChange = { loginState = it },
            modifier = Modifier.padding(top = 28.dp)
        )
        OutlinedTextField(
            value = passWordState,
            label = { Text(text = stringResource(R.string.senha)) },
            onValueChange = { passWordState = it },
            modifier = Modifier.padding(top = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Button(
            onClick = {
                if (!loginState.isNullOrBlank() && !passWordState.isNullOrBlank()) {
                    firebaseAuth.signInWithEmailAndPassword(loginState, passWordState)
                        .addOnSuccessListener {
                            showMovmentScreen(context)
                        }.addOnFailureListener { exception ->
                            Toast.makeText(
                                context,
                                "Erro ao realizar o login: ${exception.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                } else {
                    Toast.makeText(context, "Preencha login e senha", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .padding(top = 16.dp)
        ) {
            Text(text = stringResource(R.string.login_button))
        }
        ClickableText(
            modifier = Modifier
                .padding(16.dp),
            text = AnnotatedString(stringResource(R.string.register_text)),
            style = TextStyle(textDecoration = TextDecoration.Underline, color = Color.Blue)
        ) {
            showRegisterForm(context)
        }
    }
}

private fun showMovmentScreen(context: Context) {
    context.startActivity(Intent(context, MovementActivity::class.java))
}

private fun showRegisterForm(context: Context) {
    context.startActivity(Intent(context, RegisterActivity::class.java))
}