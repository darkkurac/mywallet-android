package com.nikita.mywallet

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_NAME (
                $KEY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $KEY_AMOUNT REAL,
                $KEY_TYPE TEXT,
                $KEY_DESCRIPTION TEXT,
                $KEY_DATE TEXT
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun getBalance(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT COALESCE(SUM(CASE WHEN $KEY_TYPE = 'Расход' THEN -$KEY_AMOUNT ELSE $KEY_AMOUNT END), 0) FROM $TABLE_NAME",
            null
        )

        var balance = 0.0
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0)
        }
        cursor.close()
        return balance
    }

    fun insertOperation(amount: Double, type: String, description: String, date: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_AMOUNT, amount)
            put(KEY_TYPE, type)
            put(KEY_DESCRIPTION, description)
            put(KEY_DATE, date)
        }

        val rowId = db.insert(TABLE_NAME, null, values)
        db.close()
        return rowId
    }

    fun deleteOperation(id: Int): Int {
        val db = writableDatabase
        val deletedRows = db.delete(TABLE_NAME, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return deletedRows
    }

    fun updateOperation(id: Int, amount: Double, type: String, description: String, date: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(KEY_AMOUNT, amount)
            put(KEY_TYPE, type)
            put(KEY_DESCRIPTION, description)
            put(KEY_DATE, date)
        }

        val updatedRows = db.update(TABLE_NAME, values, "$KEY_ID=?", arrayOf(id.toString()))
        db.close()
        return updatedRows
    }

    fun getAllOperations(): List<Operation> {
        val operations = mutableListOf<Operation>()
        val db = readableDatabase

        try {
            val cursor = db.rawQuery("SELECT * FROM $TABLE_NAME ORDER BY $KEY_ID DESC", null)
            if (cursor.moveToFirst()) {
                do {
                    val operation = Operation(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_AMOUNT)),
                        type = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TYPE)),
                        description = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DESCRIPTION)),
                        date = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATE))
                    )
                    operations.add(operation)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (_: Exception) {
            return emptyList()
        }

        return operations
    }

    companion object {
        private const val DATABASE_NAME = "MyWallet.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_NAME = "operations"
        private const val KEY_ID = "id"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_TYPE = "type"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_DATE = "date"
    }
}
