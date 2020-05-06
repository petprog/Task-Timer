package com.android.petprog.tasktimer.viewmodel

import android.app.Application
import android.content.ContentValues
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.petprog.tasktimer.database.TaskTimerContract.CurrentTimingContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TasksContract
import com.android.petprog.tasktimer.database.TaskTimerContract.TimingsContract
import com.android.petprog.tasktimer.model.Task
import com.android.petprog.tasktimer.model.Timing
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val TAG = "TaskTimerViewModel"

class TaskTimerViewModel(application: Application) : AndroidViewModel(application) {

    private val contentObserver = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            Log.d(TAG, "contentObserver.onChange: called uri is $uri")
            loadTask()
        }
    }
    private var currentTiming: Timing? = null

    private val databaseCursor = MutableLiveData<Cursor>()
    val cursor: LiveData<Cursor> = databaseCursor

    private val taskTiming = MutableLiveData<String>()
    val timing: LiveData<String> = taskTiming

    init {
        Log.d(TAG, "TaskTimerViewModel: starts")
        // tell the uri we want to observe changes
        getApplication<Application>().contentResolver.registerContentObserver(
            TasksContract.CONTENT_URI,
            true, contentObserver
        )

        currentTiming = retrieveTiming()
        loadTask()
    }

    private fun loadTask() {
        // projection (column to query out)
        val projection = arrayOf(
            TasksContract.Columns.ID,
            TasksContract.Columns.TASK_NAME,
            TasksContract.Columns.TASK_DESCRIPTION,
            TasksContract.Columns.TASK_SORT_ORDER
        )
        val sortOrder =
            "${TasksContract.Columns.TASK_SORT_ORDER}, ${TasksContract.Columns.TASK_NAME}"
        GlobalScope.launch {
            val cursor = getApplication<Application>().contentResolver.query(
                TasksContract.CONTENT_URI,
                projection, null, null,
                sortOrder
            )
            databaseCursor.postValue(cursor)
        }
    }

    fun deleteTask(taskId: Long) {
        // because we are providing the id we don't need where clause or selections args
        Log.d(TAG, "Deleting task")
        GlobalScope.launch {
            getApplication<Application>().contentResolver?.delete(
                TasksContract.buildUriFromId(
                    taskId
                ), null, null
            )
        }
    }

    fun taskTime(task: Task) {
        Log.d(TAG, "taskTime: called")
        // timingRecord & currentTiming refer to the same instance
        val timingRecord = currentTiming
        if (timingRecord == null) {
            // no task being timed, start timing the new task now
            currentTiming = Timing(task.id)
            saveTiming(currentTiming!!)
        } else {
            // We have a task being timed saved it
            timingRecord.setDuration()
            saveTiming(currentTiming!!)

            if (task.id == timingRecord.taskId) {
                // current task was tapped the second time
                currentTiming = null
            } else {
                // a new task is being timed
                currentTiming = Timing(task.id)
                saveTiming(currentTiming!!)
            }
        }

        // update live data
        taskTiming.value = if (currentTiming != null) task.name else null
    }

    private fun saveTiming(currentTiming: Timing) {
        Log.d(TAG, "saveTiming called")

        // are we updating a row or inserting a new row?
        val inserting = (currentTiming.duration == 0L)

        val values = ContentValues().apply {
            if (inserting) {
                put(TimingsContract.Columns.TIMING_TASK_ID, currentTiming.taskId)
                put(TimingsContract.Columns.TIMING_START_TIME, currentTiming.startTime)
            }
            put(TimingsContract.Columns.TIMING_DURATION, currentTiming.duration)
        }

        GlobalScope.launch {
            if (inserting) {
                val uri = getApplication<Application>().contentResolver.insert(
                    TimingsContract.CONTENT_URI,
                    values
                )
                if (uri != null) {
                    currentTiming.id = TimingsContract.getId(uri)
                }
            } else {
                getApplication<Application>().contentResolver.update(
                    TimingsContract.buildUriFromId(
                        currentTiming.id
                    ), values, null, null
                )
            }
        }
    }

    fun saveTask(task: Task): Task {
        val values = ContentValues()

        if (task.name.isNotEmpty()) {
            values.put(TasksContract.Columns.TASK_NAME, task.name)
            values.put(TasksContract.Columns.TASK_DESCRIPTION, task.description)
            values.put(TasksContract.Columns.TASK_SORT_ORDER, task.sortOrder)

            if (task.id == 0L) {
                GlobalScope.launch {
                    val uri = getApplication<Application>().contentResolver?.insert(
                        TasksContract.CONTENT_URI,
                        values
                    )
                    if (uri != null) {
                        task.id = TasksContract.getId(uri)
                        Log.d(TAG, "saveTask: new id is ${task.id}")
                    }
                }
            } else {
                // task has an id, so we are updating
                GlobalScope.launch {
                    Log.d(TAG, "saveTask: updating task")
                    getApplication<Application>().contentResolver?.update(
                        TasksContract.buildUriFromId(
                            task.id
                        ), values, null, null
                    )
                }
            }
        }
        return task
    }

    private fun retrieveTiming(): Timing? {
        Log.d(TAG, "retrieveTiming called")

        val timing: Timing?

        val timingCursor: Cursor? = getApplication<Application>().contentResolver.query(
            CurrentTimingContract.CONTENT_URI,
            null, // passing null return all the columns.
            null,
            null,
            null
        )

        if (timingCursor != null && timingCursor.moveToFirst()) {
            val id =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TIMING_ID))
            val taskId =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_ID))
            val startTime =
                timingCursor.getLong(timingCursor.getColumnIndex(CurrentTimingContract.Columns.START_TIME))
            val name =
                timingCursor.getString(timingCursor.getColumnIndex(CurrentTimingContract.Columns.TASK_NAME))
            timing = Timing(taskId, startTime, id)

            // update live data
            taskTiming.value = name
        } else {
            timing = null
        }
        Log.d(TAG, "Retrieving Current Timing")
        timingCursor?.close()
        return timing
    }

    override fun onCleared() {
        getApplication<Application>().contentResolver.unregisterContentObserver(contentObserver)
    }
}