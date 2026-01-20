package com.ediapp.mykeyword.ui.notey

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.MemoActivity
import com.ediapp.mykeyword.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteyScreen(refreshTrigger: Int = 0, searchVisible: Boolean) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var memos: List<Memo> by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Memo?>(null) }
    var expandedMemo by remember { mutableStateOf<Memo?>(null) }
    var showAddUserDicDialog by remember { mutableStateOf<Memo?>(null) } // For dialog
    var searchQuery by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("1주") }
    var quickMemoText by remember { mutableStateOf("") }
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

    LaunchedEffect(refreshTrigger) { // Refresh on initial launch
        refreshMemos()
    }

    LaunchedEffect(searchVisible) {
        if (!searchVisible) {
//            searchQuery = ""
        }
    }

    val filteredAndSortedMemos = remember(memos, searchQuery, selectedPeriod) {
        val dateFilteredMemos = if (selectedPeriod == "전체") {
            memos
        } else {
            val calendar = Calendar.getInstance()
            when (selectedPeriod) {
                "2일" -> calendar.add(Calendar.DATE, -2)
                "1주" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
                "1개월" -> calendar.add(Calendar.MONTH, -1)
            }
            val startDate = calendar.timeInMillis
            memos.filter { it.regDate != null && it.regDate >= startDate }
        }

        val filtered = if (searchQuery.isBlank()) {
            dateFilteredMemos
        } else {
            dateFilteredMemos.filter { it.title?.contains(searchQuery, ignoreCase = true) == true }
        }

        filtered.sortedByDescending { it.regDate }
    }

    if (showAddUserDicDialog != null) {
        var keywordText by remember(showAddUserDicDialog) {
            mutableStateOf(showAddUserDicDialog?.title ?: "")
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
                            scope.launch(Dispatchers.IO) {
                                dbHelper.addOrUpdateUserDic(-1L, keywordText, "NNP") // Default to Proper Noun
                            }
                            showAddUserDicDialog = null
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
                val intent = Intent(context,MemoActivity::class.java)
                intent.putExtra("MEMO_ID", -1L)
                editMemoLauncher.launch(intent)
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Memo")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            AnimatedVisibility(visible = searchVisible) {
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
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                val periods = listOf("2일", "1주", "1개월", "전체")
                periods.forEach { period ->
                    Button(
                        onClick = { selectedPeriod = period },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text(text = period)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = quickMemoText,
                    onValueChange = { quickMemoText = it },
                    placeholder = { Text("Quick memo...") },
                    modifier = Modifier.weight(1f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(5.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = {
                    if (quickMemoText.isNotBlank()) {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                dbHelper.addMemo(
                                    title = quickMemoText,
                                    mean = null,
                                    url = null,
                                    address = null,
                                    regDate = System.currentTimeMillis()
                                )
                            }
                            quickMemoText = ""
                            refreshMemos()
                        }
                    }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.check),
                        contentDescription = "Save Quick Memo",
                        tint = MaterialTheme.colorScheme.primary
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
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.edit)) }, onClick = {
                                val intent = Intent(context, MemoActivity::class.java)
                                intent.putExtra("MEMO_ID", memo.id)
                                editMemoLauncher.launch(intent)
                                expandedMemo = null
                            })
                            DropdownMenuItem(
                                text = { Text("사용자 사전 추가") },
                                onClick = {
                                    showAddUserDicDialog = memo
                                    expandedMemo = null
                                }
                            )
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.duplicate)) }, onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        dbHelper.duplicateMemo(memo.id)
                                    }
                                    refreshMemos()
                                }
                                expandedMemo = null
                            })
                            DropdownMenuItem(text = { Text(stringResource(id = R.string.delete)) }, onClick = {
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
            title = { Text(stringResource(id = R.string.delete_memo_title)) },
            text = { Text(stringResource(id = R.string.delete_memo_message)) },
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
                    Text(stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmDialog = null }) {
                    Text(stringResource(id = R.string.cancel))
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
