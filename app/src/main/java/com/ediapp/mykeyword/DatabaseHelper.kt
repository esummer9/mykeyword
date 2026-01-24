package com.ediapp.mykeyword

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ediapp.mykeyword.ui.notey.Memo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class Keyword(val keyword: String, val count: Int)

data class UserDic(val id: Long, val keyword: String, val pos: String)

class DatabaseHelper private constructor(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "memos3.db"
        private const val DATABASE_VERSION = 2

        @Volatile
        private var INSTANCE: DatabaseHelper? = null

        fun getInstance(context: Context): DatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseHelper(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        // tb_MEMOS 테이블
        const val TABLE_MEMOS = "tb_memos"
        const val MEMOS_COL_ID = "_id"
        const val MEMOS_COL_CATEGORY = "category"
        const val MEMOS_COL_TITLE = "title"
        const val MEMOS_COL_MEANING = "meaning"
        const val MEMOS_COL_TIMESTAMP = "created_at"
        const val MEMOS_COL_REG_DATE = "reg_date"
        const val MEMOS_COL_REG_DT = "reg_dt"
        const val MEMOS_COL_REG_TM = "reg_tm"
        const val MEMOS_COL_URL = "url"
        const val MEMOS_COL_LAT = "lat"
        const val MEMOS_COL_LON = "lon"
        const val MEMOS_COL_ADDRESS = "address"
        const val MEMOS_COL_SIDO = "sido"
        const val MEMOS_COL_SIGUNGU = "sigungu"
        const val MEMOS_COL_EUPMYEONDONG = "eupmyeondong"
        const val MEMOS_COL_STATUS = "status"
        const val MEMOS_COL_DELETED_AT = "deleted_at"

        private const val CREATE_TABLE_MEMOS = (
            "CREATE TABLE " + TABLE_MEMOS + " (" +
                "$MEMOS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$MEMOS_COL_CATEGORY TEXT," +
                "$MEMOS_COL_TITLE TEXT," +
                "$MEMOS_COL_MEANING TEXT," +
                "$MEMOS_COL_TIMESTAMP INTEGER," +
                "$MEMOS_COL_REG_DATE INTEGER," +
                "$MEMOS_COL_REG_DT TEXT," +
                "$MEMOS_COL_REG_TM TEXT," +
                "$MEMOS_COL_URL TEXT," +
                "$MEMOS_COL_LAT REAL," +
                "$MEMOS_COL_LON REAL," +
                "$MEMOS_COL_ADDRESS TEXT," +
                "$MEMOS_COL_SIDO TEXT," +
                "$MEMOS_COL_SIGUNGU TEXT," +
                "$MEMOS_COL_EUPMYEONDONG TEXT," +
                "$MEMOS_COL_STATUS TEXT DEFAULT 'R'," +
                "$MEMOS_COL_DELETED_AT INTEGER default 0 " +
                ")"
            )

        // tb_KEYWORDS 테이블
        const val TABLE_KEYWORDS = "tb_keywords"
        const val KEYWORDS_COL_ID = "_id"
        const val KEYWORDS_COL_KEYWORD = "keyword"
        const val MEMOS_COL_MYWORD_ID = "memos_id"

        private const val CREATE_TABLE_KEYWORDS = (
            "CREATE TABLE " + TABLE_KEYWORDS + " (" +
                "$KEYWORDS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$KEYWORDS_COL_KEYWORD TEXT," +
                "$MEMOS_COL_MYWORD_ID INTEGER," +
                "FOREIGN KEY($MEMOS_COL_MYWORD_ID) REFERENCES $TABLE_MEMOS($MEMOS_COL_ID)" +
                ")"
            )

        // tb_userdics 테이블
        const val TABLE_DICS = "tb_userdics"
        const val DICS_COL_ID = "_id"
        const val DICS_COL_KEYWORD = "keyword"
        const val DICS_COL_POS = "pos"
        private const val CREATE_TABLE_USERDICS = (
            "CREATE TABLE " + TABLE_DICS + " (" +
                "$DICS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$DICS_COL_KEYWORD TEXT," +
                "$DICS_COL_POS TEXT" +
                ")"
            )

        private const val CREATE_INDEX_MEMOS_1 = "CREATE INDEX idx_memos_query ON $TABLE_MEMOS($MEMOS_COL_CATEGORY, $MEMOS_COL_DELETED_AT, $MEMOS_COL_REG_DATE, $MEMOS_COL_TIMESTAMP);"

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_MEMOS)
        db.execSQL(CREATE_TABLE_KEYWORDS)
        db.execSQL(CREATE_TABLE_USERDICS)
        db.execSQL(CREATE_INDEX_MEMOS_1)

        // Insert initial data
        val now = System.currentTimeMillis()
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(now)

        val values = ContentValues()
        values.put(MEMOS_COL_CATEGORY, "init")
        values.put(MEMOS_COL_TITLE, "install")
        values.put(MEMOS_COL_TIMESTAMP, now)
        values.put(MEMOS_COL_REG_DATE, now)
        values.put(MEMOS_COL_REG_DT, sdfDate.format(date))
        values.put(MEMOS_COL_REG_TM, sdfTime.format(date))
        values.put(MEMOS_COL_DELETED_AT, 0)
        db.insert(TABLE_MEMOS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(CREATE_INDEX_MEMOS_1)
        }
    }

    fun addMemo(title: String, mean: String?, address: String?, url: String?, regDate: Long): Long {
        val db = writableDatabase
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(regDate)

        val values = ContentValues().apply {
            put(MEMOS_COL_CATEGORY, "notey")
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_MEANING, mean)
            put(MEMOS_COL_ADDRESS, address)
            put(MEMOS_COL_URL, url)
            put(MEMOS_COL_TIMESTAMP, System.currentTimeMillis())
            put(MEMOS_COL_REG_DATE, regDate)
            put(MEMOS_COL_REG_DT, sdfDate.format(date))
            put(MEMOS_COL_REG_TM, sdfTime.format(date))
            put(MEMOS_COL_DELETED_AT, 0)
        }
        return db.insert(TABLE_MEMOS, null, values)
    }

    suspend fun addKeywords(analyzer: KomoranAnalyzer, title: String, memoId: Long) {
        withContext(Dispatchers.IO) {
            if (memoId != -1L) {
                val keywords = analyzer.analyzeText(title).filter {
                    val pos = it.substringAfterLast('/', "")
                    pos == "NNG" || pos == "NNP" || pos == "NA"
                }.map {
                    it.substringBeforeLast('/')
                }.distinct()

                val db = writableDatabase
                db.beginTransaction()
                try {
                    db.delete(TABLE_KEYWORDS, "$MEMOS_COL_MYWORD_ID = ?", arrayOf(memoId.toString()))

                    Log.d("DatabaseHelper", "keywords: $keywords")

                    keywords.forEach { keyword ->
                        val keywordValues = ContentValues().apply {
                            put(KEYWORDS_COL_KEYWORD, keyword)
                            put(MEMOS_COL_MYWORD_ID, memoId)
                        }
                        db.insert(TABLE_KEYWORDS, null, keywordValues)
                    }

                    val updateValues = ContentValues().apply {
                        put(MEMOS_COL_STATUS, "A")
                    }
                    db.update(TABLE_MEMOS, updateValues, "$MEMOS_COL_ID = ?", arrayOf(memoId.toString()))

                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    Log.e("DatabaseHelper", "Error in addKeywords transaction", e)
                } finally {
                    if (db.inTransaction()) {
                        db.endTransaction()
                    }
                }
            }
        }
    }

    fun updateMemo(id: Long, title: String, mean: String, address: String, url: String, regDate: Long) {
        val db = writableDatabase
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(regDate)

        val values = ContentValues().apply {
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_MEANING, mean)
            put(MEMOS_COL_ADDRESS, address)
            put(MEMOS_COL_URL, url)
            put(MEMOS_COL_REG_DATE, regDate)
            put(MEMOS_COL_REG_DT, sdfDate.format(date))
            put(MEMOS_COL_REG_TM, sdfTime.format(date))
            put(MEMOS_COL_STATUS, "R")
        }
        db.update(TABLE_MEMOS, values, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))
    }


    fun getMemoById(id: Long): Memo? {
        val db = readableDatabase
        var memo: Memo? = null
        val cursor = db.query(
            TABLE_MEMOS,
            null, // 모든 컬럼
            "$MEMOS_COL_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )

        if (cursor.moveToFirst()) {
            memo = Memo(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_ID)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)),
                meaning = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)),
                regDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE))) null else cursor.getLong(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
                ),
                regDt = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DT)),
                regTm = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_TM)),
                url = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_URL)),
                lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LAT))) null else cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
                ),
                lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LON))) null else cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
                ),
                address = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)),
                sido = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)),
                sigungu = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)),
                eupmyeondong = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        MEMOS_COL_EUPMYEONDONG
                    )
                ),
                status = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)),
                deleted_at = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT))) null else cursor.getLong(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT)
                )
            )
        }
        cursor.close()
        return memo
    }

    fun getRecentMemos(limit: Int): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase

        val selection = "$MEMOS_COL_DELETED_AT = 0"
        val cursor = db.query(
            TABLE_MEMOS,
            null,
            selection,
            null,
            null,
            null,
            "$MEMOS_COL_TIMESTAMP DESC",
            limit.toString()
        )

        if (cursor.moveToFirst()) {
            do {
                val memo = Memo(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_ID)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)),
                    meaning = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)),
                    regDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
                    ),
                    regDt = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DT)),
                    regTm = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_TM)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_URL)),
                    lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LAT))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
                    ),
                    lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LON))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
                    ),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)),
                    sido = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)),
                    sigungu = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)),
                    eupmyeondong = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            MEMOS_COL_EUPMYEONDONG
                        )
                    ),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)),
                    deleted_at = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT)
                    )
                )
                memos.add(memo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return memos
    }

    fun getTrendingKeywords(limit: Int): List<Keyword> {
        val keywords = mutableListOf<Keyword>()
        val db = readableDatabase
        val query = "SELECT ${KEYWORDS_COL_KEYWORD}, COUNT(*) as count FROM ${TABLE_KEYWORDS} " +
                "GROUP BY ${KEYWORDS_COL_KEYWORD} ORDER BY count DESC LIMIT ?"

        val cursor = db.rawQuery(query, arrayOf(limit.toString()))

        if (cursor.moveToFirst()) {
            do {
                val keyword = cursor.getString(cursor.getColumnIndexOrThrow(KEYWORDS_COL_KEYWORD))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                keywords.add(Keyword(keyword, count))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return keywords
    }

    fun getLatestMemo(category: String): Memo? {
        val db = readableDatabase
        var memo: Memo? = null
        val cursor = db.query(
            TABLE_MEMOS,
            null, // 모든 컬럼
            "$MEMOS_COL_CATEGORY = ?",
            arrayOf(category),
            null, null,  "$MEMOS_COL_TIMESTAMP DESC", "1"
        )

        if (cursor.moveToFirst()) {
            memo = Memo(
                id = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_ID)),
                category = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)),
                title = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)),
                meaning = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)),
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)),
                regDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE))) null else cursor.getLong(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
                ),
                regDt = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DT)),
                regTm = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_TM)),
                url = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_URL)),
                lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LAT))) null else cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
                ),
                lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LON))) null else cursor.getDouble(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
                ),
                address = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)),
                sido = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)),
                sigungu = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)),
                eupmyeondong = cursor.getString(
                    cursor.getColumnIndexOrThrow(
                        MEMOS_COL_EUPMYEONDONG
                    )
                ),
                status = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)),
                deleted_at = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT))) null else cursor.getLong(
                    cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT)
                )
            )
        }
        cursor.close()
        return memo
    }

    fun deleteMemo(id: Long) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // "deleted_at" 컬럼 업데이트로 "soft delete" 처리
            val values = ContentValues().apply {
                put(MEMOS_COL_DELETED_AT, System.currentTimeMillis())
            }
            db.update(TABLE_MEMOS, values, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))

            db.delete(TABLE_KEYWORDS, "$MEMOS_COL_MYWORD_ID = ?", arrayOf(id.toString()))
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun duplicateMemo(id: Long) {
        val db = writableDatabase
        val cursor = db.query(TABLE_MEMOS, null, "$MEMOS_COL_ID = ?", arrayOf(id.toString()), null, null, null)
        if (cursor.moveToFirst()) {
            val values = ContentValues()
            for (i in 0 until cursor.columnCount) {
                val columnName = cursor.getColumnName(i)
                if (columnName != MEMOS_COL_ID) { // Don't copy the ID
                    when (cursor.getType(i)) {
                        android.database.Cursor.FIELD_TYPE_BLOB -> values.put(columnName, cursor.getBlob(i))
                        android.database.Cursor.FIELD_TYPE_FLOAT -> values.put(columnName, cursor.getFloat(i))
                        android.database.Cursor.FIELD_TYPE_INTEGER -> values.put(columnName, cursor.getLong(i))
                        android.database.Cursor.FIELD_TYPE_STRING -> values.put(columnName, cursor.getString(i))
                        android.database.Cursor.FIELD_TYPE_NULL -> values.putNull(columnName)
                    }
                }
            }
            // Modify title and timestamps
            val currentTitle = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE))
            val now = System.currentTimeMillis()
            val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val date = Date(now)

            values.put(MEMOS_COL_TITLE, "$currentTitle (copy)")
            values.put(MEMOS_COL_TIMESTAMP, now)
            values.put(MEMOS_COL_REG_DATE, now)
            values.put(MEMOS_COL_REG_DT, sdfDate.format(date))
            values.put(MEMOS_COL_REG_TM, sdfTime.format(date))
            values.put(MEMOS_COL_DELETED_AT, 0)
            values.put(MEMOS_COL_STATUS, "R")

            db.insert(TABLE_MEMOS, null, values)
        }
        cursor.close()
    }

    fun getAllMemos(status: String?): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase

        val selection = if (status == null) "$MEMOS_COL_DELETED_AT = 0" else "$MEMOS_COL_DELETED_AT = 0 and $MEMOS_COL_STATUS = '$status'"

        val cursor = db.query(TABLE_MEMOS, null, selection, null, null, null, "$MEMOS_COL_TIMESTAMP DESC")

        if (cursor.moveToFirst()) {
            do {
                val memo = Memo(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_ID)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)),
                    meaning = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)),
                    regDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
                    ),
                    regDt = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DT)),
                    regTm = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_TM)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_URL)),
                    lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LAT))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
                    ),
                    lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LON))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
                    ),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)),
                    sido = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)),
                    sigungu = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)),
                    eupmyeondong = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            MEMOS_COL_EUPMYEONDONG
                        )
                    ),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)),
                    deleted_at = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT)
                    )
                )
                memos.add(memo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return memos
    }
    fun getMemosWithPagination(
        category: String,
        searchQuery: String?,
        startDate: Long?,
        limit: Int,
        offset: Int
    ): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase

        val selectionClauses = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()

        selectionClauses.add("$MEMOS_COL_DELETED_AT = 0")
        selectionClauses.add("$MEMOS_COL_CATEGORY = ?")
        selectionArgs.add(category)

        searchQuery?.let {
            selectionClauses.add("$MEMOS_COL_TITLE LIKE ?")
            selectionArgs.add("%$it%")
        }

        startDate?.let {
            selectionClauses.add("$MEMOS_COL_REG_DATE >= ?")
            selectionArgs.add(it.toString())
        }

        val selection = selectionClauses.joinToString(separator = " AND ")
        val cursor = db.query(
            TABLE_MEMOS,
            null,
            selection,
            selectionArgs.toTypedArray(),
            null,
            null,
            "$MEMOS_COL_TIMESTAMP DESC",
            "$limit OFFSET $offset"
        )

        if (cursor.moveToFirst()) {
            do {
                val memo = Memo(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_ID)),
                    category = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)),
                    title = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)),
                    meaning = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)),
                    timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)),
                    regDate = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
                    ),
                    regDt = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DT)),
                    regTm = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_REG_TM)),
                    url = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_URL)),
                    lat = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LAT))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
                    ),
                    lon = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_LON))) null else cursor.getDouble(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
                    ),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)),
                    sido = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)),
                    sigungu = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)),
                    eupmyeondong = cursor.getString(
                        cursor.getColumnIndexOrThrow(
                            MEMOS_COL_EUPMYEONDONG
                        )
                    ),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)),
                    deleted_at = if (cursor.isNull(cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT))) null else cursor.getLong(
                        cursor.getColumnIndexOrThrow(MEMOS_COL_DELETED_AT)
                    )
                )
                memos.add(memo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return memos
    }

    fun getKeywordsByDateRange(startDate: Long, endDate: Long): List<Keyword> {
        val keywords = mutableListOf<Keyword>()
        val db = readableDatabase
        val query = "SELECT ${KEYWORDS_COL_KEYWORD}, COUNT(*) as count FROM ${TABLE_KEYWORDS} " +
                "INNER JOIN ${TABLE_MEMOS} ON ${TABLE_KEYWORDS}.${MEMOS_COL_MYWORD_ID} = ${TABLE_MEMOS}.${MEMOS_COL_ID} " +
                "WHERE ${TABLE_MEMOS}.${MEMOS_COL_REG_DATE} BETWEEN ? AND ? " +
                "GROUP BY ${KEYWORDS_COL_KEYWORD} ORDER BY count DESC"

        val cursor = db.rawQuery(query, arrayOf(startDate.toString(), endDate.toString()))

        if (cursor.moveToFirst()) {
            do {
                val keyword = cursor.getString(cursor.getColumnIndexOrThrow(KEYWORDS_COL_KEYWORD))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("count"))
                keywords.add(Keyword(keyword, count))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return keywords
    }

    fun deleteKeyword(keyword: String) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // 1. Find all memo IDs associated with this keyword
            val memoIds = mutableListOf<String>()
            val cursor = db.query(
                TABLE_KEYWORDS,
                arrayOf(MEMOS_COL_MYWORD_ID),
                "$KEYWORDS_COL_KEYWORD = ?",
                arrayOf(keyword),
                null, null, null
            )
            if (cursor.moveToFirst()) {
                do {
                    memoIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(MEMOS_COL_MYWORD_ID)).toString())
                } while (cursor.moveToNext())
            }
            cursor.close()

            // 2. Delete the keyword from the keywords table
            db.delete(TABLE_KEYWORDS, "$KEYWORDS_COL_KEYWORD = ?", arrayOf(keyword))

            // 3. Update the status of the affected memos to 'R'
            if (memoIds.isNotEmpty()) {
                val values = ContentValues().apply {
                    put(MEMOS_COL_STATUS, "R")
                }
                val whereClause = "$MEMOS_COL_ID IN (${memoIds.joinToString(separator = ",")})"

                Log.d("DatabaseHelper", "whereClause: $whereClause")

                db.update(TABLE_MEMOS, values, whereClause, null)
            }

            db.setTransactionSuccessful()
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error in deleteKeyword transaction", e)
        } finally {
            if (db.inTransaction()) {
                db.endTransaction()
            }
        }
    }

    suspend fun reprocessKeywords(analyzer: KomoranAnalyzer) {
        withContext(Dispatchers.IO) {
            val memos = getAllMemos("R")
            memos.forEach { memo ->
                if(memo.title != null)
                    addKeywords(analyzer, memo.title, memo.id)
            }
        }
    }

    fun addOrUpdateUserDic(_id: Long, keyword: String, pos: String): Long {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_DICS,
            arrayOf(DICS_COL_ID),
            "$DICS_COL_KEYWORD = ? AND $DICS_COL_POS = ?",
            arrayOf(keyword, pos),
            null, null, null
        )
        if (cursor.count > 0) {
            cursor.close()
            return -1L // Already exists
        }
        cursor.close()

        val values = ContentValues().apply {
            put(DICS_COL_KEYWORD, keyword)
            put(DICS_COL_POS, pos)
        }
        if (_id > -0L) {
            db.update(TABLE_DICS, values, "$DICS_COL_ID = ?", arrayOf(_id.toString()))
            return _id
        }
        else
            return db.insert(TABLE_DICS, null, values)
    }

    fun deleteUserDic(_id: Long): Long {
        val db = writableDatabase
        db.delete(TABLE_DICS, "$DICS_COL_ID = ?", arrayOf(_id.toString()))
        return 0
    }

    fun getAllUserDics(): List<UserDic> {
        val userDics = mutableListOf<UserDic>()
        val db = readableDatabase
        val cursor = db.query(TABLE_DICS, null, null, null, null, null, "$DICS_COL_ID DESC")

        if (cursor.moveToFirst()) {
            do {
                val userDic = UserDic(
                    id = cursor.getLong(cursor.getColumnIndexOrThrow(DICS_COL_ID)),
                    keyword = cursor.getString(cursor.getColumnIndexOrThrow(DICS_COL_KEYWORD)),
                    pos = cursor.getString(cursor.getColumnIndexOrThrow(DICS_COL_POS))
                )
                userDics.add(userDic)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return userDics
    }

    suspend fun writeUserDictionaryToFile(context: Context) = withContext(Dispatchers.IO) {
        val userDics = getAllUserDics()
        val komoranDir = File(context.filesDir, "komoran")
        if (!komoranDir.exists()) {
            komoranDir.mkdirs()
        }
        val userDicFile = File(komoranDir, "user.dict")

        try {
            userDicFile.printWriter().use { out ->
                userDics.forEach { userDic ->
                    out.println("${userDic.keyword}\t${userDic.pos}")
                }
            }
            Log.d("DatabaseHelper", "User dictionary written to ${userDicFile.absolutePath}")
        } catch (e: Exception) {
            Log.e("DatabaseHelper", "Error writing user dictionary to file", e)
        }
    }
 }
