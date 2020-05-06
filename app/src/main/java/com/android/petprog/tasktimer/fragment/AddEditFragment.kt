package com.android.petprog.tasktimer.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.android.petprog.tasktimer.R
import com.android.petprog.tasktimer.model.Task
import com.android.petprog.tasktimer.viewmodel.TaskTimerViewModel
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "AddEditFragment"
private const val ARG_TASK = "task"


/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSaveClicked? = null

    private val viewModel: TaskTimerViewModel by activityViewModels()

    // do not perform initialization that involves UI elements
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                addOrEditName.setText(task.name)
                addOrEditDescription.setText(task.description)
                addOrEditSortOrder.setText(task.sortOrder.toString())
            } else {
                Log.d(TAG, "onViewCreated: No arguments, adding new record")
            }
        }
    }

    private fun taskFromUi(): Task {
        val sortOrder = if (addOrEditSortOrder.text.isNotEmpty()) {
            Integer.parseInt(addOrEditSortOrder.text.toString())
        } else {
            0
        }
        val newTask =
            Task(addOrEditName.text.toString(), addOrEditDescription.text.toString(), sortOrder)
        newTask.id = task?.id ?: 0
        return newTask
    }

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return (
                (newTask != task)
                        && (
                        newTask.name.isNotBlank()
                                || newTask.description.isNotBlank()
                                || newTask.sortOrder != 0
                        )
                )
    }

    private fun saveTask() {
        val newTask = taskFromUi()
        Log.d(TAG, "Task is ${task.toString()}")
        Log.d(TAG, "Task is ${newTask.toString()}")
        if (newTask != task) {
            Log.d(TAG, "saveTask: saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, "saveTask: id is ${task?.id}")
        }
    }

    // Where the fragment view is attached
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: called")
        super.onActivityCreated(savedInstanceState)

        if (listener is AppCompatActivity) {
            val actionBar = (listener as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addOrEditSave.setOnClickListener {
            saveTask()
            listener?.onSavedClick()
        }
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)
        if (context is OnSaveClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSaveClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null
    }

    interface OnSaveClicked {
        fun onSavedClick()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited, or null to add a new task.
         * @return A new instance of fragment AddEditFragment.
         */
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
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

}
