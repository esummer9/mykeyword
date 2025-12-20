package com.ediapp.mykeyword

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.ui.notey.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EditMemoActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val memoId = intent.getLongExtra("MEMO_ID", -1L)
        if (memoId == -1L) {
            finish()
            return
        }

        setContent {
            MaterialTheme {
                EditMemoScreen(memoId = memoId, onSave = { finish() }, onNavigateBack = { finish() })
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMemoScreen(memoId: Long, onSave: () -> Unit, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val dbHelper = if (inPreview) null else remember { DatabaseHelper.getInstance(context) }
    var memo by remember { mutableStateOf<Memo?>(null) }
    val scope = rememberCoroutineScope()

    // Memo state
    var title by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }
    var address by remember { mutableStateOf("") }
    var sido by remember { mutableStateOf("") }
    var sigungu by remember { mutableStateOf("") }
    var eupmyeondong by remember { mutableStateOf("") }

    val transparentTextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedIndicatorColor = Color.Gray,
        unfocusedIndicatorColor = Color.Gray,
        disabledIndicatorColor = Color.Transparent
    )
    
    LaunchedEffect(memoId) {
        if (!inPreview) {
            memo = withContext(Dispatchers.IO) {
                dbHelper?.getMemoById(memoId)
            }
            memo?.let {
                title = it.title ?: ""
                meaning = it.meaning ?: ""
                url = it.url ?: ""
                it.regDate?.let { date ->
                    val newCal = Calendar.getInstance()
                    newCal.timeInMillis = date
                    calendar = newCal
                }
                address = it.address ?: ""
                sido = it.sido ?: ""
                sigungu = it.sigungu ?: ""
                eupmyeondong = it.eupmyeondong ?: ""
            }
        }
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val newCal = calendar.clone() as Calendar
            newCal.set(year, month, dayOfMonth)
            calendar = newCal
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val newCal = calendar.clone() as Calendar
            newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newCal.set(Calendar.MINUTE, minute)
            calendar = newCal
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // 24 hour view
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.edit_memo)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                onClick = {
                    if (!inPreview) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                dbHelper?.updateMemo(
                                    id = memoId,
                                    title = title,
                                    mean = meaning,
                                    address = address,
                                    url = url,
                                    regDate = calendar.timeInMillis
                                )
//                            dbHelper.addKeywords(title, memoId)
                            }
                            withContext(Dispatchers.Main) {
                                onSave()
                            }
                        }
                    } else {
                        onSave()
                    }
                }
            ) {
                Text("Save")
            }
        }
    ) { padding ->
        if (memo != null || inPreview) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {


                TextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(id = R.string.title)) }, modifier = Modifier.fillMaxWidth(), colors = transparentTextFieldColors)
                TextField(value = meaning, onValueChange = { meaning = it }, label = { Text("의미") }, modifier = Modifier.fillMaxWidth(), colors = transparentTextFieldColors)
                TextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth(), colors = transparentTextFieldColors)
                TextField(value = address, onValueChange = { address = it }, label = { Text("위치") }, modifier = Modifier.fillMaxWidth(), colors = transparentTextFieldColors)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = dateFormat.format(calendar.time),
                        onValueChange = { },
                        label = { Text(stringResource(id = R.string.registration_date)) },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        colors = transparentTextFieldColors
                    )
                    IconButton(onClick = { datePickerDialog.show() }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select date")
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextField(
                        value = timeFormat.format(calendar.time),
                        onValueChange = { },
                        label = { Text("시간") },
                        modifier = Modifier.weight(1f),
                        readOnly = true,
                        colors = transparentTextFieldColors
                    )
                    IconButton(onClick = { timePickerDialog.show() }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)) {
                        Icon(painter = painterResource(id = R.drawable.time), contentDescription = "Select time")
                    }
                }
            }
        } else {
            // Show a loading indicator or a message
            Text("Loading memo...", modifier = Modifier.padding(padding))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditMemoScreenPreview() {
    MaterialTheme {
        EditMemoScreen(memoId = -1L, onSave = {}, onNavigateBack = {})
    }
}
