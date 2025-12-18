package com.ediapp.mykeyword

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "memos.db"
        private const val DATABASE_VERSION = 2

        // tb_MEMOS 테이블
        const val TABLE_MEMOS = "tb_memos"
        const val MEMOS_COL_ID = "_id"
        const val MEMOS_COL_CATEGORY = "category"
        const val MEMOS_COL_TITLE = "title"
        const val MEMOS_COL_MEANING = "meaning"
        const val MEMOS_COL_TIMESTAMP = "created_at"
        const val MEMOS_COL_REG_DATE = "reg_date"
        const val MEMOS_COL_URL = "url"
        const val MEMOS_COL_LAT = "lat"
        const val MEMOS_COL_LON = "lon"
        const val MEMOS_COL_ADDRESS = "address"
        const val MEMOS_COL_SIDO = "sido"
        const val MEMOS_COL_SIGUNGU = "sigungu"
        const val MEMOS_COL_EUPMYEONDONG = "eupmyeondong"
        const val MEMOS_COL_STATUS = "status"

        private const val CREATE_TABLE_MEMOS =
            "CREATE TABLE $TABLE_MEMOS (" +
                    "$MEMOS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$MEMOS_COL_CATEGORY TEXT," +
                    "$MEMOS_COL_TITLE TEXT," +
                    "$MEMOS_COL_MEANING TEXT," +
                    "$MEMOS_COL_TIMESTAMP INTEGER," +
                    "$MEMOS_COL_REG_DATE INTEGER," +
                    "$MEMOS_COL_URL TEXT," +
                    "$MEMOS_COL_LAT TEXT," +
                    "$MEMOS_COL_LON TEXT," +
                    "$MEMOS_COL_ADDRESS TEXT," +
                    "$MEMOS_COL_SIDO TEXT," +
                    "$MEMOS_COL_SIGUNGU TEXT," +
                    "$MEMOS_COL_EUPMYEONDONG TEXT," +
                    "$MEMOS_COL_STATUS TEXT DEFAULT 'R'" +
                    ")"

        // tb_MEMOS 테이블
        const val TABLE_KEYWORDS = "tb_keywords"
        const val KEYWORDS_COL_ID = "_id"
        const val KEYWORDS_COL_KEYWORD = "keyword"
        const val MEMOS_COL_MYWORD_ID = "memos_id"

        private const val CREATE_TABLE_KEYWORDS =
            "CREATE TABLE $TABLE_KEYWORDS (" +
                    "$KEYWORDS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$KEYWORDS_COL_KEYWORD TEXT," +
                    "$MEMOS_COL_MYWORD_ID INTEGER," +
                    "FOREIGN KEY($MEMOS_COL_MYWORD_ID) REFERENCES $TABLE_MEMOS($MEMOS_COL_ID)" +
                    ")"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_MEMOS)
        db.execSQL(CREATE_TABLE_KEYWORDS)

        // Insert initial data
        val values = ContentValues()
        values.put(MEMOS_COL_CATEGORY, "init")
        values.put(MEMOS_COL_TITLE, "install")
        values.put(MEMOS_COL_TIMESTAMP, System.currentTimeMillis())
        values.put(MEMOS_COL_REG_DATE, System.currentTimeMillis())
        db.insert(TABLE_MEMOS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_STATUS TEXT DEFAULT 'R'")
        }
    }

    fun addMemo(title: String, regDate: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MEMOS_COL_CATEGORY, "notey")
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_TIMESTAMP, System.currentTimeMillis())
            put(MEMOS_COL_REG_DATE, regDate)
        }
        db.insert(TABLE_MEMOS, null, values)
    }

    fun updateMemo(id: Long, title: String, regDate: Long) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(MEMOS_COL_TITLE, title)
            put(MEMOS_COL_REG_DATE, regDate)
        }
        db.update(TABLE_MEMOS, values, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))
    }

    fun deleteMemo(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_MEMOS, "$MEMOS_COL_ID = ?", arrayOf(id.toString()))
    }

    fun getAllMemos(): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase
        val cursor = db.query(TABLE_MEMOS, null, null, null, null, null, "$MEMOS_COL_TIMESTAMP DESC")

        val idCol = cursor.getColumnIndexOrThrow(MEMOS_COL_ID)
        val categoryCol = cursor.getColumnIndexOrThrow(MEMOS_COL_CATEGORY)
        val titleCol = cursor.getColumnIndexOrThrow(MEMOS_COL_TITLE)
        val meaningCol = cursor.getColumnIndexOrThrow(MEMOS_COL_MEANING)
        val timestampCol = cursor.getColumnIndexOrThrow(MEMOS_COL_TIMESTAMP)
        val regDateCol = cursor.getColumnIndexOrThrow(MEMOS_COL_REG_DATE)
        val urlCol = cursor.getColumnIndexOrThrow(MEMOS_COL_URL)
        val latCol = cursor.getColumnIndexOrThrow(MEMOS_COL_LAT)
        val lonCol = cursor.getColumnIndexOrThrow(MEMOS_COL_LON)
        val addressCol = cursor.getColumnIndexOrThrow(MEMOS_COL_ADDRESS)
        val sidoCol = cursor.getColumnIndexOrThrow(MEMOS_COL_SIDO)
        val sigunguCol = cursor.getColumnIndexOrThrow(MEMOS_COL_SIGUNGU)
        val eupmyeondongCol = cursor.getColumnIndexOrThrow(MEMOS_COL_EUPMYEONDONG)
        val statusCol = cursor.getColumnIndexOrThrow(MEMOS_COL_STATUS)

        if (cursor.moveToFirst()) {
            do {
                val memo = Memo(
                    id = cursor.getLong(idCol),
                    category = cursor.getString(categoryCol),
                    title = cursor.getString(titleCol),
                    meaning = cursor.getString(meaningCol),
                    timestamp = cursor.getLong(timestampCol),
                    regDate = if (cursor.isNull(regDateCol)) null else cursor.getLong(regDateCol),
                    url = cursor.getString(urlCol),
                    lat = if (cursor.isNull(latCol)) null else cursor.getDouble(latCol),
                    lon = if (cursor.isNull(lonCol)) null else cursor.getDouble(lonCol),
                    address = cursor.getString(addressCol),
                    sido = cursor.getString(sidoCol),
                    sigungu = cursor.getString(sigunguCol),
                    eupmyeondong = cursor.getString(eupmyeondongCol),
                    status = cursor.getString(statusCol)
                )
                memos.add(memo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return memos
    }
}
