package com.ediapp.mykeyword.ui.notey

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ediapp.mykeyword.DatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MemosViewModel(private val dbHelper: DatabaseHelper) : ViewModel() {

    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    private var currentPage = 0
    private val pageSize = 20
    private var isLoading = false
    private var allMemosLoaded = false

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPeriod = MutableStateFlow("1주")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    init {
        loadMemos()
    }

    fun loadMemos() {
        if (isLoading || allMemosLoaded) return

        isLoading = true
        viewModelScope.launch {
            val newMemos = withContext(Dispatchers.IO) {
                val calendar = Calendar.getInstance()
                val startDate = when (_selectedPeriod.value) {
                    "2일" -> calendar.apply { add(Calendar.DATE, -2) }.timeInMillis
                    "1주" -> calendar.apply { add(Calendar.WEEK_OF_YEAR, -1) }.timeInMillis
                    "1개월" -> calendar.apply { add(Calendar.MONTH, -1) }.timeInMillis
                    else -> null
                }

                dbHelper.getMemosWithPagination(
                    category = "notey",
                    searchQuery = _searchQuery.value.ifBlank { null },
                    startDate = startDate,
                    limit = pageSize,
                    offset = currentPage * pageSize
                )
            }

            if (newMemos.isNotEmpty()) {
                _memos.value = if (currentPage == 0) newMemos else _memos.value + newMemos
                currentPage++
            } else {
                allMemosLoaded = true
            }
            isLoading = false
        }
    }

    fun refreshMemos() {
        currentPage = 0
        allMemosLoaded = false
        _memos.value = emptyList()
        loadMemos()
    }

    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
        refreshMemos()
    }

    fun onPeriodChanged(newPeriod: String) {
        _selectedPeriod.value = newPeriod
        refreshMemos()
    }

    fun addQuickMemo(text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.addMemo(
                title = text,
                mean = null,
                url = null,
                address = null,
                regDate = System.currentTimeMillis()
            )
            withContext(Dispatchers.Main) {
                refreshMemos()
            }
        }
    }

    fun duplicateMemo(memoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.duplicateMemo(memoId)
            withContext(Dispatchers.Main) {
                refreshMemos()
            }
        }
    }

    fun deleteMemo(memoId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.deleteMemo(memoId)
            withContext(Dispatchers.Main) {
                refreshMemos()
            }
        }
    }

    fun addUserDic(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dbHelper.addOrUpdateUserDic(-1L, keyword, "NNP")
        }
    }
}