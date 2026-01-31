package com.ediapp.mykeyword.ui.home

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
//import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.AppDestinations
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.Keyword
import com.ediapp.mykeyword.KomoranAnalyzer
import com.ediapp.mykeyword.R
import com.ediapp.mykeyword.ui.notey.Memo
import com.ediapp.mykeyword.utils.formatRegDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun HomeScreen(
    onNavigate: (AppDestinations) -> Unit,
) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()
    val analyzer = remember { KomoranAnalyzer(context) }

    var newMemoText by remember { mutableStateOf("") }
    var recentMemos by remember { mutableStateOf<List<Memo>>(emptyList()) }
    var trendingKeywords by remember { mutableStateOf<List<Keyword>>(emptyList()) }
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(key1 = refreshTrigger) {
        withContext(Dispatchers.IO) {
            recentMemos = dbHelper.getRecentMemos(limit = 3)
            trendingKeywords = dbHelper.getTrendingKeywords(limit = 15)
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 새 메모 작성 필드 + 버튼
        Text("새 메모", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = newMemoText,
                onValueChange = { newMemoText = it },
                placeholder = { Text("빠른메모 입력...") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                modifier = Modifier.weight(1f),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                )
            )
            IconButton(onClick = {
                if (newMemoText.isNotBlank()) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            val newId = dbHelper.addMemo(
                                title = newMemoText,
                                mean = null,
                                url = null,
                                address = null,
                                regDate = System.currentTimeMillis()
                            )
                            recentMemos = dbHelper.getRecentMemos(limit = 3) // "최근메모"를 즉시 업데이트
                        }
                        newMemoText = ""
                        refreshTrigger++
                    }
                }
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.check),
                    contentDescription = "Save Memo",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }


        Spacer(modifier = Modifier.height(24.dp))

        // 2. 최근 메모 목록
        Text("최근 메모", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        if (recentMemos.isEmpty()) {
            Text("아직 작성된 메모가 없습니다.", style = MaterialTheme.typography.bodyMedium)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recentMemos.forEach { memo ->
                    MemoItem(memo = memo, onClick = { onNavigate(AppDestinations.NOTEY) })
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 인기 키워드
        Text("인기 키워드", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        if (trendingKeywords.isEmpty()) {
            Text("분석된 키워드가 없습니다.", style = MaterialTheme.typography.bodyMedium)
        } else {
            KeywordCloud(keywords = trendingKeywords)
        }
    }
}

@Composable
fun MemoItem(memo: Memo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = memo.title ?: "제목 없음",
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatRegDate(memo.regDate),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordCloud(keywords: List<Keyword>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keywords.forEach { keyword ->
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            ) {
                Text(
                    text = keyword.keyword,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
        }
    }