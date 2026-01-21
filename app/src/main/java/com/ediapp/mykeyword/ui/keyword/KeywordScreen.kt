
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import java.util.Calendar

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KeywordScreen() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()

    var keywords by remember { mutableStateOf<List<Keyword>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf("1개월") }
    var myDictionaryValue by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf(0) }
    var showReprocessDialog by remember { mutableStateOf(false) }
    var isReprocessing by remember { mutableStateOf(false) }
    var keywordToDelete by remember { mutableStateOf<Keyword?>(null) }
    var expandedKeyword by remember { mutableStateOf<Keyword?>(null) } // For long press menu
    var showAddUserDicDialog by remember { mutableStateOf<Keyword?>(null) } // For dialog

    fun getKeywords(period: String) {
        val calendar = Calendar.getInstance()
        val endDate = calendar.timeInMillis
        when (period) {
            "2일" -> calendar.add(Calendar.DATE, -2)
            "1주" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
            "1개월" -> calendar.add(Calendar.MONTH, -1)
            "전체" -> calendar.timeInMillis = 0
        }
        val startDate = calendar.timeInMillis
        keywords = dbHelper.getKeywordsByDateRange(startDate, endDate)
        selectedPeriod = period
    }

    LaunchedEffect(refreshKey) {
        getKeywords(selectedPeriod)
    }

    if (showReprocessDialog) {
        AlertDialog(
            onDismissRequest = { showReprocessDialog = false },
            title = { Text("키워드 재처리") },
            text = { Text("메모의 형태소를 분석합니다") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showReprocessDialog = false
                        scope.launch {
                            isReprocessing = true
                            dbHelper.reprocessKeywords()
                            isReprocessing = false
                            refreshKey++ // Trigger a refresh
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showReprocessDialog = false }
                ) {
                    Text("취소")
                }
            }
        )
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
                            keywordToDelete?.let { dbHelper.deleteKeyword(it.keyword) }
                            keywordToDelete = null
                            refreshKey++
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
        myDictionaryValue = showAddUserDicDialog?.keyword ?: ""
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
//                        Toast.makeText(context, myDictionaryValue, Toast.LENGTH_SHORT)
                        Log.d("KeywordScreen", "${myDictionaryValue} != ${keywordText}")
                        if(myDictionaryValue != keywordText) {
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

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showReprocessDialog = true }) {
                Icon(
                    painterResource(id = R.drawable.etl_keywords),
                    contentDescription = "Reprocess Keywords",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(25.dp)
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
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
            if (isReprocessing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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
