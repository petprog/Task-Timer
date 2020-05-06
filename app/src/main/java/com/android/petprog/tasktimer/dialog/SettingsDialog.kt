package com.android.petprog.tasktimer.dialog

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatDialogFragment
import com.android.petprog.tasktimer.R
import kotlinx.android.synthetic.main.settings_dialog.*
import kotlinx.android.synthetic.main.settings_dialog.view.*
import java.util.*

private const val TAG = "SettingsDialog"

const val SETTINGS_FIRST_DAY_OF_WEEK = "FirstDay"
const val SETTINGS_IGNORE_LESS_THAN = "IgnoreLessThan"
const val SETTINGS_DEFAULT_IGNORE_LESS_THAN = 0

private val deltas = intArrayOf(
    0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55,
    60, 120, 180, 240, 300, 360, 420, 480, 540, 600, 900, 1080, 2700
)

class SettingsDialog : AppCompatDialogFragment() {

    private val defaultFirstDayOfWeek = GregorianCalendar(Locale.getDefault()).firstDayOfWeek
    private var firstDay = defaultFirstDayOfWeek
    private var ignoreLessThan = SETTINGS_DEFAULT_IGNORE_LESS_THAN

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate called")
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.SettingsDialogStyle)
        retainInstance = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: called")
        return inflater.inflate(R.layout.settings_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: called")
        dialog?.setTitle(R.string.action_settings)

        // positive/ok button
        view.settings_dialog_ok_button.setOnClickListener {
            saveValues(view)
            dismiss()
        }

        ignoreSeconds.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress < 12) {
                    ignoreSecondsTitle.text = getString(
                        R.string.settingsIgnoreSecondsTitle,
                        deltas[progress],
                        resources.getQuantityString(R.plurals.settingsLittleUnits, deltas[progress])
                    )
                } else {
                    val minutes = deltas[progress] / 60
                    ignoreSecondsTitle.text = getString(
                        R.string.settingsIgnoreSecondsTitle,
                        minutes,
                        resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                    )
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // We don't need this
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // we dont need this too
            }
        })

        // negative/cancel button
        view.settings_dialog_cancel_button.setOnClickListener {
            dismiss()
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored called ")
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState == null) {
            readValue()
            firstDaySpinner.setSelection(firstDay - GregorianCalendar.SUNDAY)

            // index 0 -> 24
            val seekBarValue = deltas.binarySearch(ignoreLessThan)
            if (seekBarValue < 0) {
                throw IndexOutOfBoundsException("seekBarValue $seekBarValue not found in deltas array")
            }

            ignoreSeconds.max = deltas.size - 1
            Log.d(TAG, "onViewStateRestored: setting slider to $seekBarValue")
            ignoreSeconds.progress = seekBarValue

            if (ignoreLessThan < 60) {
                ignoreSecondsTitle.text = getString(
                    R.string.settingsIgnoreSecondsTitle,
                    ignoreLessThan,
                    resources.getQuantityString(
                        R.plurals.settingsLittleUnits,
                        ignoreLessThan
                    )// needs to know the quantity
                )
            }
            if (ignoreLessThan >= 60 && ignoreLessThan < 60 * 60) {
                val minutes = ignoreLessThan / 60
                ignoreSecondsTitle.text = getString(
                    R.string.settingsIgnoreSecondsTitle,
                    minutes,
                    resources.getQuantityString(R.plurals.settingsBigUnits, minutes)
                )
            }
        }


    }

    private fun readValue() {
        with(PreferenceManager.getDefaultSharedPreferences(context)) {
            firstDay = getInt(SETTINGS_FIRST_DAY_OF_WEEK, defaultFirstDayOfWeek)
            ignoreLessThan = getInt(SETTINGS_IGNORE_LESS_THAN, SETTINGS_DEFAULT_IGNORE_LESS_THAN)
        }
        Log.d(
            TAG,
            "readValue Retrieving first day = $firstDay and ignoreLessThan = $ignoreLessThan"
        )
    }

    private fun saveValues(view: View) {
        val newFirstDay = view.firstDaySpinner.selectedItemPosition + GregorianCalendar.SUNDAY
        val newIgnoreLessThan = deltas[view.ignoreSeconds.progress]

        Log.d(TAG, "saving first day: $newFirstDay, ignore seconds: $newIgnoreLessThan")

        with(PreferenceManager.getDefaultSharedPreferences(context).edit()) {
            if (newFirstDay != firstDay) {
                putInt(SETTINGS_FIRST_DAY_OF_WEEK, newFirstDay)
            }
            if (newIgnoreLessThan != ignoreLessThan) {
                putInt(SETTINGS_IGNORE_LESS_THAN, newIgnoreLessThan)
            }
            apply()
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }
}