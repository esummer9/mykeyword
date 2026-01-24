package com.ediapp.mykeyword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.ui.notey.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class KeywordMemosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyword = intent.getStringExtra("KEYWORD") ?: ""
        setContent {
            MaterialTheme {
                KeywordMemosScreen(keyword = keyword) {
                    finish()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeywordMemosScreen(keyword: String, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val dbHelper = remember { DatabaseHelper.getInstance(context) }
    var memos by remember { mutableStateOf<List<Memo>>(emptyList()) }

    LaunchedEffect(keyword) {
        withContext(Dispatchers.IO) {
            val memosFromDb = dbHelper.getMemosWithPagination(
                category = "notey",
                searchQuery = keyword,
                startDate = null,
                limit = 100, // Load first 100 items
                offset = 0
            )
            memos = memosFromDb
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = keyword) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            itemsIndexed(memos) { index, memo ->
                val prevMemo = memos.getOrNull(index + 1)
                MemoItemWithTimeDifference(memo = memo, prevMemo = prevMemo)
            }
        }
    }
}

@Composable
fun MemoItemWithTimeDifference(memo: Memo, prevMemo: Memo?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = memo.title ?: "", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = formatFullDate(memo.regDate))
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text(text = "(${formatDaysAgo(memo.regDate)})")
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatTimeDifference(memo.regDate, prevMemo?.regDate),
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

private fun formatFullDate(timeInMillis: Long?): String {
    if (timeInMillis == null) return ""
    val date = Date(timeInMillis)
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(date)
}

private fun formatDaysAgo(timeInMillis: Long?): String {
    if (timeInMillis == null) return ""
    val diff = System.currentTimeMillis() - timeInMillis
    val days = TimeUnit.MILLISECONDS.toDays(diff)
    return when {
        days > 0 -> "$days" + "일 전"
        else -> "오늘"
    }
}

private fun formatTimeDifference(currentTime: Long?, previousTime: Long?): String {
    if (currentTime == null || previousTime == null) {
        return ""
    }
    val diff = currentTime - previousTime
    if (diff < 0) return ""

    val days = TimeUnit.MILLISECONDS.toDays(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60

    val sb = StringBuilder()
    if (days > 0) sb.append("+${days}일 ")
    if (hours > 0) sb.append("+${hours}시간 ")
    if (minutes > 0) sb.append("+${minutes}분")

    return if(sb.isNotEmpty()) sb.toString().trim() else {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        if (seconds > 0) "+${seconds}초" else ""
    }
}
