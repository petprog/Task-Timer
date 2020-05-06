package com.android.petprog.tasktimer.adapter

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.petprog.tasktimer.R
import com.android.petprog.tasktimer.model.Task
import com.android.petprog.tasktimer.database.TaskTimerContract
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_item.view.*

private const val TAG = "CursorRecyclerViewA"

class TaskViewHolder(override val containerView: View) :
    RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) {
        containerView.taskItemName.text = task.name
        containerView.taskItemDescription.text = task.description
        containerView.taskItemEdit.visibility = View.VISIBLE
        containerView.taskItemDelete.visibility = View.VISIBLE

        containerView.taskItemEdit.setOnClickListener {
            Log.d(TAG, "edit button tapped. task name is ${task.name}")
            listener.onEditClick(task)
        }

        containerView.taskItemDelete.setOnClickListener {
            Log.d(TAG, "delete button tapped. task name is ${task.name}")
            listener.onDeleteClick(task)
        }
        containerView.setOnLongClickListener {
            Log.d(TAG, "onLongClick: task name is ${task.name}")
            listener.onTaskLongClick(task)
            true
        }
    }


}

class CursorRecyclerViewAdapter(private var cursor: Cursor?, private val listener: OnTaskClickListener) :
    RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_item, parent, false)
        return TaskViewHolder(view)
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        return if (cursor == null || cursor.count == 0) 1 else cursor.count
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val cursor = cursor

        // if the cursor was null. Fresh app
        if (cursor == null || cursor.count == 0) {
            holder.containerView.taskItemName.setText(R.string.instruction_title)
            holder.containerView.taskItemDescription.setText(R.string.instruction_message)
            holder.containerView.taskItemDelete.visibility = View.GONE
            holder.containerView.taskItemEdit.visibility = View.GONE
        } else {
            // if we can move the position of our cursor
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor position $position")
            }

            // create a Task object from the data in the cursor
            val task = Task(
                cursor.getString(cursor.getColumnIndex(TaskTimerContract.TasksContract.Columns.TASK_NAME)),
                cursor.getString(cursor.getColumnIndex(TaskTimerContract.TasksContract.Columns.TASK_DESCRIPTION)),
                cursor.getInt(cursor.getColumnIndex(TaskTimerContract.TasksContract.Columns.TASK_SORT_ORDER))
            )
            task.id =
                cursor.getLong(cursor.getColumnIndex(TaskTimerContract.TasksContract.Columns.ID))

            holder.bind(task, listener)
        }
    }

    /**
     * swap a new cursor, returning the old cursor
     * The returned old cursor
     *
     * @param newCursor The new cursor to be used
     * @return the previous set cursor, or null if there wasn't one
     *
     * If the new cursor is the same instance as the previously set cursor then
     * it will null is also returned
     */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if(newCursor === cursor) {
            return null
        }
        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            // notify the observer about the new cursor
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }

}