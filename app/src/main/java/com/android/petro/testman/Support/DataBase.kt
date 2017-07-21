package com.android.petro.testman.Support

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * Database for storing test information
 */

class DataBase(context: Context): SQLiteOpenHelper(context, "database.db", null, 1) {

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("create table if not exists tests(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name text," +
                "author text," +
                "fiveBegins integer," +
                "fourBegins integer," +
                "threeBegins integer," +
                "showWrongs boolean," +
                "time integer," +
                "tasks text);")
    }
}
