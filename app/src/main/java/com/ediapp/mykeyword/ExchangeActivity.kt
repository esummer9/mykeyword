package com.ediapp.mykeyword

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.ediapp.mykeyword.DatabaseHelper
import com.ediapp.mykeyword.UserDic
import com.ediapp.mykeyword.ui.notey.Memo
import com.ediapp.mykeyword.ui.theme.MyKeywordTheme
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import androidx.compose.material3.TopAppBar
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
class ExchangeActivity : ComponentActivity() {

    private val gson = Gson()
    private val PICK_FILE_REQUEST_CODE = 1001

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                processImportFile(uri)
            }
        }
    }

    private var showProcessingDialog by mutableStateOf(false)
    private var processingMessage by mutableStateOf("")

    private var showResultDialog by mutableStateOf(false)
    private var resultMessage by mutableStateOf("")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyKeywordTheme {
                val context = LocalContext.current
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("데이터 주고/받기") },
                            navigationIcon = {
                                IconButton(onClick = { (context as? Activity)?.finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Button(onClick = { exportNoteyData() }) {
                            Text("데이터 내보내기 (JSON)")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { importNoteyData() }) {
                            Text("데이터 가져오기 (JSON)")
                        }
                    }
                }

                if (showProcessingDialog) {
                    ProcessingBackupDialog(message = processingMessage)
                }

                if (showResultDialog) {
                    ResultDialog(
                        message = resultMessage,
                        onDismiss = { showResultDialog = false }
                    )
                }
            }
        }
    }

    private fun exportNoteyData() {
        showProcessingDialog = true
        processingMessage = "데이터 내보내기 준비 중..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbHelper = DatabaseHelper.getInstance(this@ExchangeActivity)
                val memosToExport = dbHelper.getAllMemos("notey")
                val userDictToExport = dbHelper.getAllUserDics()
                val exportData = ExportData(memosToExport, userDictToExport)
                val json = gson.toJson(exportData)

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = dateFormat.format(System.currentTimeMillis())
                val filename = "mykeyword_backup_${currentDate}.json"
                val file = File(cacheDir, filename)
                file.writeText(json)

                val uri = FileProvider.getUriForFile(this@ExchangeActivity, "${packageName}.provider", file)

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                withContext(Dispatchers.Main) {
                    showProcessingDialog = false
                    resultMessage = "데이터 내보내기 성공!"
                    showResultDialog = true
                    startActivity(Intent.createChooser(shareIntent, "MyKeyword 데이터 공유"))
                }

            } catch (e: Exception) {
                Log.e("ExchangeActivity", "데이터 내보내기 오류: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    processingMessage = "내보내기 실패: ${e.message}"
                    showProcessingDialog = false
                    resultMessage = "내보내기 실패: ${e.message}"
                    showResultDialog = true
                }
            }
        }
    }

    private fun importNoteyData() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        importLauncher.launch(Intent.createChooser(intent, "MyKeyword 데이터 파일 선택"))
    }

    private fun processImportFile(uri: Uri) {
        showProcessingDialog = true
        processingMessage = "데이터 가져오기 및 병합 중..."
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val json = inputStream?.bufferedReader().use { it?.readText() } ?: throw Exception("파일을 읽을 수 없습니다.")

                val importData = gson.fromJson(json, ExportData::class.java)

                val dbHelper = DatabaseHelper.getInstance(this@ExchangeActivity)

                // Duplicate checking logic similar to previous P2P implementation
                val existingMemoTitles = dbHelper.getAllMemos("notey").mapNotNull { it.title }.toSet()
                val existingUserKeywords = dbHelper.getAllUserDics().map { it.keyword }.toSet()

                var newMemosAdded = 0
                var newWordsAdded = 0

                importData.memos.forEach { memo ->
                    if (memo.title !in existingMemoTitles) {
                        dbHelper.addMemo(
                            title = memo.title?:"",
                            mean = memo.meaning,
                            url = memo.url,
                            address = memo.address,
                            regDate = memo.regDate?:0
                        )
                        newMemosAdded++
                    }
                }

                importData.userDictionary.forEach { userDic ->
                    if (userDic.keyword !in existingUserKeywords) {
                        dbHelper.addOrUpdateUserDic(userDic.id, userDic.keyword, "NNP")
                        newWordsAdded++
                    }
                }

                withContext(Dispatchers.Main) {
                    processingMessage = "가져오기 및 병합 완료! (새 메모: $newMemosAdded, 새 단어: $newWordsAdded)"
                    showProcessingDialog = false
                    resultMessage = "가져오기 및 병합 완료! (새 메모: $newMemosAdded, 새 단어: $newWordsAdded)"
                    showResultDialog = true
                }

            } catch (e: Exception) {
                Log.e("ExchangeActivity", "데이터 가져오기 오류: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    processingMessage = "가져오기 실패: ${e.message}"
                    showProcessingDialog = false
                    resultMessage = "가져오기 실패: ${e.message}"
                    showResultDialog = true
                }
            }
        }
    }
}

// Data class to encapsulate exported data
data class ExportData(
    val memos: List<Memo>,
    val userDictionary: List<UserDic>
)

@Composable
fun ProcessingBackupDialog(message: String) {
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
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = message)
            }
        },
        confirmButton = {}
    )
}

@Composable
fun ResultDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "결과") },
        text = { Text(text = message) },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("확인")
            }
        }
    )
}
