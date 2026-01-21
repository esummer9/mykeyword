package com.ediapp.mykeyword.ui.notey

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.DatabaseHelper
//import com.ediapp.mykeyword.Memo
import com.ediapp.mykeyword.MemoActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun NoteyScreen() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var memos by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var showAddUserDicDialog by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(refreshTrigger) {
        memos = withContext(Dispatchers.IO) {
            dbHelper.getAllMemos(null)
        }
    }

    if (showAddUserDicDialog != null) {
        var keywordText by remember(showAddUserDicDialog) {
            mutableStateOf(showAddUserDicDialog ?: "")
        }

        AlertDialog(
            onDismissRequest = { showAddUserDicDialog = null },
            title = { Text("사용자 사전 추가") },
            text = {
                TextField(
                    value = keywordText,
                    onValueChange = { keywordText = it },
                    label = { Text("키워드") }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (keywordText.isNotBlank()) {
                            scope.launch {
                                val result = withContext(Dispatchers.IO) {
                                    dbHelper.addOrUpdateUserDic(0L, keywordText, "NNP") // Default to Proper Noun
                                }
                                if (result == -1L) {
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "이미 존재하는 키워드입니다.", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    showAddUserDicDialog = null
                                }
                            }
                        }
                    }
                ) {
                    Text("저장")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddUserDicDialog = null }
                ) {
                    Text("취소")
                }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context, MemoActivity::class.java)
                context.startActivity(intent)
                refreshTrigger++
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Memo")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            items(memos) { memo ->
                MemoItem(memo = memo, onLongClick = {
                    showAddUserDicDialog = memo.title
                }) {
                    val intent = Intent(context, MemoActivity::class.java).apply {
                        putExtra("memo_id", memo.id)
                    }
                    context.startActivity(intent)
                    refreshTrigger++
                }
            }
        }
    }
}

@Composable
fun MemoItem(memo: Memo, onLongClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = memo.title ?: "")
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = memo.regDt ?: "",
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }
        }
    }
}