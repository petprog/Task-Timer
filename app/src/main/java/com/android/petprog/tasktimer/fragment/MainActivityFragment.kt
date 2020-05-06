package com.android.petprog.tasktimer.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.petprog.tasktimer.BuildConfig
import com.android.petprog.tasktimer.R
import com.android.petprog.tasktimer.viewmodel.TaskTimerViewModel
import com.android.petprog.tasktimer.adapter.CursorRecyclerViewAdapter
import com.android.petprog.tasktimer.dialog.*
import com.android.petprog.tasktimer.model.Task
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivityFragment"

private const val DIALOG_DELETE_ID = 1
private const val DIALOG_TASK_ID = "task_id"


class MainActivityFragment : Fragment(), CursorRecyclerViewAdapter.OnTaskClickListener, AppDialog.DialogEvents {

//    companion object {
//        fun newInstance() = MainActivityFragment()
//    }

    private var listener: OnTaskEdit? = null

    interface OnTaskEdit {
        fun onTaskEdit(task: Task)
    }

//    private val viewModel by lazy { ViewModelProviders.of(requireActivity()).get(TaskTimerViewModel::class.j) }
    private val viewModel: TaskTimerViewModel by activityViewModels()
    private val mAdapter =
        CursorRecyclerViewAdapter(null, this)

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: called")
        super.onAttach(context)
        if (context is OnTaskEdit) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnTaskEdit")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        // swapCursor enable our adapter to get a new data
        // old cursor gets closed
        viewModel.cursor.observe(this, Observer { cursor -> mAdapter.swapCursor(cursor)?.close()})

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    // Just like onCreate() on Activity
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        super.onViewCreated(view, savedInstanceState)
        taskList.layoutManager = LinearLayoutManager(context)
        taskList.adapter = mAdapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated")
        super.onActivityCreated(savedInstanceState)
//        val viewModel : MainActivityViewModel by viewModels()

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: called")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: called")
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: called")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: called")
        super.onDetach()
    }

    override fun onEditClick(task: Task) {
        listener?.onTaskEdit(task)
    }

    override fun onDeleteClick(task: Task) {
        val args = Bundle().apply {
            putInt(DIALOG_ID, DIALOG_DELETE_ID)
            putString(DIALOG_MESSAGE, getString(R.string.delete_dialog_message_caption, task.id, task.name))
            putInt(DIALOG_POSITIVE_RID, R.string.delete_dialog_pos_caption)
            putInt(DIALOG_NEGATIVE_RID, R.string.delete_dialog_neg_caption)
            putLong(DIALOG_TASK_ID, task.id)
        }
        val dialog = AppDialog()
        dialog.arguments = args
        dialog.show(childFragmentManager, null)
    }

    override fun onTaskLongClick(task: Task) {
        Log.d(TAG, "onTaskLongClick: called")
        viewModel.taskTime(task)
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult is called with id: $dialogId")
        if(dialogId == DIALOG_DELETE_ID) {
            val taskId = args.getLong(DIALOG_TASK_ID)
            if(BuildConfig.DEBUG && taskId == 0L) throw AssertionError("Task ID is zero")
            viewModel.deleteTask(taskId)
        }
    }

}
