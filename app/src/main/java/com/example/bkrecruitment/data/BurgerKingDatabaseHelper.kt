package com.example.bkrecruitment.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BurgerKingDatabaseHelper(
    private val context: Context,
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val script = context.assets
            .open(SQL_ASSET_NAME)
            .bufferedReader()
            .use { it.readText() }

        db.beginTransaction()
        try {
            script
                .split(";")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .forEach(db::execSQL)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS applications")
        db.execSQL("DROP TABLE IF EXISTS candidates")
        db.execSQL("DROP TABLE IF EXISTS vacancies")
        db.execSQL("DROP TABLE IF EXISTS restaurants")
        onCreate(db)
    }

    companion object {
        private const val DATABASE_NAME = "bk_hiring.db"
        private const val DATABASE_VERSION = 1
        private const val SQL_ASSET_NAME = "burger_king_recruitment.sql"
    }
}
