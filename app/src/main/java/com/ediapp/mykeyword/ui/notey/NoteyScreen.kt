package com.ediapp.mykeyword.ui.notey

import android.app.DatePickerDialog
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.Memo
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteyScreen() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper(context) }
    var memos by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var showDialog by remember { mutableStateOf(false) }
    var memoToEdit by remember { mutableStateOf<Memo?>(null) }
    var title by remember { mutableStateOf("") }
    var regDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Memo?>(null) }
    var expandedMemo by remember { mutableStateOf<Memo?>(null) }

    fun refreshMemos() {
        memos = dbHelper.getAllMemos()
    }

    fun openAddMemoDialog() {
        memoToEdit = null
        title = ""
        regDate = System.currentTimeMillis()
        showDialog = true
    }

    fun openEditMemoDialog(memo: Memo) {
        memoToEdit = memo
        title = memo.title ?: ""
        regDate = memo.regDate ?: System.currentTimeMillis()
        showDialog = true
    }

    fun closeDialog() {
        showDialog = false
        memoToEdit = null
    }

    LaunchedEffect(Unit) { // Refresh on initial launch
        refreshMemos()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { openAddMemoDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Memo")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
            items(memos) { memo ->
                Box {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .combinedClickable(
                                onClick = { /* No action on simple click */ },
                                onLongClick = { expandedMemo = memo }
                            )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = memo.title ?: "")
                            Text(text = memo.meaning ?: "")
                            Text(text = formatRegDate(memo.regDate))
                        }
                    }
                    DropdownMenu(
                        expanded = expandedMemo == memo,
                        onDismissRequest = { expandedMemo = null }
                    ) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = {
                            openEditMemoDialog(memo)
                            expandedMemo = null
                        })
                        DropdownMenuItem(text = { Text("Duplicate") }, onClick = {
                            dbHelper.duplicateMemo(memo.id)
                            refreshMemos()
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

    if (showDialog) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = regDate
        }
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                regDate = calendar.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val dateFormatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

        AlertDialog(
            onDismissRequest = { closeDialog() },
            title = { Text(if (memoToEdit == null) "Add Memo" else "Edit Memo") },
            text = {
                Column {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") }
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = dateFormatter.format(regDate),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Date") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Select date")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (memoToEdit == null) {
                            dbHelper.addMemo(title, regDate)
                        } else {
                            dbHelper.updateMemo(memoToEdit!!.id, title, regDate)
                        }
                        refreshMemos()
                        closeDialog()
                    },
                    enabled = title.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { closeDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteConfirmDialog?.let { memo ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Memo") },
            text = { Text("Are you sure you want to delete this memo?") },
            confirmButton = {
                Button(
                    onClick = {
                        dbHelper.deleteMemo(memo.id)
                        refreshMemos()
                        showDeleteConfirmDialog = null
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

    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        days < 1 -> {
            if (hours < 1) {
                if (minutes < 1) "방금 전" else "$minutes 분 전"
            } else {
                "$hours 시간 전"
            }
        }
        days in 1..7 -> "$days 일 전"
        else -> {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.format(Date(regDate))
        }
    }
}
