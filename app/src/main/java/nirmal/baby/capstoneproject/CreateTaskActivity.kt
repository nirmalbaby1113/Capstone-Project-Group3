package nirmal.baby.capstoneproject

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.TimePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CreateTaskActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        var selectedPriority: String = "Medium"
        val closeTextView = findViewById<TextView>(R.id.txtViewClose)
        val dateTextView = findViewById<TextView>(R.id.dateTextView)
        val timeTextView = findViewById<TextView>(R.id.timeTextView)
        val publishButton = findViewById<Button>(R.id.btnPublishTask)
        val radioGroupPriority: RadioGroup = findViewById(R.id.radioGroupPriority)
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        dateTextView.text = getCurrentDate()
        timeTextView.text = getCurrentTime()



        // Set a listener to listen for changes in the RadioGroup
        radioGroupPriority.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioButtonLow -> selectedPriority = "Low"
                R.id.radioButtonMedium -> selectedPriority = "Medium"
                R.id.radioButtonHigh -> selectedPriority = "High"
            }
        }

        closeTextViewFunctionality(closeTextView)

        dateTextView.setOnClickListener {
            showDatePicker(dateTextView)
        }

        timeTextView.setOnClickListener {
            showTimePicker(timeTextView, dateTextView)
        }


        publishButton.setOnClickListener {
            publishTask(selectedPriority)
        }


    }

    private fun closeTextViewFunctionality(closeTextView: TextView){
        closeTextView.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }

    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("E, d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    private fun showDatePicker(dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker?, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
                val selectedDate = formatDate(selectedYear, selectedMonth, selectedDay)
                dateTextView.text = selectedDate
            },
            year,
            month,
            day
        )

        // Disable past dates
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        val dateFormat = SimpleDateFormat("E, d MMMM yyyy", Locale.getDefault())
        return dateFormat.format(calendar.time)
    }


    private fun showTimePicker(timeTextView: TextView, dateTextView: TextView) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val selectedDate = dateTextView.text.toString()
        val currentDate = getCurrentDate()

        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker?, selectedHour: Int, selectedMinute: Int ->
                if (selectedDate == currentDate && selectedHour < calendar.get(Calendar.HOUR_OF_DAY)) {
                    timeTextView.text = "Invalid Time"
                } else {
                    // Set the selected time
                    val selectedTime = formatTime(selectedHour, selectedMinute)
                    timeTextView.text = selectedTime
                }
            },
            hour,
            minute,
            false // Set to false to display 24-hour format
        )


        timePickerDialog.show()
    }



    private fun formatTime(hour: Int, minute: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)

        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return timeFormat.format(calendar.time)
    }

    private fun publishTask(selectedPriority: String) {
        // Get references to UI elements
        val taskTitleTextView = findViewById<TextView>(R.id.taskNameTextView)
        val taskTitle = findViewById<EditText>(R.id.editTextTaskName)
        val taskTitleRequired = findViewById<TextView>(R.id.taskNameRequired)
        val taskDescTextView = findViewById<TextView>(R.id.taskDescTextView)
        val taskDescription = findViewById<EditText>(R.id.editTextTaskDescription)
        val taskDescRequired = findViewById<TextView>(R.id.taskDescRequired)
        val taskAmtTextView = findViewById<TextView>(R.id.taskAmountTextView)
        val taskAmt = findViewById<EditText>(R.id.editTextTaskAmount)
        val taskAmtRequired = findViewById<TextView>(R.id.taskAmountRequired)
        val taskTipTextView = findViewById<TextView>(R.id.taskTipTextView)
        val taskTip = findViewById<EditText>(R.id.editTextTaskTip)
        val taskTipRequired = findViewById<TextView>(R.id.taskTipRequired)
        val taskDocs = findViewById<EditText>(R.id.editTextTaskDocuments).text.toString()
        val scrollViewCreateTask = findViewById<ScrollView>(R.id.createTaskScrollView)
        val dateText = findViewById<TextView>(R.id.dateTextView)
        val timeText = findViewById<TextView>(R.id.timeTextView)







        // Validating fields
        if (taskTitle.text.toString().isEmpty()){
            taskTitleRequired.visibility = View.VISIBLE
            taskDescRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.GONE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(taskTitleTextView, scrollViewCreateTask)
        }else if (taskDescription.text.toString().isEmpty()){
            taskDescRequired.visibility = View.VISIBLE
            taskTitleRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.GONE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(taskDescTextView, scrollViewCreateTask)
        }else if (taskAmt.text.toString().isEmpty()){
            taskAmtRequired.visibility = View.GONE
            taskDescRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.VISIBLE
            taskTipRequired.visibility = View.GONE
            scrollScrollViewToView(taskAmtTextView, scrollViewCreateTask)
        }else if (taskTip.text.toString().isEmpty()){
            taskAmtRequired.visibility = View.GONE
            taskDescRequired.visibility = View.GONE
            taskAmtRequired.visibility = View.GONE
            taskTipRequired.visibility = View.VISIBLE
            scrollScrollViewToView(taskTipTextView, scrollViewCreateTask)
        }else {
            // Show loading icon
            findViewById<View>(R.id.loadingView).visibility = View.VISIBLE

            // Create a data object to be stored in Firestore
            val taskData = hashMapOf(
                "title" to taskTitle.text.toString(),
                "description" to taskDescription.text.toString(),
                "amount" to taskAmt.text.toString(),
                "tip" to taskTip.text.toString(),
                "priority" to selectedPriority,
                "docs" to taskDocs,
                "dateDue" to dateText.text.toString(),
                "timeDue" to timeText.text.toString(),
                "createdBy" to firebaseAuth.currentUser?.uid // Assuming you want to associate the task with the current user
            )

            // Add the data to Firestore
            firestore.collection("tasks")
                .add(taskData)
                .addOnSuccessListener {
                    // Data added successfully
                    // Show pop-up with "OK" button
                    showStatusPopup(true)
                }
                .addOnFailureListener {
                    // Handle errors
                    // Show pop-up with "Retry" button
                    showStatusPopup(false)
                }
        }


    }

    private fun scrollScrollViewToView(view: View, scrollView: ScrollView) {
        // Get the Y position of the view relative to its parent
        val yPos = view.y.toInt() - 128

        // Scroll the ScrollView to the Y position
        scrollView.smoothScrollTo(0, yPos)
    }

    private fun showStatusPopup(success: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.status_popup)

        val okButton = dialog.findViewById<Button>(R.id.okButton)
        val retryButton = dialog.findViewById<Button>(R.id.retryButton)

        if (success) {
            // Task published successfully, show "OK" button
            retryButton.visibility = View.GONE
            okButton.setOnClickListener {
                dialog.dismiss()

            }
        } else {
            // Error in publishing task, show "Retry" button
            okButton.visibility = View.GONE
            retryButton.setOnClickListener {
                dialog.dismiss()

            }
        }

        dialog.show()
    }



}