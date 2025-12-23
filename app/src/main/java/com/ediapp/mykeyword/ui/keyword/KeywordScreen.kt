package com.ediapp.mykeyword.ui.keyword

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.ediapp.mykeyword.R
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun KeywordScreen() {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()

    var keywords by remember { mutableStateOf<List<Keyword>>(emptyList()) }
    var selectedPeriod by remember { mutableStateOf("전체") }
    var refreshKey by remember { mutableStateOf(0) }
    var showReprocessDialog by remember { mutableStateOf(false) }
    var isReprocessing by remember { mutableStateOf(false) }

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
                    Button(onClick = { getKeywords("2일") }) {
                        Text("2일")
                    }
                    Button(onClick = { getKeywords("1주") }) {
                        Text("1주")
                    }
                    Button(onClick = { getKeywords("1개월") }) {
                        Text("1개월")
                    }
                    Button(onClick = { getKeywords("전체") }) {
                        Text("전체")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "'${selectedPeriod}' 검색 결과")

                LazyColumn {
                    items(keywords) { keyword ->
                        KeywordItem(keyword)
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

@Composable
fun KeywordItem(keyword: Keyword) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = keyword.keyword, modifier = Modifier.weight(1f))
            Text(text = "${keyword.count}건")
        }
    }
}
