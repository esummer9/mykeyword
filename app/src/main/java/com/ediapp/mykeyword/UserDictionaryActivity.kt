package com.ediapp.mykeyword

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme

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
                var userDics by remember { mutableStateOf(dbHelper.getAllUserDics()) }
                var showAddDialog by remember { mutableStateOf(false) }
                var showEditDialog by remember { mutableStateOf<UserDic?>(null) }
                var showDeleteConfirmDialog by remember { mutableStateOf<UserDic?>(null) }
                var expandedMenuUserDic by remember { mutableStateOf<UserDic?>(null) }
                var fabMenuExpanded by remember { mutableStateOf(false) }

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
                                        val allDics = dbHelper.getAllUserDics()
                                        allDics.forEach {
                                            WriteUserDic(context, UserDicItem(it.keyword, it.pos))
                                        }
                                        Toast.makeText(context, "사용자 사전을 재생성했습니다.", Toast.LENGTH_SHORT).show()
                                        fabMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    LazyColumn(modifier = Modifier.padding(innerPadding)) {
                        items(userDics) { userDic ->
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

                    if (showAddDialog) {
                        EditKeywordDialog(
                            onDismiss = { showAddDialog = false },
                            onConfirm = { keyword, pos ->
                                if (dbHelper.addOrUpdateUserDic(0L, keyword, pos) == -1L) {
                                    Toast.makeText(context, "이미 존재하는 키워드입니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    userDics = dbHelper.getAllUserDics()
                                    showAddDialog = false
                                }
                                WriteUserDic(context, UserDicItem(keyword, pos))
                            }
                        )
                    }

                    showEditDialog?.let { userDic ->
                        EditKeywordDialog(
                            userDic = userDic,
                            onDismiss = { showEditDialog = null },
                            onConfirm = { keyword, pos ->
                                if (dbHelper.addOrUpdateUserDic(userDic.id, keyword, pos) == -1L) {
                                    Toast.makeText(context, "이미 존재하는 키워드입니다.", Toast.LENGTH_SHORT).show()
                                } else {
                                    userDics = dbHelper.getAllUserDics()

                                    WriteUserDic(context, UserDicItem(keyword, pos))
                                    showEditDialog = null
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
                                    dbHelper.deleteUserDic(userDic.id)
                                    userDics = dbHelper.getAllUserDics()
                                    showDeleteConfirmDialog = null
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditKeywordDialog(
    userDic: UserDic? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var keyword by remember { mutableStateOf(userDic?.keyword ?: "") }
    var expanded by remember { mutableStateOf(false) }
    val posDisplayMap = remember {
        linkedMapOf(
            "NNG" to "일반명사",
            "NNP" to "고유명사",
            "NNB" to "의존명사",
            "NP" to "대명사",
            "NR" to "수사",
            "NA" to "불능"
        )
    }
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
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
            Button(onClick = { onConfirm(keyword, selectedPos) }) {
                Text(if (userDic == null) "추가" else "수정")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
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
    val posDisplayMap = remember {
        linkedMapOf(
            "NNG" to "일반명사",
            "NNP" to "고유명사",
            "NNB" to "의존명사",
            "NP" to "대명사",
            "NR" to "수사",
            "NA" to "불능"
        )
    }
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 8.dp)
                .combinedClickable(
                    onClick = { },
                    onLongClick = onLongClick
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text(text = userDic.keyword, modifier = Modifier.weight(1f))
                Text(text = posDisplayMap[userDic.pos] ?: userDic.pos)
            }
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = onDismissMenu
        ) {
            DropdownMenuItem(
                text = { Text("수정") },
                onClick = onEdit
            )
            DropdownMenuItem(
                text = { Text("삭제") },
                onClick = onDelete
            )
        }
    }
}
