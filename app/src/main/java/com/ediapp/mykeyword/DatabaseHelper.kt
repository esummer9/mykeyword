package com.ediapp.mykeyword

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "memos.db"
        private const val DATABASE_VERSION = 3

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
        const val MEMOS_COL_DELETED_AT = "deleted_at"

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
                    "$MEMOS_COL_STATUS TEXT DEFAULT 'R'," +
                    "$MEMOS_COL_DELETED_AT INTEGER DEFAULT 0" +
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
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_DELETED_AT INTEGER")
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
                        // Get column values without re-querying indices
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
            values.put(MEMOS_COL_TITLE, "$currentTitle (copy)")
            values.put(MEMOS_COL_TIMESTAMP, System.currentTimeMillis())
            values.put(MEMOS_COL_REG_DATE, System.currentTimeMillis())
//            values.putNull(MEMOS_COL_DELETED_AT) // Ensure the new copy is not deleted
            values.put(MEMOS_COL_DELETED_AT, 0) // Ensure the new copy is not deleted

            db.insert(TABLE_MEMOS, null, values)
        }
        cursor.close()
    }

    fun getAllMemos(): List<Memo> {
        val memos = mutableListOf<Memo>()
        val db = readableDatabase
        val selection = "$MEMOS_COL_DELETED_AT < 1"
        val cursor = db.query(TABLE_MEMOS, null, selection, null, null, null, "$MEMOS_COL_TIMESTAMP DESC")

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
                // Check if deleted_at column exists before trying to access it
                val deletedAtCol = cursor.getColumnIndex(MEMOS_COL_DELETED_AT)
                val deletedAt = if (deletedAtCol != -1 && !cursor.isNull(deletedAtCol)) cursor.getLong(deletedAtCol) else null

                val memo = Memo(
                    id = cursor.getLong(idCol),
                    category = cursor.getString(categoryCol),
                    title = cursor.getString(titleCol),
                    meaning = cursor.getString(meaningCol),
                    timestamp = cursor.getLong(timestampCol),
                    regDate = if (cursor.isNull(regDateCol)) null else cursor.getLong(regDateCol),
                    url = cursor.getString(urlCol),
                    lat = if (cursor.isNull(latCol)) null else cursor.getString(latCol)?.toDoubleOrNull(),
                    lon = if (cursor.isNull(lonCol)) null else cursor.getString(lonCol)?.toDoubleOrNull(),
                    address = cursor.getString(addressCol),
                    sido = cursor.getString(sidoCol),
                    sigungu = cursor.getString(sigunguCol),
                    eupmyeondong = cursor.getString(eupmyeondongCol),
                    status = cursor.getString(statusCol),
                    deleted_at = 0
                )
                memos.add(memo)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return memos
    }
}