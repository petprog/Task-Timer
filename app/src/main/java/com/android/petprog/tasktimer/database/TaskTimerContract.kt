package com.android.petprog.tasktimer.database

import android.content.ContentResolver
import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns


// Available in thr package folder
object TaskTimerContract {

    object TasksContract {

        internal const val TABLE_NAME = "Tasks"

        val CONTENT_URI = Uri.withAppendedPath(
            CONTENT_AUTHORITY_URI,
            TABLE_NAME
        )

        const val CONTENT_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

        object Table {
            // CREATE TABLE Tasks (_id INTEGER PRIMARY KEY NOT NULL, Name TEXT NOT NULL, Description TEXT, SortOrder TEXT)
            val CREATE_TABLE = """CREATE TABLE $TABLE_NAME (
            ${Columns.ID} INTEGER PRIMARY KEY NOT NULL,
             ${Columns.TASK_NAME} TEXT NOT NULL,
             ${Columns.TASK_DESCRIPTION} TEXT,
             ${Columns.TASK_SORT_ORDER} INTEGER);""".replaceIndent(" ")
        }

        // Tasks fields
        object Columns {
            const val ID = BaseColumns._ID
            const val TASK_NAME = "Name"
            const val TASK_DESCRIPTION = "Description"
            const val TASK_SORT_ORDER = "SortOrder"
        }

        fun getId(uri: Uri): Long {
            return ContentUris.parseId(uri)
        }

        fun buildUriFromId(id: Long): Uri {
            return ContentUris.withAppendedId(CONTENT_URI, id)
        }
    }

    object TimingsContract {

        internal const val TABLE_NAME = "Timings"

        val CONTENT_URI = Uri.withAppendedPath(
            CONTENT_AUTHORITY_URI,
            TABLE_NAME
        )

        const val CONTENT_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.${TABLE_NAME}"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.${TABLE_NAME}"

        object Columns {
            const val ID = BaseColumns._ID
            const val TIMING_TASK_ID = "TaskId"
            const val TIMING_START_TIME = "StartTime"
            const val TIMING_DURATION = "Duration"
        }

        object Table {
            val CREATE_TABLE = """CREATE TABLE $TABLE_NAME (
            ${Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${Columns.TIMING_TASK_ID} INTEGER NOT NULL,
            ${Columns.TIMING_START_TIME} INTEGER,
            ${Columns.TIMING_DURATION} INTEGER);""".replaceIndent(" ")
        }

        fun getId(uri: Uri): Long {
            return ContentUris.parseId(uri)
        }

        fun buildUriFromId(id: Long): Uri {
            return ContentUris.withAppendedId(CONTENT_URI, id)
        }

    }

    object CurrentTimingContract {

        internal const val TABLE_NAME = "vwCurrentTiming"

        val CONTENT_URI = Uri.withAppendedPath(
            CONTENT_AUTHORITY_URI,
            TABLE_NAME
        )

        const val CONTENT_TYPE =
            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
        const val CONTENT_ITEM_TYPE =
            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

        object Columns {
            // Don't have their own IDs
            const val TIMING_ID = TimingsContract.Columns.ID
            const val TASK_ID = TimingsContract.Columns.TIMING_TASK_ID
            const val START_TIME = TimingsContract.Columns.TIMING_START_TIME
            const val TASK_NAME = TasksContract.Columns.TASK_NAME
        }

//        object Table {
//            val CREATE_TABLE = """CREATE TABLE $TABLE_NAME (
//            ${Columns.ID} INTEGER PRIMARY KEY NOT NULL,
//            ${Columns.TIMING_TASK_ID} INTEGER NOT NULL,
//            ${Columns.TIMING_START_TIME} INTEGER,
//            ${Columns.TIMING_DURATION} INTEGER);""".replaceIndent(" ")
//        }

    }



//    object DurationsContract {
//
//        internal const val TABLE_NAME = "Durations"
//
//        const val CONTENT_TYPE =
//            "${ContentResolver.CURSOR_DIR_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.${TasksContract.TABLE_NAME}"
//        const val CONTENT_ITEM_TYPE =
//            "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/vnd.$CONTENT_AUTHORITY.${TasksContract.TABLE_NAME}"
//    }




}