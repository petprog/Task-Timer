package com.android.petprog.tasktimer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.android.petprog.tasktimer.dialog.AppDialog
import com.android.petprog.tasktimer.dialog.SettingsDialog
import com.android.petprog.tasktimer.fragment.*
import com.android.petprog.tasktimer.model.Task
import com.android.petprog.tasktimer.viewmodel.TaskTimerViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.fragment_main.*

private const val TAG = "MainActivity"
private const val DIALOG_ID_CANCEL_EDIT = 1

class MainActivity : AppCompatActivity(), AddEditFragment.OnSaveClicked,
    MainActivityFragment.OnTaskEdit, AppDialog.DialogEvents {

    // Whether or the activity is in 2-pane mode
    //i.e running in landscape in landscape or on a tablet
    private var mTwoPane: Boolean = false
    private var aboutDialog: AlertDialog? = null

    private val viewModel: TaskTimerViewModel by viewModels()

    override fun onTaskEdit(task: Task) {
        taskEditRequest(task)
    }

    override fun onSavedClick() {
        Log.d(TAG, "onSavedClick: called")
        removeEditPane(findFragmentById(R.id.taskFragmentContainer))
    }

    override fun onBackPressed() {
        val fragment = findFragmentById(R.id.taskFragmentContainer)
        if (fragment == null) {
            super.onBackPressed()
        } else {
//            removeEditPane(fragment)
            if ((fragment is AddEditFragment) && fragment.isDirty()) {
                showConfirmationDialog(
                    DIALOG_ID_CANCEL_EDIT,
                    getString(R.string.cancel_edit_dialog_message),
                    R.string.cancel_edit_dialog_pos_caption,
                    R.string.cancel_edit_dialog_neg_caption
                )
            } else {
                removeEditPane(fragment)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mTwoPane = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        Log.d(TAG, "onCreate: mTwoPane is $mTwoPane")

        val fragment = findFragmentById(R.id.taskFragmentContainer)
        if (fragment != null) {
            showEditPane()
        } else {
            taskFragmentContainer.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
            mainFragment.view?.visibility = View.VISIBLE
        }

//        testInsert()

        viewModel.timing.observe(this, Observer { timing ->
            currentTask.text = if (timing != null) {
                getString(R.string.timing_title_message, timing)
            } else {
                getString(R.string.no_task_message)
            }
        })
        Log.d(TAG, "onCreate: finished")
    }

    // To remove EditPane (FrameLayout)
    private fun removeEditPane(fragment: Fragment? = null) {
        Log.d(TAG, "removeEditPane called")
        if (fragment != null) {
            removeFragment(fragment)
        }
        // Set the visibility of right hand pane
        taskFragmentContainer.visibility = if (mTwoPane) View.INVISIBLE else View.GONE
        // and show the left hand pane
        mainFragment.view?.visibility = View.VISIBLE

        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun showEditPane() {
        // if there is an existing fragment to edit a task, make sure the panes are set correctly
        taskFragmentContainer.visibility = View.VISIBLE
        // therefore hide the mainFragment
        mainFragment.view?.visibility = if (mTwoPane) View.VISIBLE else View.GONE
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.menu_addTask -> taskEditRequest(null)
            R.id.menu_settings -> {
                val dialog = SettingsDialog()
                dialog.show(supportFragmentManager, null)
            }
            android.R.id.home -> {
                Log.d(TAG, "onOptionsItemSelected: home button pressed")
                val fragment = findFragmentById(R.id.taskFragmentContainer)
//                removeEditPane(fragment)
                if ((fragment is AddEditFragment) && fragment.isDirty()) {
                    showConfirmationDialog(
                        DIALOG_ID_CANCEL_EDIT,
                        getString(R.string.cancel_edit_dialog_message),
                        R.string.cancel_edit_dialog_pos_caption,
                        R.string.cancel_edit_dialog_neg_caption
                    )
                } else {
                    removeEditPane(fragment)
                }
            }
            R.id.menu_about -> showAboutDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("InflateParams")
    private fun showAboutDialog() {

        val messageView = layoutInflater.inflate(R.layout.about, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.app_name)
        builder.setIcon(R.mipmap.ic_launcher)

        builder.setPositiveButton(R.string.ok) { _, _ ->
            Log.d(TAG, "builder.setPositiveButton click")
            if (aboutDialog != null && aboutDialog?.isShowing == true) {
                aboutDialog?.dismiss()
            }
        }
        aboutDialog = builder
            .setView(messageView)
            .create()
        aboutDialog?.setCanceledOnTouchOutside(true)
        val aboutVersion: TextView = messageView.findViewById(R.id.about_version)
        aboutVersion.text = BuildConfig.VERSION_NAME

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val aboutWebUrl = messageView.findViewById<TextView>(R.id.about_weblink_url)
            aboutWebUrl.setOnClickListener { v ->
                val intent = Intent(Intent.ACTION_VIEW)
                val text = (v as TextView).text.toString()
                intent.data = Uri.parse(text)
                startActivity(intent)
            }
        }
//        val aboutWebUrl = messageView.findViewById<Te xtView>(R.id.about_weblink_url)
//        aboutWebUrl.setOnClickListener { v ->
//            val intent = Intent(Intent.ACTION_VIEW)
//            val text = (v as TextView).text.toString()
//            intent.data = Uri.parse(text)
//            startActivity(intent)
//        }

        aboutDialog?.show()
    }
//    @SuppressLint("InflateParams")
//    private fun showAboutDialog() {
//
//        val messageView = layoutInflater.inflate(R.layout.about, null, false)
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(R.string.app_name)
//        builder.setIcon(R.mipmap.ic_launcher)
//
//        aboutDialog = builder.setView(messageView).create()
//        aboutDialog?.setCanceledOnTouchOutside(true)
//
//        messageView.setOnClickListener {
//            Log.d(TAG, "messageView.setOnClickListener click")
//            if (aboutDialog != null && aboutDialog?.isShowing == true) {
//                aboutDialog?.dismiss()
//            }
//        }
//        val aboutVersion: TextView = messageView.findViewById(R.id.about_version)
//        aboutVersion.text = BuildConfig.VERSION_NAME
//
//        aboutDialog?.show()
//    }

    private fun taskEditRequest(task: Task?) {
        Log.d(TAG, "taskEditRequest: starts")
        replaceFragment(AddEditFragment.newInstance(task), R.id.taskFragmentContainer)
        showEditPane()
        Log.d(TAG, "Exiting taskEditRequest")
    }

    override fun onStart() {
        Log.d(TAG, "onStart: called")
        super.onStart()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        Log.d(TAG, "onRestoreInstanceState: called")
        super.onRestoreInstanceState(savedInstanceState)
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
        if (aboutDialog?.isShowing == true)
            aboutDialog?.dismiss() // this is why aboutDialog is a field
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult with dialog id: $dialogId")
        if (dialogId == DIALOG_ID_CANCEL_EDIT) {
            removeEditPane(findFragmentById(R.id.taskFragmentContainer))
        }
    }


//    private fun testUpdate() {
//        val values = ContentValues().apply {
//            put(TaskTimerContract.TasksContract.Columns.TASK_NAME, "Content Provider")
//            put(TaskTimerContract.TasksContract.Columns.TASK_DESCRIPTION, "Record content provider videos")
//        }
//
//        val taskUri = TaskTimerContract.TasksContract.buildUriFromId(4)
//        val rowAffected = contentResolver.update(taskUri, values, null, null)
//        Log.d(TAG, "Number of rows updated is $rowAffected")
//    }
//
//    private fun testQuery() {
////        val projection = arrayOf(
////            TasksContract.Columns.ID,
////            TasksContract.Columns.TASK_NAME,
////            TasksContract.Columns.TASK_SORT_ORDER
////        )
//        val sortColumn = TaskTimerContract.TasksContract.Columns.TASK_SORT_ORDER
////        val cursor = contentResolver.query(TasksContract.buildUriFromId(2),
//        val cursor = contentResolver.query(
//            TaskTimerContract.TasksContract.CONTENT_URI,
//            null,
//            null,
//            null,
//            sortColumn
//        )
//
//        if (cursor != null) {
//            Log.d(TAG, "***************************************")
//            cursor.use {
//                while (it.moveToNext()) {
//                    val id = it.getLong(0)
//                    val name = it.getString(1)
//                    val description = it.getString(2)
//                    val sortOrder = it.getString(3)
//                    val result =
////                            "ID $id, Name $name, SortOrder $sortOrder"
//                        "ID: $id, Name $name, Description $description, SortOrder $sortOrder"
//                    Log.d(TAG, "onCreate: reading data: $result")
//                }
//            }
//        }
//        Log.d(TAG, "***************************************")
//
//    }


//
//    private fun testInsertTwo() {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "New Task 1")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "Description 1")
//            put(TasksContract.Columns.TASK_SORT_ORDER, 1)
//        }
//
//        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
//        Log.d(TAG, "New row id (in uri) is $uri")
//        Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri!!)}")
//    }
//
//    private fun testUpdateTwo() {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_SORT_ORDER, 999)
//            put(TasksContract.Columns.TASK_DESCRIPTION, "For deletion")
//        }
//        val selection = "${TasksContract.Columns.TASK_SORT_ORDER} = ?"
//        val selectionArgs = arrayOf("99")
////        val taskUri = TasksContract.buildUriFromId(4)
//        val rowAffected = contentResolver.update(TasksContract.CONTENT_URI, values, selection, selectionArgs)
//        Log.d(TAG, "Number of rows updated is $rowAffected")
//    }
//
//    private fun testInsert() {
//        val values = ContentValues().apply {
//            put(TasksContract.Columns.TASK_NAME, "New Task 1")
//            put(TasksContract.Columns.TASK_DESCRIPTION, "Description 1")
//            put(TasksContract.Columns.TASK_SORT_ORDER, 1)
//        }
//
//        val uri = contentResolver.insert(TasksContract.CONTENT_URI, values)
//        Log.d(TAG, "New row id (in uri) is $uri")
//        Log.d(TAG, "id (in uri) is ${TasksContract.getId(uri!!)}")
//    }
//
//    private fun testDelete() {
//        val taskUri = TasksContract.buildUriFromId(3)
////        val taskUri = TasksContract.CONTENT_URI
//        val rowAffected = contentResolver.delete(taskUri, null, null)
//        Log.d(TAG, "Number of rows deleted is $rowAffected")
//    }
//
//    private fun testDeleteTwo() {
//        val selection = "${TasksContract.Columns.TASK_DESCRIPTION} = ?"
//        val selectionArgs = arrayOf("For deletion")
////        val taskUri = TasksContract.buildUriFromId(3)
//        val taskUri = TasksContract.CONTENT_URI
//        val rowAffected = contentResolver.delete(taskUri, selection, selectionArgs)
//        Log.d(TAG, "Number of rows deleted is $rowAffected")
//    }


}