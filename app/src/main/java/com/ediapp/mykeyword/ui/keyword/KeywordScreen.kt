
package com.ediapp.mykeyword.ui.keyword

import android.content.ContentValues
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.Keyword
import com.ediapp.mykeyword.KeywordMemosActivity
import com.ediapp.mykeyword.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeywordScreen(refreshKey: Int) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()

    var keywords by remember { mutableStateOf<List<Keyword>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf("1개월") }
    var addUserDicOriginalKeyword by remember { mutableStateOf("") }
    var keywordToDelete by remember { mutableStateOf<Keyword?>(null) }
    var expandedKeyword by remember { mutableStateOf<Keyword?>(null) } // For long press menu
    var showAddUserDicDialog by remember { mutableStateOf<Keyword?>(null) } // For dialog

    fun getKeywords(period: String) {
        scope.launch {
            val calendar = Calendar.getInstance()
            val endDate = calendar.timeInMillis
            when (period) {
                "2일" -> calendar.add(Calendar.DATE, -2)
                "1주" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
                "1개월" -> calendar.add(Calendar.MONTH, -1)
                "전체" -> calendar.timeInMillis = 0
            }
            val startDate = calendar.timeInMillis
            val newKeywords = withContext(Dispatchers.IO) {
                dbHelper.getKeywordsByDateRange(startDate, endDate)
            }
            keywords = newKeywords
            selectedPeriod = period
        }
    }

    LaunchedEffect(refreshKey) {
        getKeywords(selectedPeriod)
    }

    if (keywordToDelete != null) {
        AlertDialog(
            onDismissRequest = { keywordToDelete = null },
            title = { Text("키워드 삭제") },
            text = { Text("'${keywordToDelete?.keyword}' 을(를) 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            keywordToDelete?.let { dbHelper.updateMemoStatusForKeywordAndDelete(it.keyword) }
                            keywordToDelete = null
                            getKeywords(selectedPeriod)
                        }
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { keywordToDelete = null }
                ) {
                    Text("취소")
                }
            }
        )
    }

    if (showAddUserDicDialog != null) {
        var keywordText by remember(showAddUserDicDialog) {
            mutableStateOf(showAddUserDicDialog?.keyword ?: "")
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
                        Log.d("KeywordScreen", "${addUserDicOriginalKeyword} != ${keywordText}")
                        if(addUserDicOriginalKeyword != keywordText) {
                            if (keywordText.isNotBlank()) {
                                scope.launch(Dispatchers.IO) {
                                    val db = dbHelper.writableDatabase
                                    val values = ContentValues().apply {
                                        put(DatabaseHelper.DICS_COL_KEYWORD, keywordText)
                                        put(
                                            DatabaseHelper.DICS_COL_POS,
                                            "NNP"
                                        ) // Default to Proper Noun
                                    }
                                    db.insert(DatabaseHelper.TABLE_DICS, null, values)
                                }
                                showAddUserDicDialog = null
                            }
                        } else {
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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                val periods = listOf("2일", "1주", "1개월", "전체")
                periods.forEach { period ->
                    Button(
                        onClick = { getKeywords(period) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedPeriod == period) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text(text = period)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "'${selectedPeriod}' 필터링")

            LazyColumn {
                items(keywords) { keyword ->
                    Box {
                        KeywordItem(
                            keyword = keyword,
                            onItemClick = { kw ->
                                val intent = Intent(context, KeywordMemosActivity::class.java)
                                intent.putExtra("KEYWORD", kw.keyword)
                                context.startActivity(intent)
                            },
                            onLongClick = { kw -> expandedKeyword = kw }
                        )
                        DropdownMenu(
                            expanded = expandedKeyword == keyword,
                            onDismissRequest = { expandedKeyword = null }
                        ) {
                            DropdownMenuItem(
                                text = { Text("사용자 사전 추가") },
                                onClick = {
                                    addUserDicOriginalKeyword = keyword.keyword
                                    showAddUserDicDialog = keyword
                                    expandedKeyword = null
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("삭제") },
                                onClick = {
                                    keywordToDelete = keyword
                                    expandedKeyword = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeywordItem(
    keyword: Keyword,
    onItemClick: (Keyword) -> Unit,
    onLongClick: (Keyword) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .combinedClickable(
                onClick = { onItemClick(keyword) },
                onLongClick = { onLongClick(keyword) }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = keyword.keyword, modifier = Modifier.weight(1f))
            Text(text = "${keyword.count}건")
        }
    }
}
