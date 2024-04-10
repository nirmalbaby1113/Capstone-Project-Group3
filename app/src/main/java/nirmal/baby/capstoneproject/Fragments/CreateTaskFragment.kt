package nirmal.baby.capstoneproject.Fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.CardPaymentActivity
import nirmal.baby.capstoneproject.R
import java.text.SimpleDateFormat
import java.util.*

class CreateTaskFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_task, container, false)
        initViews(view)
        return view
    }

    private fun initViews(view: View) {
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val radioGroupPriority = view.findViewById<RadioGroup>(R.id.radioGroupPriority)
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = view.findViewById<TextView>(R.id.timeTextView)
        val publishButton = view.findViewById<Button>(R.id.btnPublishTask)

        setupDefaultDateTime(dateTextView, timeTextView)
        setDateTimeListeners(dateTextView, timeTextView)
        setPublishButtonListener(radioGroupPriority, view, publishButton)
        showTotalAmtInfoTextview(view)
    }

    private fun setupDefaultDateTime(dateTextView: TextView, timeTextView: TextView) {
        dateTextView.text = getCurrentDate()
        timeTextView.text = getCurrentTime()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("E, d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(Calendar.getInstance().time)
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(Calendar.getInstance().time)
    }

    private fun setDateTimeListeners(dateTextView: TextView, timeTextView: TextView) {
        dateTextView.setOnClickListener {
            showDatePicker(dateTextView)
        }

        timeTextView.setOnClickListener {
            showTimePicker(timeTextView, dateTextView)
        }
    }

    private fun showDatePicker(dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                dateTextView.text = selectedDate
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(year, month, day)
        }
        val dateFormat = SimpleDateFormat("E, d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun showTimePicker(timeTextView: TextView, dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                if (isTimeValid(selectedHour, selectedMinute)) {
                    val selectedTime = formatTime(selectedHour, selectedMinute)
                    timeTextView.text = selectedTime
                } else {
                    timeTextView.text = "Invalid Time"
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun isTimeValid(hour: Int, minute: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        return if (hour > currentHour) true
        else hour == currentHour && minute >= currentMinute
    }

    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    private fun setPublishButtonListener(
        radioGroupPriority: RadioGroup,
        view: View,
        publishButton: Button
    ) {
        publishButton.setOnClickListener {
            publishTask(getSelectedPriority(radioGroupPriority), view)
        }
    }

    private fun getSelectedPriority(radioGroupPriority: RadioGroup): String {
        return when (radioGroupPriority.checkedRadioButtonId) {
            R.id.radioButtonLow -> "Low"
            R.id.radioButtonMedium -> "Medium"
            R.id.radioButtonHigh -> "High"
            else -> "Medium"
        }
    }

    private fun publishTask(selectedPriority: String, view: View) {
        // Implementation of publishing task
        // You can put your implementation here
        val taskTitle = view.findViewById<EditText>(R.id.editTextTaskName).text.toString()
        val taskDescription = view.findViewById<EditText>(R.id.editTextTaskDescription).text.toString()
        val taskAmount = view.findViewById<EditText>(R.id.editTextTaskAmount).text.toString()
        val taskTip = view.findViewById<EditText>(R.id.editTextTaskTip).text.toString()
        val taskDocs = view.findViewById<EditText>(R.id.editTextTaskDocuments).text.toString()
        val dateText = view.findViewById<TextView>(R.id.dateTextView).text.toString()
        val timeText = view.findViewById<TextView>(R.id.timeTextView).text.toString()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (validateFields(taskTitle, taskDescription, taskAmount, taskTip)) {
            view.findViewById<View>(R.id.loadingView).visibility = View.VISIBLE
            val dataBundle = Bundle().apply {
                putString("title", taskTitle)
                putString("description", taskDescription)
                putString("amount", taskAmount)
                putString("tip", taskTip)
                putString("priority", selectedPriority)
                putString("docs", taskDocs)
                putString("dateDue", dateText)
                putString("timeDue", timeText)
                putString("createdBy", currentUser?.displayName ?: "unknown_user")
            }
            val intent = Intent(activity, CardPaymentActivity::class.java).apply {
                putExtras(dataBundle)
            }
            startActivity(intent)
        }
    }

    private fun validateFields(
        taskTitle: String,
        taskDescription: String,
        taskAmount: String,
        taskTip: String
    ): Boolean {
        val view = requireView()
        val taskTitleRequired = view.findViewById<TextView>(R.id.taskNameRequired)
        val taskDescRequired = view.findViewById<TextView>(R.id.taskDescRequired)
        val taskAmtRequired = view.findViewById<TextView>(R.id.taskAmountRequired)
        val taskTipRequired = view.findViewById<TextView>(R.id.taskTipRequired)
        val scrollViewCreateTask = view.findViewById<ScrollView>(R.id.createTaskScrollView)

        if (taskTitle.isEmpty()) {
            taskTitleRequired.visibility = View.VISIBLE
            taskDescRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.GONE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(view.findViewById(R.id.taskNameTextView), scrollViewCreateTask)
            return false
        } else if (taskDescription.isEmpty()) {
            taskDescRequired.visibility = View.VISIBLE
            taskTitleRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.GONE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(view.findViewById(R.id.taskDescTextView), scrollViewCreateTask)
            return false
        } else if (taskAmount.isEmpty()) {
            taskAmtRequired.visibility = View.VISIBLE
            taskDescRequired.visibility = View.GONE
            taskTitleRequired.visibility = View.GONE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(view.findViewById(R.id.taskAmountTextView), scrollViewCreateTask)
            return false
        } else if (taskTip.isEmpty()) {
            taskAmtRequired.visibility = View.GONE
            taskDescRequired.visibility = View.GONE
            taskTitleRequired.visibility = View.GONE
            taskTipRequired.visibility = View.VISIBLE
            scrollScrollViewToView(view.findViewById(R.id.taskTipTextView), scrollViewCreateTask)
            return false
        }
        return true
    }

    private fun scrollScrollViewToView(view: View, scrollView: ScrollView) {
        val yPos = view.y.toInt() - 128
        scrollView.smoothScrollTo(0, yPos)
    }

    private fun showTotalAmtInfoTextview(view: View) {
        val editTextTaskTip = view.findViewById<EditText>(R.id.editTextTaskTip)
        val taskTotalAmtInfo = view.findViewById<TextView>(R.id.taskTotalAmtInfo)
        val editTextTaskAmount = view.findViewById<EditText>(R.id.editTextTaskAmount)

        editTextTaskTip.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateTotalAmount(editTextTaskTip.text.toString(), editTextTaskAmount.text.toString(), taskTotalAmtInfo)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        editTextTaskAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                updateTotalAmount(editTextTaskTip.text.toString(), editTextTaskAmount.text.toString(), taskTotalAmtInfo)
            }
            override fun afterTextChanged(p0: Editable?) {}
        })
    }

    private fun updateTotalAmount(tipText: String, amountText: String, taskTotalAmtInfo: TextView) {
        if (!tipText.isBlank() && !amountText.isBlank()) {
            val totalAmount = tipText.toDouble() + amountText.toDouble()
            taskTotalAmtInfo.text = "Total Payable amount will be CAD $totalAmount "
            taskTotalAmtInfo.visibility = View.VISIBLE
        } else {
            taskTotalAmtInfo.visibility = View.GONE
        }
    }
}
