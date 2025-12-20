package com.ediapp.mykeyword

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.ediapp.mykeyword.ui.notey.Memo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseHelper private constructor(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "memos2.db"
        private const val DATABASE_VERSION = 4

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
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_MEMOS)
        db.execSQL(CREATE_TABLE_KEYWORDS)

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
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_STATUS TEXT DEFAULT 'R'")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_DELETED_AT INTEGER")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_REG_DT TEXT")
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_REG_TM TEXT")
        }
    }

    fun addMemoNoTran(title: String, regDate: Long): Long {
        val db = writableDatabase
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(regDate)

        val values = ContentValues().apply {
            put(MEMOS_COL_CATEGORY, "notey")
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_TIMESTAMP, System.currentTimeMillis())
            put(MEMOS_COL_REG_DATE, regDate)
            put(MEMOS_COL_REG_DT, sdfDate.format(date))
            put(MEMOS_COL_REG_TM, sdfTime.format(date))
            put(MEMOS_COL_DELETED_AT, 0)
        }
        return db.insert(TABLE_MEMOS, null, values)
    }

    suspend fun addKeywords(title: String, memoId: Long) {
        withContext(Dispatchers.IO) {
            val db = writableDatabase
            db.beginTransaction()
            try {
                if (memoId != -1L) {
                    val myApp = context.applicationContext as MyApplication
                    val analyzer = myApp.morphemeAnalyzer
                    val keywords = analyzer.analyzeText(title).filter {
                        val pos = it.substringAfterLast('/', "")
                        pos == "NNG" || pos == "NNP"
                    }.map {
                        it.substringBeforeLast('/')
                    }.distinct()

                    db.delete(TABLE_KEYWORDS, "$MEMOS_COL_MYWORD_ID = ?", arrayOf(memoId.toString()))
                    keywords.forEach { keyword ->
                        val keywordValues = ContentValues().apply {
                            put(KEYWORDS_COL_KEYWORD, keyword)
                            put(MEMOS_COL_MYWORD_ID, memoId)
                        }
                        db.insert(TABLE_KEYWORDS, null, keywordValues)
                    }

//                    Log.d("MorphemeResult", "Keywords for '$title': $keywords")

                    val updateValues = ContentValues().apply {
                        put(MEMOS_COL_STATUS, "A")
                    }
                    db.update(TABLE_MEMOS, updateValues, "$MEMOS_COL_ID = ?", arrayOf(memoId.toString()))
                }

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

    fun updateMemo(id: Long, title: String, regDate: Long) {
        val db = writableDatabase
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val date = Date(regDate)

        val values = ContentValues().apply {
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_REG_DATE, regDate)
            put(MEMOS_COL_REG_DT, sdfDate.format(date))
            put(MEMOS_COL_REG_TM, sdfTime.format(date))
        }
        db.update(TABLE_MEMOS, values, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))
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

    fun deleteMemo(id: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MEMOS_COL_DELETED_AT, System.currentTimeMillis())
        }
        db.update(TABLE_MEMOS, values, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))
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

            db.insert(TABLE_MEMOS, null, values)
        }
        cursor.close()
    }

    fun getAllMemos(): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase
        val selection = "$MEMOS_COL_DELETED_AT = 0"
        val columns = arrayOf(
            MEMOS_COL_ID,
            MEMOS_COL_TITLE,
            MEMOS_COL_MEANING,
            MEMOS_COL_REG_DATE,
            MEMOS_COL_REG_DT,
            MEMOS_COL_REG_TM,
            MEMOS_COL_CATEGORY,
            MEMOS_COL_TIMESTAMP, // for sorting
            MEMOS_COL_DELETED_AT, // for model
            MEMOS_COL_LAT, // for model
            MEMOS_COL_LON, // for model
            MEMOS_COL_URL,
            MEMOS_COL_ADDRESS, // for model
            MEMOS_COL_SIDO, // for model
            MEMOS_COL_SIGUNGU, // for model
            MEMOS_COL_EUPMYEONDONG, // for model
            MEMOS_COL_STATUS // for model
        )
        val cursor = db.query(TABLE_MEMOS, columns, selection, null, null, null, "$MEMOS_COL_TIMESTAMP DESC")

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
}
