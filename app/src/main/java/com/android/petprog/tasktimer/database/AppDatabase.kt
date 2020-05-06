package com.android.petprog.tasktimer.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.android.petprog.tasktimer.database.TaskTimerContract.CurrentTimingContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TasksContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TimingsContract

/**
 * Basic database class for the application
 *
 * The only class that should use this is [AppProvider].
 *
 * */

// SQLiteOpenHelper is an abstract class

private const val TAG = "AppDatabase"

private const val DATABASE_NAME = "TaskTimer.db"

private const val DATABASE_VERSION = 3

// Singleton
internal class AppDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    init {
        Log.d(TAG, "AppDatabase: Initializing")
    }

    override fun onCreate(db: SQLiteDatabase) {

        Log.d(TAG, "onCreate starts")
        val sSQL =
            TasksContract.Table.CREATE_TABLE
        Log.d(TAG, "sSQL: $sSQL")
        db.execSQL(sSQL)

        addTimingsTable(db)
        addCurrentTimingView(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade starts")
        when (oldVersion) {
            1 -> {
                addTimingsTable(db)
                addCurrentTimingView(db)
            }

            2 -> {
                addCurrentTimingView(db)
            }
            else -> throw IllegalStateException("onUpgrade() with unknown version: $newVersion")
        }
    }

    private fun addTimingsTable(db: SQLiteDatabase) {
        val sSQLTiming = TimingsContract.Table.CREATE_TABLE
        Log.d(TAG, sSQLTiming)
        db.execSQL(sSQLTiming)

        val sSQLTrigger = """CREATE TRIGGER Remove_Task
            AFTER DELETE ON ${TasksContract.TABLE_NAME}
            FOR EACH ROW
            BEGIN DELETE FROM ${TimingsContract.TABLE_NAME}
            WHERE ${TimingsContract.Columns.TIMING_TASK_ID} = OLD.${TasksContract.Columns.ID};
            END;""".replaceIndent(" ")
        Log.d(TAG, sSQLTrigger)
        db.execSQL(sSQLTrigger)
    }

    private fun addCurrentTimingView(db: SQLiteDatabase) {
        val sSQLTimingView = """CREATE VIEW ${CurrentTimingContract.TABLE_NAME}
            AS SELECT ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID},
            ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME},
            ${TasksContract.TABLE_NAME}.${TasksContract.Columns.TASK_NAME}
            FROM ${TimingsContract.TABLE_NAME}
            JOIN ${TasksContract.TABLE_NAME}
            ON ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_TASK_ID} = ${TasksContract.TABLE_NAME}.${TasksContract.Columns.ID}
            WHERE ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_DURATION} = 0
            ORDER BY ${TimingsContract.TABLE_NAME}.${TimingsContract.Columns.TIMING_START_TIME} DESC;
        """.replaceIndent(" ")
        Log.d(TAG, sSQLTimingView)
        db.execSQL(sSQLTimingView)

    }

    companion object : SingletonHolder<AppDatabase, Context>(::AppDatabase)

}