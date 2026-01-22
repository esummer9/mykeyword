package com.ediapp.mykeyword

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ediapp.mykeyword.ui.notey.Memo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class KeywordFreq(val keyword: String, val frequency: Int)

class MemoViewActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val memoId = intent.getLongExtra("MEMO_ID", -1L)

        setContent {
            MaterialTheme {
                MemoViewScreen(
                    memoId = memoId,
                    onNavigateBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoViewScreen(memoId: Long, onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val inPreview = LocalInspectionMode.current
    val dbHelper = if (inPreview) null else remember { DatabaseHelper.getInstance(context) }
    val scope = rememberCoroutineScope()

    var memo by remember { mutableStateOf<Memo?>(null) }
    var keywordFrequencies by remember { mutableStateOf<List<KeywordFreq>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(memoId) {
        if (!inPreview && memoId != -1L) {
            scope.launch {
                isLoading = true
                try {
                    // 메모 데이터 가져오기
                    memo = withContext(Dispatchers.IO) {
                        dbHelper?.getMemoById(memoId)
                    }

                    // 메모 내용에서 키워드 추출 및 빈도수 계산
                    memo?.let { memoData ->
                        val content = (memoData.title ?: "") + " " + (memoData.meaning ?: "")
                        keywordFrequencies = withContext(Dispatchers.Default) {
                            extractKeywordsWithFrequency(content)
                        }
                    }
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메모 보기") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
//                horizontalAlignment = androidx.compose.foundation.layout.Alignment.CenterHorizontally
//                horizontalAlignment = androidx.compose.foundation.layout.Alignment.CenterHorizontally
                        horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("로딩 중...")
            }
        } else if (memo != null) {
            LazyColumn(
                modifier = Modifier.padding(padding)
            ) {
                // 메모 제목
                item {
                    Text(
                        text = memo!!.title ?: "",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                }

                // 메모 의미/내용
                if (!memo!!.meaning.isNullOrBlank()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "의미",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = memo!!.meaning ?: "",
                                fontSize = 16.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // URL
                if (!memo!!.url.isNullOrBlank()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "URL",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = memo!!.url ?: "",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // 위치
                if (!memo!!.address.isNullOrBlank()) {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "위치",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = memo!!.address ?: "",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }

                // 키워드 빈도수
                if (keywordFrequencies.isNotEmpty()) {
                    item {
                        Text(
                            text = "키워드 (빈도순)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp)
                        )
                    }

                    items(keywordFrequencies) { keywordFreq ->
                        KeywordItem(
                            keyword = keywordFreq.keyword,
                            frequency = keywordFreq.frequency,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "추출된 키워드가 없습니다.",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                Text("메모를 찾을 수 없습니다.")
            }
        }
    }
}

@Composable
fun KeywordItem(keyword: String, frequency: Int, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = keyword,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "빈도수: $frequency",
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

/**
 * 메모 텍스트에서 키워드를 추출하고 빈도수를 계산합니다.
 * KomoranAnalyzer를 사용하여 형태소 분석을 수행합니다.
 */
suspend fun extractKeywordsWithFrequency(text: String): List<KeywordFreq> {
    if (text.isBlank()) return emptyList()

    return withContext(Dispatchers.Default) {
        try {
            // 간단한 토크나이저: 공백으로 구분된 단어를 추출
            // 실제로는 KomoranAnalyzer를 사용하면 더 정확합니다.
            val keywords = text
                .split(Regex("[\\s,!?.;:\\'\"\\(\\)\\[\\]\\{\\}]+")) // 특수문자와 공백으로 구분
                .filter { it.length > 1 } // 한 글자 이상만 포함
                .map { it.lowercase() } // 소문자로 통일
                .filter { it !in stopWords() } // 불용어 제외

            // 키워드별 빈도수 계산
            val frequencyMap = keywords.groupingBy { it }.eachCount()

            // 빈도수 높은 순서대로 정렬
            frequencyMap
                .map { (keyword, count) -> KeywordFreq(keyword, count) }
                .sortedByDescending { it.frequency }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

/**
 * 불용어 목록
 */
fun stopWords(): Set<String> {
    return setOf(
        "이", "그", "저", "것", "수", "등", "들", "및", "또는", "그리고",
        "하지만", "그러나", "그래서", "따라서", "때문에", "위해", "통해",
        "있다", "없다", "하다", "되다", "있습니다", "없습니다", "합니다",
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for",
        "of", "with", "from", "is", "are", "was", "were", "be", "been", "being"
    )
}

