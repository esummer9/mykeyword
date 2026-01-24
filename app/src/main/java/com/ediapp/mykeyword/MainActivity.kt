package com.ediapp.mykeyword

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Spacer
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.ediapp.mykeyword.service.NotificationService
import com.ediapp.mykeyword.ui.home.HomeScreen
import com.ediapp.mykeyword.ui.keyword.KeywordScreen
import com.ediapp.mykeyword.ui.notey.NoteyScreen
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Start the service.
            startNotificationService()
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied. At the
            // same time, respect the user's decision. Don't link to system
            // settings in an effort to convince the user to change their
            // decision.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            MyKeywordTheme {
                MyKeywordApp()
            }
        }
    }


    private fun startNotificationService() {
        val serviceIntent = Intent(this, NotificationService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
                startNotificationService()
            } else {
                // Request permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // No permission needed for older versions
            startNotificationService()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun MyKeywordApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val context = LocalContext.current
    var noteyRefreshTrigger by remember { mutableStateOf(0) }
    var searchVisible by remember { mutableStateOf(false) }
    var showAddMemoDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dbHelper = remember { DatabaseHelper.getInstance(context) }

    var menuExpanded by remember { mutableStateOf(false) }

    var showReprocessDialog by remember { mutableStateOf(false) }
    var isReprocessing by remember { mutableStateOf(false) }
    var keywordRefreshKey by remember { mutableStateOf(0) }
    val analyzer = remember { KomoranAnalyzer(context) }

    LaunchedEffect(analyzer) {
        analyzer.initialize()
    }

    if (showReprocessDialog) {
        AlertDialog(
            onDismissRequest = { showReprocessDialog = false },
            title = { Text("키워드 재처리") },
            text = { Text("메모의 형태소를 분석합니다") },
            confirmButton = {
                Button(
                    onClick = {
                        showReprocessDialog = false
                        scope.launch {
                            isReprocessing = true
                            dbHelper.writeUserDictionaryToFile(context)
                            analyzer.reloadUserDic()
                            dbHelper.reprocessKeywords(analyzer)
                            kotlinx.coroutines.delay(2000)
                            isReprocessing = false
                            keywordRefreshKey++
                        }
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                Button(onClick = { showReprocessDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    if (isReprocessing) {
        ProcessingDialog(message = "키워드를 재처리하는 중입니다...")
    }

    val view = LocalView.current
    DisposableEffect(view, currentDestination) {
        val listener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus && currentDestination == AppDestinations.NOTEY) {
                noteyRefreshTrigger++
                Log.d("noteyRefreshTrigger", "Window focus gained: ${noteyRefreshTrigger}")
            }
        }
        view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
        onDispose {
            view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        }
    }

    if (showAddMemoDialog) {
        AddMemoDialog(
            onDismiss = { showAddMemoDialog = false },
            onConfirm = { memoText ->
                scope.launch(Dispatchers.IO) {
                    val newId = dbHelper.addMemo(
                        title = memoText,
                        mean = null,
                        url = null,
                        address = null,
                        regDate = System.currentTimeMillis()
                    )
                    dbHelper.addKeywords(analyzer, memoText, newId)
                }
                showAddMemoDialog = false
                noteyRefreshTrigger++
                currentDestination = AppDestinations.NOTEY
            }
        )
    }

    LaunchedEffect(currentDestination) {
        if (currentDestination != AppDestinations.NOTEY) {
            searchVisible = false
        }
    }

    ModalNavigationDrawer(
        drawerContent = {},
        drawerState = drawerState
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach { destination ->
                    item(
                        icon = {
                            when (val icon = destination.icon) {
                                is ImageVector -> Icon(
                                    icon,
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(25.dp)
                                )

                                is Int -> Icon(
                                    painterResource(id = icon),
                                    contentDescription = destination.label,
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                        },
                        label = { Text(destination.label) },
                        selected = destination == currentDestination,
                        onClick = { currentDestination = destination }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    Column {
                        TopAppBar(
                            title = { Text(text = "My Keyword") },
                            navigationIcon = {
                                Box {
                                    IconButton(onClick = { menuExpanded = true }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                                    }
                                    DropdownMenu(
                                        expanded = menuExpanded,
                                        onDismissRequest = { menuExpanded = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("사용자 사전") },
                                            onClick = {
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        UserDictionaryActivity::class.java
                                                    )
                                                )
                                                menuExpanded = false
                                            }
                                        )

                                        DropdownMenuItem(
                                            text = { Text("도움말") },
                                            onClick = {
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        HelpActivity::class.java
                                                    )
                                                )
                                                menuExpanded = false
                                            }
                                        )

//                                        DropdownMenuItem(
//                                            text = { Text("주고/받기") },
//                                            onClick = {
//                                                context.startActivity(
//                                                    Intent(
//                                                        context,
//                                                        ExchangeActivity::class.java
//                                                    )
//                                                )
//                                                menuExpanded = false
//                                            }
//                                        )
                                    }
                                }
                            },
                            actions = {
                                if (currentDestination == AppDestinations.KEYWORD) {
                                    IconButton(onClick = { showReprocessDialog = true }) {
                                        Icon(
                                            painterResource(id = R.drawable.etl_keywords),
                                            contentDescription = "Reprocess Keywords",
                                            tint = Color.Unspecified,
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }
                                }
                                if (currentDestination == AppDestinations.NOTEY) {
                                    IconButton(onClick = { searchVisible = !searchVisible }) {
                                        Icon(
                                            painterResource(id = R.drawable.glass),
                                            contentDescription = "Search memos",
                                            modifier = Modifier.size(25.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                IconButton(onClick = { showAddMemoDialog = true }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Memo")
                                }
                            }
                        )
                        Divider(color = Color.Gray, thickness = 1.dp)
                    }
                }
            ) { scaffoldPadding ->
                Box(modifier = Modifier.padding(scaffoldPadding)) {
                    when (currentDestination) {
                        AppDestinations.HOME -> HomeScreen(onNavigate = { currentDestination = it })
                        AppDestinations.NOTEY -> NoteyScreen(
                            refreshTrigger = noteyRefreshTrigger,
                            searchVisible = searchVisible
                        )
                        AppDestinations.KEYWORD -> KeywordScreen(refreshKey = keywordRefreshKey)
                    }
                }
            }
        }
    }
}

@Composable
fun ProcessingDialog(message: String) {
    AlertDialog(
        onDismissRequest = { /* 사용자가 대화 상자를 닫을 수 없도록 함 */ },
        title = { Text(text = "처리 중") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = message)
            }
        },
        confirmButton = {}
    )
}


@Composable
fun AddMemoDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("메모 추가") },
        text = {
            TextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("메모 내용") }
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (text.isNotBlank()) {
                        onConfirm(text)
                    }
                }
            ) {
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

enum class AppDestinations(
    val label: String,
    val icon: Any,
) {
    HOME("Home", R.drawable.home),
    NOTEY("Notey", R.drawable.memo),
    KEYWORD("Keyword", R.drawable.keyword),
}