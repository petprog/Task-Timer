package com.android.petprog.tasktimer.model

import android.util.Log
import java.util.*

private const val TAG = "Timing"

class Timing(val taskId: Long, val startTime: Long = (Date().time/ 1000), var id: Long = 0) {

    var duration: Long = 0

    private set
    fun setDuration() {
        duration = Date().time/1000 - startTime //  working in seconds
        Log.d(TAG, "$taskId -> Start Time = $startTime | duration $duration")
    }


}