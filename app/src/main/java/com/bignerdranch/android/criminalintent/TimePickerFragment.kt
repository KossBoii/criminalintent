package com.bignerdranch.android.criminalintent

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.telecom.Call
import android.text.format.DateFormat
import android.text.format.Time
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.math.abs
import kotlin.properties.Delegates

private const val ARG_TIME = "time"

class TimePickerFragment : DialogFragment() {

    private var time by Delegates.notNull<Long>()
    private var hr by Delegates.notNull<Int>()
    private var min by Delegates.notNull<Int>()

    interface Callbacks {
        fun onTimeSelected(time: Long)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val timeListener = TimePickerDialog.OnTimeSetListener {
            _:TimePicker, hourOfDay: Int, minute: Int ->

            val resultTime : Long = (time + abs(hr - hourOfDay) * 60 * 60 * 1000 + abs(min - minute) * 60 * 1000).toLong()
            targetFragment?.let { fragment ->
                (fragment as Callbacks).onTimeSelected(resultTime)
            }
        }

        time = arguments?.getSerializable(ARG_TIME) as Long

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time as Long
        hr = calendar.get(Calendar.HOUR_OF_DAY)
        min = calendar.get(Calendar.MINUTE)

        return TimePickerDialog(
            requireContext(),
            timeListener,
            hr,
            min,
            false
        )
    }


    companion object {
        fun newInstance(time: Long): TimePickerFragment {
            val args = Bundle().apply {
                putSerializable(ARG_TIME, time)
            }
            return TimePickerFragment().apply {
                arguments = args
            }
        }
    }
}