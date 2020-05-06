package com.android.petprog.tasktimer.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import com.android.petprog.tasktimer.database.TaskTimerContract.CurrentTimingContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TasksContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TimingsContract

/**
 * Provider for the TaskTimer App only. This is the only class that knows about [AppDatabase].
 *
 * */

private const val TAG = "AppProvider"

const val CONTENT_AUTHORITY = "com.android.petprog.tasktimer.provider"

// Can use any distinct positive number
// Code that will be return when there is a particular match with the path
private const val TASKS = 100
private const val TASKS_ID = 101

private const val TIMINGS = 200
private const val TIMINGS_ID = 201

private const val CURRENT_TIMING = 300

private const val TASK_DURATIONS = 400
private const val TASK_DURATIONS_ID = 401

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

//    private val db: AppDatabase

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "AppProvider: buildUriMatcher starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        // content://com.android.petprog.tasktimer.provider/Tasks
        matcher.addURI(
            CONTENT_AUTHORITY, TasksContract.TABLE_NAME,
            TASKS
        )

        // content://com.android.petprog.tasktimer.provider/Tasks/8
        matcher.addURI(
            CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#",
            TASKS_ID
        )

        // content://com.android.petprog.tasktimer.provider/Timings
        matcher.addURI(
            CONTENT_AUTHORITY, TimingsContract.TABLE_NAME,
            TIMINGS
        )

        // content://com.android.petprog.tasktimer.provider/Timings/3
        matcher.addURI(
            CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#",
            TIMINGS_ID
        )

        // content://com.android.petprog.tasktimer.provider/vwCurrentTiming
        matcher.addURI(
            CONTENT_AUTHORITY, CurrentTimingContract.TABLE_NAME, CURRENT_TIMING
        )

//        // content://com.android.petprog.tasktimer.provider/Durations
//        matcher.addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TASK_DURATIONS)
//
//        // content://com.android.petprog.tasktimer.provider/Durations/3
//        matcher.addURI(CONTENT_AUTHORITY, "${DurationsContract.TABLE_NAME}/#", TASK_DURATIONS_ID)

        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")

        return true
    }

    override fun getType(uri: Uri): String? {
        Log.d(TAG, "query: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        return when (match) {
            TASKS -> TasksContract.CONTENT_TYPE
            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE
            TIMINGS -> TimingsContract.CONTENT_TYPE
            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE
            CURRENT_TIMING -> CurrentTimingContract.CONTENT_TYPE
//            TASK_DURATIONS -> TaskTimerContract.DurationsContract.CONTENT_TYPE
//            TASK_DURATIONS_ID -> TaskTimerContract.DurationsContract.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("unknown Uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert: match is $match")
        val recordId: Long
        val returnUri: Uri

        when (match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId == -1L) {
                    throw SQLException("Failed to insert, Uri is $uri")
                } else {
                    returnUri = TasksContract.buildUriFromId(recordId)
                }
            }
            TIMINGS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                recordId = db.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TimingsContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri is $uri")
                }
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }
        if (recordId > 0) {
            Log.d(TAG, "insert: Setting notifyChange with $uri")
            context?.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exiting insert, returning $returnUri")
        return returnUri
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        val queryBuilder = SQLiteQueryBuilder()
        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")       // <-- change method
                queryBuilder.appendWhereEscapeString("$taskId")       // <-- change method
            }

            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME

            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val timingId = TimingsContract.getId(uri)

                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")   // <-- and here
                queryBuilder.appendWhereEscapeString("$timingId")   // <-- and here
            }

            CURRENT_TIMING -> {
                queryBuilder.tables = CurrentTimingContract.TABLE_NAME
            }

//
//            TASK_DURATIONS -> queryBuilder.tables = DurationsContract.TABLE_NAME
//
//            TASK_DURATIONS_ID -> {
//                queryBuilder.tables = DurationsContract.TABLE_NAME
//                val durationId = DurationsContract.getId(uri)
//                queryBuilder.appendWhere("${DurationsContract.Columns.ID} = ")   // <-- and here
//                queryBuilder.appendWhereEscapeString("$durationId")   // <-- and here
//            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val db = AppDatabase.getInstance(context!!).readableDatabase
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query: rows in returned cursor = ${cursor.count}") // TODO remove this line

        return cursor
    }


    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        Log.d(TAG, "update: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val rowCount: Int
        var selectionCriteria: String
        var selectionCriteriaArgs: Array<String>

        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                rowCount = db.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TasksContract.getId(uri)
                // Where clause
                selectionCriteria = "${TasksContract.Columns.ID} = ?"
                selectionCriteriaArgs = arrayOf("$id")
                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND (?)"
                    selectionCriteriaArgs += selection
                }
                rowCount =
                    db.update(TasksContract.TABLE_NAME, values, selectionCriteria, selectionCriteriaArgs)
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                rowCount = db.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TimingsContract.getId(uri)
                // Where clause
                selectionCriteria = "${TimingsContract.Columns.ID} = ?"
                selectionCriteriaArgs = arrayOf("$id")
                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND (?)"
                    selectionCriteriaArgs += selection
                }
                rowCount =
                    db.update(TimingsContract.TABLE_NAME, values, selectionCriteria, selectionCriteriaArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri $uri")
        }

        if (rowCount > 0) {
            Log.d(TAG, "update: Setting notifyChange with $uri")
            context?.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exiting update, returning $rowCount")
        return rowCount
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "delete: match is $match")

        val rowCount: Int
        var selectionCriteria: String
        var selectionCriteriaArgs: Array<String>

        when (match) {
            TASKS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                rowCount = db.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }
            TASKS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TasksContract.getId(uri)
                // Where clause
                selectionCriteria = "${TasksContract.Columns.ID} = ?"
                selectionCriteriaArgs = arrayOf("$id")
                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND (?)"
                    selectionCriteriaArgs += selection
                }
                rowCount =
                    db.delete(TasksContract.TABLE_NAME, selectionCriteria, selectionCriteriaArgs)
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                rowCount = db.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context!!).writableDatabase
                val id = TimingsContract.getId(uri)
                // Where clause
                selectionCriteria = "${TimingsContract.Columns.ID} = ?"
                selectionCriteriaArgs = arrayOf("$id")
                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria.plus("AND (?)")
                    selectionCriteriaArgs += selection
                }
                rowCount =
                    db.delete(TimingsContract.TABLE_NAME, selectionCriteria, selectionCriteriaArgs)
            }

            else -> throw IllegalArgumentException("Unknown uri $uri")
        }
        if (rowCount > 0) {
            Log.d(TAG, "delete: setting notifyChange with $uri")
            context?.contentResolver?.notifyChange(uri, null)
        }
        Log.d(TAG, "Exiting delete, returning $rowCount")
        return rowCount
    }

}