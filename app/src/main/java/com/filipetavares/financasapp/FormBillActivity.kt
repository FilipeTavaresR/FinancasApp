package com.filipetavares.financasapp

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.filipetavares.financasapp.model.Bill
import com.filipetavares.financasapp.model.FinancialRecord
import com.filipetavares.financasapp.utils.Constants.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FormBillActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        var financialRecord = FinancialRecord()
        val id = intent.getStringExtra("id") ?: ""
        val debit = intent.getStringExtra("debit") ?: ""
        val description = intent.getStringExtra("description") ?: ""
        val date = intent.getStringExtra("date") ?: ""
        if (id != "") {
            financialRecord = FinancialRecord(
                date = date,
                description = description,
                debit = debit,
                id = id
            )
        }
        super.onCreate(savedInstanceState)
        setContent {
            FormContent(onBackPressed = { finish() }, financialRecord)
        }
    }
}


private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firebaseStore by lazy { FirebaseFirestore.getInstance() }


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun FormContent(onBackPressed: () -> Unit, financialRecord: FinancialRecord) {
    Scaffold(
        topBar = { FormTopBar(onBackPressed) },
    ) { paddingValues ->
        FormFields(modifier = Modifier.padding(paddingValues), financialRecord)
    }
}

@Composable
private fun FormFields(modifier: Modifier = Modifier, financialRecord: FinancialRecord) {
    val options =
        listOf(stringResource(R.string.radio_expense), stringResource(R.string.radio_incoming))
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    var movimentState by remember {
        if (financialRecord.description != "") {
            mutableStateOf(financialRecord.description)
        } else mutableStateOf("")
    }
    var selectedDate by remember {
        if (financialRecord.date != "") {
            mutableStateOf(financialRecord.date)
        } else mutableStateOf(getCurrentDate())
    }
    var amountState by remember {
        if (financialRecord.debit != "") {
            mutableStateOf(financialRecord.debit)
        } else mutableStateOf("0.00")
    }
    var radioButtonState by remember {
        if (financialRecord.debit != "" && financialRecord.debit.toBigDecimal() > BigDecimal(0)) {
            mutableStateOf(options[1])
        } else mutableStateOf(options[0])
    }
    val datePickerDialog = remember {
        createDatePickerDialog(context, calendar) { date ->
            selectedDate = date
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        OutlinedTextField(
            value = selectedDate,
            onValueChange = {},
            label = { Text(getCurrentDate()) },
            readOnly = true,
            modifier = Modifier
                .clickable { datePickerDialog.show() }
                .padding(top = 16.dp)
        )
        OutlinedTextField(
            value = movimentState,
            label = { Text(text = stringResource(R.string.hint_moviment)) },
            onValueChange = { movimentState = it },
            modifier = Modifier.padding(top = 8.dp)
        )
        OutlinedTextField(
            value = amountState,
            onValueChange = { newValue ->
                // Aceita apenas números
                if (newValue.matches(Regex("^[0-9]*([.,][0-9]{0,2})?\$"))) {
                    amountState = newValue
                }
            },
            modifier = Modifier.padding(top = 16.dp),
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number // Exibe teclado numérico
            ),
            label = { Text("Número") }
        )
        Row(modifier = Modifier.padding(24.dp)) {
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    RadioButton(
                        selected = (option == radioButtonState),
                        onClick = { radioButtonState = option }
                    )
                    Text(text = option)
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val idUsuser = firebaseAuth.currentUser?.uid
            Button(
                onClick = {
                    if (financialRecord.id != "") {

                        if (idUsuser != null) {
                            firebaseStore
                                .collection(Collections.DATA)
                                .document(idUsuser)
                                .collection(Collections.BILLS)
                                .document(financialRecord.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Excluído com sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showMovementForm(context)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        "Erro ao excluir conta: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    }
                }
            ) {
                Text(text = stringResource(R.string.delete))
            }
            Button(
                onClick = {
                    val negative = -1
                    if (idUsuser != null && selectedDate.isNotEmpty() && amountState.toDouble() != 0.00 && movimentState.isNotBlank()) {
                        val bill =
                            Bill(
                                selectedDate, movimentState, radioButtonState,
                                if (radioButtonState == "Despesa" && amountState.toDouble() > 0.00) {
                                    amountState.toDouble() * negative
                                } else {
                                    if (radioButtonState == "Receita" && amountState.toDouble() < 0.00) {
                                        amountState.toDouble() * negative
                                    } else {
                                        amountState.toDouble()
                                    }
                                }
                            )
                        if (financialRecord.id == "") {
                            firebaseStore
                                .collection(Collections.DATA)
                                .document(idUsuser)
                                .collection(Collections.BILLS)
                                .add(bill)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Adicionado com sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showMovementForm(context)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        "Erro ao cadastrar conta: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        } else if (financialRecord.id != "") {

                            firebaseStore
                                .collection(Collections.DATA)
                                .document(idUsuser)
                                .collection(Collections.BILLS)
                                .document(financialRecord.id)
                                .set(bill)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        context,
                                        "Alterado com sucesso",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    showMovementForm(context)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        context,
                                        "Erro ao alterar conta: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Preencha todos os dados",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                },
            ) {
                Text(text = stringResource(R.string.add_moviment_button))
            }
        }
    }

}

private fun showMovementForm(context: Context) {
    context.startActivity(Intent(context, MovementActivity::class.java))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormTopBar(onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)
    TopAppBar(
        title = { Text(text = stringResource(R.string.add_title)) },
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

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(Date())
}

private fun createDatePickerDialog(
    context: Context,
    calendar: Calendar,
    onDateSelected: (String) -> Unit
): DatePickerDialog {
    return DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            onDateSelected(dateFormat.format(calendar.time))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
}




