package com.ediapp.mykeyword

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.ediapp.mykeyword.service.NotificationService
import com.ediapp.mykeyword.ui.home.HomeScreen
import com.ediapp.mykeyword.ui.keyword.KeywordScreen
import com.ediapp.mykeyword.ui.notey.NoteyScreen
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme
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

        val myApp: MyApplication = applicationContext as MyApplication
        val analyzer = myApp.morphemeAnalyzer

        // lifecycleScope를 사용하여 코루틴을 실행합니다.
        lifecycleScope.launch {
            val textToAnalyze = "안녕하세요, 형태소 분석 예시입니다."
            val result = analyzer.analyzeText(textToAnalyze)

            // 결과 확인 (예: ["안녕/NNG", "하/XSV", "시/EP", "어요/EF", ",/SP", "형태소/NNG", ...])
            Log.d("MorphemeResult", result.toString())
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var menuExpanded by remember { mutableStateOf(false) }

    val addMemoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentDestination = AppDestinations.NOTEY
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
                                            text = { Text("어바웃") },
                                            onClick = {
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        AboutActivity::class.java
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

                                        DropdownMenuItem(
                                            text = { Text("오픈소스") },
                                            onClick = {
                                                context.startActivity(
                                                    Intent(
                                                        context,
                                                        OpenSourceActivity::class.java
                                                    )
                                                )
                                                menuExpanded = false
                                            }
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = {
                                    val intent = Intent(context, EditMemoActivity::class.java)
                                    intent.putExtra("MEMO_ID", -1L) // -1L for new memo
                                    addMemoLauncher.launch(intent)
                                }) {
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
                        AppDestinations.HOME -> HomeScreen()
                        AppDestinations.NOTEY -> NoteyScreen()
                        AppDestinations.KEYWORD -> KeywordScreen()
                    }
                }
            }
        }
    }

}

enum class AppDestinations(
    val label: String,
    val icon: Any,
) {
    HOME("Home", R.drawable.home),
    NOTEY("Notey", R.drawable.memo),
    KEYWORD("Keyword", R.drawable.keyword),
}
