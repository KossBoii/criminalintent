package com.bignerdranch.android.criminalintent

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "CrimeFragment"
private const val ARG_CRIME_ID = "crime_id"
private const val DIALOG_DATE = "DialogDate"
private const val DIALOG_TIME = "DialogTime"
private const val REQUEST_DATE = 0
private const val REQUEST_TIME = 0

class CrimeFragment : Fragment(), DatePickerFragment.Callbacks, TimePickerFragment.Callbacks {
    private lateinit var crime: Crime
    private lateinit var titleField: EditText
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var solvedCheckBox: CheckBox
    private lateinit var requirePoliceCheckBox : CheckBox
    private val crimeDetailViewModel: CrimeDetailViewModel by lazy {
        ViewModelProviders.of(this).get(CrimeDetailViewModel::class.java)
    }

    override fun onDateSelected(date: Date) {
        crime.date = date
        updateUI()
    }

    override fun onTimeSelected(time: Long) {
        crime.date.time = time
        updateUI()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        crime = Crime()
        val crimeId: UUID = arguments?.getSerializable(ARG_CRIME_ID) as UUID
        Log.d(TAG, "args bundle crime Id: %crimeId")

        // Load crime from the database and display to UI
        crimeDetailViewModel.loadCrime(crimeId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_crime, container, false)

        titleField = view.findViewById(R.id.crime_title) as EditText
        dateButton = view.findViewById(R.id.crime_date) as Button
        timeButton = view.findViewById(R.id.crime_time) as Button
        solvedCheckBox = view.findViewById(R.id.crime_solved) as CheckBox
        requirePoliceCheckBox = view.findViewById(R.id.crime_call_police) as CheckBox

//        dateButton.apply {
//            text = crime.date.toString()
//            isEnabled = false
//        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crimeDetailViewModel.crimeLiveData.observe(
            viewLifecycleOwner,
            Observer { crime ->
                 crime?.let {
                    this.crime = crime
                    updateUI()
                }
            }
        )
    }

    override fun onStart() {
        super.onStart()

        val titleWatcher = object: TextWatcher {
            override fun beforeTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int)
            {

            }

            override fun onTextChanged(
                sequence: CharSequence?,
                start: Int,
                count: Int,
                after: Int)
            {
                crime.title = sequence.toString()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        }

        titleField.addTextChangedListener(titleWatcher)
        solvedCheckBox.apply {
            setOnCheckedChangeListener{ _, isChecked ->
                crime.isSolved = isChecked
            }
        }

        requirePoliceCheckBox.apply {
            setOnCheckedChangeListener{_, isChecked ->
                crime.requiresPolice = when(isChecked) {
                    true -> 1
                    false -> 0
                }
            }
        }

        dateButton.setOnClickListener {
//            DatePickerFragment().apply {
            DatePickerFragment.newInstance(crime.date).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_DATE)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_DATE)
            }
        }

        timeButton.setOnClickListener {
            TimePickerFragment.newInstance(crime.date.time).apply {
                setTargetFragment(this@CrimeFragment, REQUEST_TIME)
                show(this@CrimeFragment.requireFragmentManager(), DIALOG_TIME)
            }
        }

    }

    override fun onStop() {
        super.onStop()
        crimeDetailViewModel.saveCrime(crime)
    }

    private fun updateUI() {
        titleField.setText(crime.title)
//        dateButton.text = crime.date.toString()
        dateButton.text = DateFormat.format("EEE MMM dd, yyyy", crime.date)
        timeButton.text = DateFormat.format("HH:mm", crime.date)
//        solvedCheckBox.isChecked = crime.isSolved
        solvedCheckBox.apply {
            isChecked = crime.isSolved
            jumpDrawablesToCurrentState()
        }

        requirePoliceCheckBox.apply {
            isChecked = when(crime.requiresPolice) {
                0 -> false
                else -> true
            }
            Log.i(TAG, "${isChecked}")
            jumpDrawablesToCurrentState()
        }
    }


    companion object {
        fun newInstance(crimeId: UUID): CrimeFragment {
            val args = Bundle().apply {
                putSerializable(ARG_CRIME_ID, crimeId)
            }
            return CrimeFragment().apply {
                arguments = args
            }
        }
    }
}