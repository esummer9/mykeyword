package com.ediapp.mykeyword

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// 초성 리스트
private val CHOSUNG = charArrayOf(
    'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
)
//
//private val posDisplayMap = mapOf(
//    "NNP" to "고유명사",
//    "NNG" to "일반명사",
//    "VA" to "형용사",
//    "SL" to "외국어"
//)

// 주어진 단어의 첫 글자의 초성을 반환하는 함수
private fun getChosung(word: String): Char? {
    if (word.isEmpty()) return null
    val firstChar = word[0]
    return if (firstChar in '가'..'힣') {
        val unicode = firstChar.code - 0xAC00
        val chosungIndex = unicode / (21 * 28)
        CHOSUNG[chosungIndex]
    } else if (firstChar in 'ㄱ'..'ㅎ') {
        firstChar
    } else {
        null // 한글이 아닌 경우
    }
}
class UserDictionaryActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyKeywordTheme {
                val view = LocalView.current
                val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
                if (!view.isInEditMode) {
                    SideEffect {
                        val window = (view.context as Activity).window
                        window.statusBarColor = primaryContainerColor.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
                    }
                }

                val context = LocalContext.current
                val dbHelper = remember { DatabaseHelper.getInstance(context) }
                val scope = rememberCoroutineScope()
                var userDics by remember { mutableStateOf(emptyList<UserDic>()) }
                var showAddDialog by remember { mutableStateOf(false) }
                var showEditDialog by remember { mutableStateOf<UserDic?>(null) }
                var showDeleteConfirmDialog by remember { mutableStateOf<UserDic?>(null) }
                var expandedMenuUserDic by remember { mutableStateOf<UserDic?>(null) }
                var fabMenuExpanded by remember { mutableStateOf(false) }

                // 필터링 UI 관련 상태
                var selectedChosung by remember { mutableStateOf("전체") }
                val chosungButtons = remember {
                    listOf("전체", "ㄱ", "ㄴ", "ㄷ", 'ㄹ', "ㅁ", "ㅂ", "ㅅ", "ㅇ", "ㅈ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ")
                }
                val chosungMap = remember {
                    mapOf(
                        'ㄱ' to listOf('ㄱ', 'ㄲ'),
                        'ㄷ' to listOf('ㄷ', 'ㄸ'),
                        'ㅂ' to listOf('ㅂ', 'ㅃ'),
                        'ㅅ' to listOf('ㅅ', 'ㅆ'),
                        'ㅈ' to listOf('ㅈ', 'ㅉ')
                    )
                }

                fun refreshUserDics() {
                    scope.launch(Dispatchers.IO) {
                        val updatedDics = dbHelper.getAllUserDics()
                        withContext(Dispatchers.Main) {
                            userDics = updatedDics
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    refreshUserDics()
                }

                val filteredUserDics = remember(userDics, selectedChosung) {
                    if (selectedChosung == "전체") {
                        userDics
                    } else {
                        val selectedChar = selectedChosung.first()
                        val targetChars = chosungMap[selectedChar] ?: listOf(selectedChar)
                        userDics.filter {
                            val chosung = getChosung(it.keyword)
                            chosung != null && chosung in targetChars
                        }
                    }
                }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                titleContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            title = { Text("사용자 사전") },
                            navigationIcon = {
                                IconButton(onClick = { (context as? Activity)?.finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        Box {
                            FloatingActionButton(onClick = { fabMenuExpanded = !fabMenuExpanded }) {
                                Icon(Icons.Default.Add, contentDescription = "Add Keyword")
                            }
                            DropdownMenu(
                                expanded = fabMenuExpanded,
                                onDismissRequest = { fabMenuExpanded = false },
                                modifier = Modifier.align(Alignment.BottomEnd)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("추가") },
                                    onClick = {
                                        showAddDialog = true
                                        fabMenuExpanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("재생성") },
                                    onClick = {
                                        scope.launch {
                                            dbHelper.writeUserDictionaryToFile(context)
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "사용자 사전을 재생성했습니다.", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        fabMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(chosungButtons) { chosung ->
                                Button(
                                    onClick = { selectedChosung = chosung.toString() },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selectedChosung == chosung.toString()) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                ) {
                                    Text(text = chosung.toString())
                                }
                            }
                        }
                        LazyColumn {
                            items(filteredUserDics, key = { it.id }) { userDic ->
                                UserDicItem(
                                    userDic = userDic,
                                    isMenuExpanded = expandedMenuUserDic == userDic,
                                    onLongClick = { expandedMenuUserDic = userDic },
                                    onDismissMenu = { expandedMenuUserDic = null },
                                    onEdit = {
                                        showEditDialog = userDic
                                        expandedMenuUserDic = null
                                    },
                                    onDelete = {
                                        showDeleteConfirmDialog = userDic
                                        expandedMenuUserDic = null
                                    }
                                )
                            }
                        }
                    }

                    if (showAddDialog) {
                        EditKeywordDialog(
                            onDismiss = { showAddDialog = false },
                            onConfirm = { keyword, pos ->
                                scope.launch(Dispatchers.IO) {
                                    if (dbHelper.addOrUpdateUserDic(0L, keyword, pos) == -1L) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "이미 존재하는 키워드입니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        dbHelper.writeUserDictionaryToFile(context)
                                        withContext(Dispatchers.Main) {
                                            refreshUserDics()
                                            showAddDialog = false
                                        }
                                    }
                                }
                            }
                        )
                    }

                    showEditDialog?.let { userDic ->
                        EditKeywordDialog(
                            userDic = userDic,
                            onDismiss = { showEditDialog = null },
                            onConfirm = { keyword, pos ->
                                scope.launch(Dispatchers.IO) {
                                    if (dbHelper.addOrUpdateUserDic(userDic.id, keyword, pos) == -1L) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "이미 존재하는 키워드입니다.", Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        dbHelper.writeUserDictionaryToFile(context)
                                        withContext(Dispatchers.Main) {
                                            refreshUserDics()
                                            showEditDialog = null
                                        }
                                    }
                                }
                            }
                        )
                    }

                    showDeleteConfirmDialog?.let { userDic ->
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirmDialog = null },
                            title = { Text("삭제 확인") },
                            text = { Text("'${userDic.keyword}'을(를) 삭제하시겠습니까?") },
                            confirmButton = {
                                Button(onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        dbHelper.deleteUserDic(userDic.id)
                                        dbHelper.writeUserDictionaryToFile(context)
                                        withContext(Dispatchers.Main) {
                                            refreshUserDics()
                                            showDeleteConfirmDialog = null
                                        }
                                    }
                                }) {
                                    Text("삭제")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDeleteConfirmDialog = null }) {
                                    Text("취소")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserDicItem(
    userDic: UserDic,
    isMenuExpanded: Boolean,
    onLongClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .combinedClickable(
                    onClick = { /* No action on simple click */ },
                    onLongClick = onLongClick
                )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = userDic.keyword, modifier = Modifier.weight(1f))
                Text(text = posDisplayMap[userDic.pos] ?: userDic.pos)
            }
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(text = { Text("수정") }, onClick = onEdit)
            DropdownMenuItem(text = { Text("삭제") }, onClick = onDelete)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable fun EditKeywordDialog(
    userDic: UserDic? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var keyword by remember { mutableStateOf(userDic?.keyword ?: "") }
    var expanded by remember { mutableStateOf(false) }

    val posOptions = remember { posDisplayMap.keys.toList() }
    var selectedPos by remember { mutableStateOf(userDic?.pos ?: posOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (userDic == null) "키워드 추가" else "키워드 수정") },
        text = {
            Column {
                TextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("키워드") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = posDisplayMap[selectedPos] ?: selectedPos,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("품사") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        posOptions.forEach { pos ->
                            DropdownMenuItem(
                                text = { Text(posDisplayMap[pos] ?: pos) },
                                onClick = {
                                    selectedPos = pos
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (keyword.isNotBlank()) {
                    onConfirm(keyword, selectedPos)
                }
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}
