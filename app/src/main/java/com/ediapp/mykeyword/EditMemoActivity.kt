package com.ediapp.mykeyword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.ui.notey.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.text.SimpleDateFormat
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
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var memo by remember { mutableStateOf<Memo?>(null) }
    val scope = rememberCoroutineScope()

    // Memo state
    var title by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var regDt by remember { mutableStateOf("") }
    var regTm by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var sido by remember { mutableStateOf("") }
    var sigungu by remember { mutableStateOf("") }
    var eupmyeondong by remember { mutableStateOf("") }


    LaunchedEffect(memoId) {
        memo = withContext(Dispatchers.IO) {
            dbHelper.getMemoById(memoId)
        }
        memo?.let {
            title = it.title ?: ""
            meaning = it.meaning ?: ""
            url = it.url ?: ""
            regDt = it.regDt?.toString() ?: ""
            regTm = it.regTm?.toString() ?: ""
            address = it.address ?: ""
            sido = it.sido ?: ""
            sigungu = it.sigungu ?: ""
            eupmyeondong = it.eupmyeondong ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Memo") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            val regDate = dateFormat.parse("$regDt $regTm")?.time ?: 0L

                            dbHelper.updateMemo(
                                id = memoId,
                                title = title,
                                mean = meaning,
                                address = address,
                                url = url,
                                regDate = regDate
                            )
//                            dbHelper.addKeywords(title, memoId)
                        }
                        withContext(Dispatchers.Main) {
                            onSave()
                        }
                    }
                }
            ) {
                Text("Save")
            }
        }
    ) { padding ->
        if (memo != null) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                TextField(value = title, onValueChange = { title = it }, label = { Text("제목") }, modifier = Modifier.fillMaxWidth()) // TODO: Add other fields
                TextField(value = meaning, onValueChange = { meaning = it }, label = { Text("의미") }, modifier = Modifier.fillMaxWidth())
                TextField(value = url, onValueChange = { url = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                TextField(value = address, onValueChange = { address = it }, label = { Text("위치") }, modifier = Modifier.fillMaxWidth())
                TextField(value = regDt, onValueChange = { regDt = it }, label = { Text("등록일") }, modifier = Modifier.fillMaxWidth())
                TextField(value = regTm, onValueChange = { regTm = it }, label = { Text("시간") }, modifier = Modifier.fillMaxWidth())
            }
        } else {
            // Show a loading indicator or a message
            Text("Loading memo...", modifier = Modifier.padding(padding))
        }
    }
}
