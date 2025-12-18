package com.ediapp.mykeyword

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "memos.db"
        private const val DATABASE_VERSION = 1

        // tb_MEMOS 테이블
        const val TABLE_MEMOS = "tb_memos"
        const val MEMOS_COL_ID = "_id"
        const val MEMOS_COL_CATEGORY = "category"
        const val MEMOS_COL_WORD = "word"
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

        private const val CREATE_TABLE_MEMOS =
            "CREATE TABLE $TABLE_MEMOS (" +
                    "$MEMOS_COL_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "$MEMOS_COL_CATEGORY TEXT," +
                    "$MEMOS_COL_WORD TEXT," +
                    "$MEMOS_COL_MEANING TEXT," +
                    "$MEMOS_COL_TIMESTAMP INTEGER," +
                    "$MEMOS_COL_REG_DATE INTEGER," +
                    "$MEMOS_COL_URL TEXT," +
                    "$MEMOS_COL_LAT REAL," +
                    "$MEMOS_COL_LON REAL," +
                    "$MEMOS_COL_ADDRESS TEXT," +
                    "$MEMOS_COL_SIDO TEXT," +
                    "$MEMOS_COL_SIGUNGU TEXT," +
                    "$MEMOS_COL_EUPMYEONDONG TEXT" +
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
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
//            db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN $MEMOS_COL_REG_DATE INTEGER")
        }
        // if (oldVersion < 3) {
        //     db.execSQL("ALTER TABLE $TABLE_MEMOS ADD COLUMN new_column_2 INTEGER DEFAULT 0")
        // }
    }
}
