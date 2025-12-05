package com.ediapp.mykeyword

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.ediapp.mykeyword.ui.favorites.FavoritesScreen
import com.ediapp.mykeyword.ui.home.HomeScreen
import com.ediapp.mykeyword.ui.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyKeywordTheme {
                MyKeywordApp()
            }
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

    ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("About") },
                    selected = false,
                    onClick = { context.startActivity(Intent(context, AboutActivity::class.java)) },
                    icon = {Icon(
                        painter = painterResource(R.drawable.about)
                        , contentDescription = "About"
                        , modifier = Modifier.size(24.dp))}
                )
                NavigationDrawerItem(
                    label = { Text("도움말") },
                    selected = false,
                    onClick = { context.startActivity(Intent(context, HelpActivity::class.java)) },
                    icon = { Icon(
                        painter = painterResource(R.drawable.guidebook)
                        , contentDescription = "도움말"
                        , modifier = Modifier.size(24.dp))
                   }
                )
                NavigationDrawerItem(
                    label = { Text("오픈소스") },
                    selected = false,
                    onClick = { context.startActivity(Intent(context, OpenSourceActivity::class.java)) },
                    icon = {Icon(
                        painter = painterResource(R.drawable.open_source)
                        , contentDescription = "오픈소스"
                        , modifier = Modifier.size(24.dp))
                    }
                )
            }
        },
        drawerState = drawerState
    ) {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.label
                            )
                        },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it }
                    )
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = { Text(text = currentDestination.label) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                // TODO: Replace with a when statement to show the correct screen
                // based on the currentDestination
                when (currentDestination) {
                    AppDestinations.HOME -> HomeScreen()
                    AppDestinations.FAVORITES -> FavoritesScreen()
                    AppDestinations.PROFILE -> ProfileScreen()
                }
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
) {
    HOME("Home", Icons.Default.Home),
    FAVORITES("Favorites", Icons.Default.Favorite),
    PROFILE("Profile", Icons.Default.AccountBox),
}
