package com.ediapp.mykeyword.ui.notey

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.MemoActivity
import com.ediapp.mykeyword.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteyScreen() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var memos: List<Memo> by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Memo?>(null) }
    var expandedMemo by remember { mutableStateOf<Memo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortDescending by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun refreshMemos() {
        scope.launch {
            val updatedMemos = withContext(Dispatchers.IO) {
                dbHelper.getAllMemos(null)
            }
            memos = updatedMemos
        }
    }

    val editMemoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshMemos()
        }
    }

    LaunchedEffect(Unit) { // Refresh on initial launch
        refreshMemos()
    }

    val filteredAndSortedMemos = remember(memos, searchQuery, sortDescending) {
        val filtered = if (searchQuery.isBlank()) {
            memos
        } else {
            memos.filter { it.title?.contains(searchQuery, ignoreCase = true) == true }
        }

        if (sortDescending) {
            filtered.sortedByDescending { it.regDate }
        } else {
            filtered.sortedBy { it.regDate }
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                val intent = Intent(context,MemoActivity::class.java)
                intent.putExtra("MEMO_ID", -1L)
                editMemoLauncher.launch(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Memo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search memos...") },
                    modifier = Modifier.weight(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = { sortDescending = !sortDescending }) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow),
                        contentDescription = "Sort memos",
                        tint = Color.Unspecified
                    )
                }
            }

            LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
                items(filteredAndSortedMemos) { memo ->
                    Box {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .combinedClickable(
                                    onClick = { /* No action on simple click */ },
                                    onLongClick = {
                                        if (memo.category == "notey") {
                                            expandedMemo = memo
                                        }
                                    }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = memo.title ?: "", fontWeight = FontWeight.Bold)
                                    Text(text = formatRegDate(memo.regDate))
                                }
                                IconButton(onClick = { 
                                    val intent = Intent(context, MemoActivity::class.java)
                                    intent.putExtra("MEMO_ID", memo.id)
                                    editMemoLauncher.launch(intent)
                                }) {
                                    Icon(painter = painterResource(id = R.drawable.edit_tool), contentDescription = "수정", modifier = Modifier.size(25.dp))
                                }
                            }
                        }
                        DropdownMenu(
                            expanded = expandedMemo == memo,
                            onDismissRequest = { expandedMemo = null }
                        ) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = {
                                val intent = Intent(context, MemoActivity::class.java)
                                intent.putExtra("MEMO_ID", memo.id)
                                editMemoLauncher.launch(intent)
                                expandedMemo = null
                            })
                            DropdownMenuItem(text = { Text("Duplicate") }, onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        dbHelper.duplicateMemo(memo.id)
                                    }
                                    refreshMemos()
                                }
                                expandedMemo = null
                            })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = {
                                showDeleteConfirmDialog = memo
                                expandedMemo = null
                            })
                        }
                    }
                }
            }
        }
    }

    showDeleteConfirmDialog?.let { memo ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Memo") },
            text = { Text("Are you sure you want to delete this memo?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                dbHelper.deleteMemo(memo.id)
                            }
                            refreshMemos()
                            showDeleteConfirmDialog = null
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatRegDate(regDate: Long?): String {
    if (regDate == null) return ""

    val currentTime = System.currentTimeMillis()
    val diff = currentTime - regDate
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(regDate))
        hours > 0 -> "${hours}시간 전"
        minutes > 0 -> "${minutes}분 전"
        else -> "방금 전"
    }
}
