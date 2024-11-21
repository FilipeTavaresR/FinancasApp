package com.filipetavares.financasapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.filipetavares.financasapp.model.Bill
import com.filipetavares.financasapp.model.FinancialRecord
import com.filipetavares.financasapp.ui.theme.DarkGreen
import com.filipetavares.financasapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.math.BigDecimal

class MovementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainContent()
        }
    }
}

private val firebaseAuth by lazy { FirebaseAuth.getInstance() }
private val firebaseStore by lazy { FirebaseFirestore.getInstance() }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
private fun MainContent() {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            MainTopBar {
                firebaseAuth.signOut()
                showLogin(context)
            }
        },
        floatingActionButton = { MainFab() }
    ) { paddingValues ->
        FinancialList(modifier = Modifier.padding(paddingValues))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTopBar(onLogoutClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        ),
        actions = {
            IconButton(onClick = onLogoutClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    tint = Color.White,
                    contentDescription = null
                )
            }
        }
    )
}

@Composable
private fun MainFab() {
    val context = LocalContext.current
    FloatingActionButton(onClick = { showAddForm(context) }) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}


@Composable
private fun FinancialList(modifier: Modifier) {
    val idUser = firebaseAuth.currentUser?.uid
    var balance = BigDecimal(0.00)
    val context = LocalContext.current
    val listFinancialRecord = remember { mutableStateListOf<FinancialRecord>() }

    if (idUser != null) {
        firebaseStore
            .collection(Constants.Collections.DATA)
            .document(idUser)
            .collection(Constants.Collections.BILLS)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    Toast.makeText(
                        context,
                        "Erro ao cadastrar conta: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    querySnapshot?.documents?.forEach {
                        val bill = it.toObject(Bill::class.java)
                        if (bill != null) {
                            balance += bill.value.toBigDecimal()
                            listFinancialRecord.add(
                                FinancialRecord(
                                    date = bill.date,
                                    description = bill.description,
                                    debit = bill.value.toString(),
                                    balance = balance.toString(),
                                    id = it.id
                                )
                            )
                        }
                    }
                }
            }
    }

    Column(
        modifier = modifier
    ) {
        HeaderRow()


        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {

            items(listFinancialRecord) { record ->
                FinancialListItem(record){clickedRecord ->
                    val editIntent = Intent(context, FormBillActivity::class.java).apply {
                        putExtra("date", clickedRecord.date)
                        putExtra("description", clickedRecord.description)
                        putExtra("debit", clickedRecord.debit)
                        putExtra("id", clickedRecord.id)
                    }
                    context.startActivity(editIntent)
                }
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
            }
        }
    }
}

@Composable
private fun HeaderRow() {
    Row(
        modifier = Modifier
            .background(Color.LightGray)
            .border(1.dp, Color.Gray)
    ) {
        Text(
            text = "Data",
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )
        Text(
            text = "Descrição",
            fontSize = 14.sp,
            modifier = Modifier.weight(2f),
            color = Color.Black
        )
        Text(
            text = "Valor",
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )
        Text(
            text = "Saldo",
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            color = Color.Black
        )
    }
}

@Composable
private fun FinancialListItem(record: FinancialRecord,onItemClick: (FinancialRecord) -> Unit) {
    val fonteSize = 12.sp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray)
            .padding(top = 4.dp, bottom = 4.dp)
            .background(Color.White)
            .clickable{onItemClick(record)}
    ) {
        Text(
            text = record.date,
            fontSize = fonteSize,
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp, end = 2.dp)
        )
        Text(
            text = record.description,
            fontSize = fonteSize,
            modifier = Modifier
                .weight(2f)
                .padding(start = 2.dp, end = 2.dp)
        )
        Text(
            text = record.debit,
            fontSize = fonteSize,
            color = if (record.debit.startsWith("-")) Color.Red else DarkGreen,
            modifier = Modifier
                .weight(1f)
                .padding(start = 2.dp, end = 2.dp)
        )
        Text(
            text = record.balance,
            fontSize = fonteSize,
            modifier = Modifier.weight(1f)
                .padding(start = 2.dp, end = 2.dp)
        )
    }
}

private fun showAddForm(context: Context) {
    context.startActivity(Intent(context, FormBillActivity::class.java))
}

private fun showLogin(context: Context) {
    context.startActivity(Intent(context, LoginActivity::class.java))
}


