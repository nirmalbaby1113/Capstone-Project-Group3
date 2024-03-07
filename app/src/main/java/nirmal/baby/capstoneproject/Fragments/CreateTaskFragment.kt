package nirmal.baby.capstoneproject.Fragments

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.TimePicker
import androidx.fragment.app.Fragment
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import nirmal.baby.capstoneproject.CardPaymentActivity
import nirmal.baby.capstoneproject.MainActivity
import nirmal.baby.capstoneproject.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateTaskFragment : Fragment() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private var taskData: HashMap<String, String> = hashMapOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_task, container, false)

        showTotalAmtInfoTextview(view)
        var selectedPriority: String = "Medium"
        val dateTextView = view.findViewById<TextView>(R.id.dateTextView)
        val timeTextView = view.findViewById<TextView>(R.id.timeTextView)
        val publishButton = view.findViewById<Button>(R.id.btnPublishTask)
        val radioGroupPriority: RadioGroup = view.findViewById(R.id.radioGroupPriority)
        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        //Log.d("CreateTaskFragment","here ${PaymentSheet(requireActivity(), ::onPaymentSheetResult)}")

       // paymentSheet = PaymentSheet(requireActivity(), ::onPaymentSheetResult)


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



        dateTextView.setOnClickListener {
            showDatePicker(dateTextView)
        }

        timeTextView.setOnClickListener {
            showTimePicker(timeTextView, dateTextView)
        }


        publishButton.setOnClickListener {
            publishTask(selectedPriority, view)
        }


        return view
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
            requireContext(),
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
            requireContext(),
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

    private fun publishTask(selectedPriority: String, view: View) {
        // Get references to UI elements
        val taskTitleTextView = view.findViewById<TextView>(R.id.taskNameTextView)
        val taskTitle = view.findViewById<EditText>(R.id.editTextTaskName)
        val taskTitleRequired = view.findViewById<TextView>(R.id.taskNameRequired)
        val taskDescTextView = view.findViewById<TextView>(R.id.taskDescTextView)
        val taskDescription = view.findViewById<EditText>(R.id.editTextTaskDescription)
        val taskDescRequired = view.findViewById<TextView>(R.id.taskDescRequired)
        val taskAmtTextView = view.findViewById<TextView>(R.id.taskAmountTextView)
        val taskAmt = view.findViewById<EditText>(R.id.editTextTaskAmount)
        val taskAmtRequired = view.findViewById<TextView>(R.id.taskAmountRequired)
        val taskTipTextView = view.findViewById<TextView>(R.id.taskTipTextView)
        val taskTip = view.findViewById<EditText>(R.id.editTextTaskTip)
        val taskTipRequired = view.findViewById<TextView>(R.id.taskTipRequired)
        val taskDocs = view.findViewById<EditText>(R.id.editTextTaskDocuments).text.toString()
        val scrollViewCreateTask = view.findViewById<ScrollView>(R.id.createTaskScrollView)
        val dateText = view.findViewById<TextView>(R.id.dateTextView)
        val timeText = view.findViewById<TextView>(R.id.timeTextView)
        val currentUser = FirebaseAuth.getInstance().currentUser

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
            view.findViewById<View>(R.id.loadingView).visibility = View.VISIBLE

            //amountGlobal = taskAmt.text.toString().toInt() + taskTip.text.toString().toInt()
            // Create a data object to be stored in Firestore
            /*taskData = hashMapOf(
                "title" to taskTitle.text.toString(),
                "description" to taskDescription.text.toString(),
                "amount" to taskAmt.text.toString(),
                "tip" to taskTip.text.toString(),
                "priority" to selectedPriority,
                "docs" to taskDocs,
                "dateDue" to dateText.text.toString(),
                "timeDue" to timeText.text.toString(),
                "createdBy" to
            )*/
            //getDetails(amountGlobal)


            val dataBundle = Bundle().apply {
                putString("title", taskTitle.text.toString())
                putString("description", taskDescription.text.toString())
                putString("amount", taskAmt.text.toString())
                putString("tip", taskTip.text.toString())
                putString("priority", selectedPriority)
                putString("docs", taskDocs)
                putString("dateDue", dateText.text.toString())
                putString("timeDue", timeText.text.toString())
                putString("createdBy",(currentUser?.displayName ?: "unknown_user"))
            }
            val intent = Intent(activity, CardPaymentActivity::class.java).apply {
                putExtras(dataBundle)
            }
            startActivity(intent)
            //activity?.finish()



        }


    }

    private fun scrollScrollViewToView(view: View, scrollView: ScrollView) {
        // Get the Y position of the view relative to its parent
        val yPos = view.y.toInt() - 128

        // Scroll the ScrollView to the Y position
        scrollView.smoothScrollTo(0, yPos)
    }

    private fun showStatusPopup(success: Boolean) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.status_popup)

        val okButton = dialog.findViewById<Button>(R.id.okButton)
        val retryButton = dialog.findViewById<Button>(R.id.retryButton)

        if (success) {
            // Task published successfully, show "OK" button
            retryButton.visibility = View.GONE
            okButton.setOnClickListener {
                dialog.dismiss()
                //clearEditTextFields()
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

    private fun showTotalAmtInfoTextview(view: View){
        val editTextTaskTip: EditText = view.findViewById<EditText>(R.id.editTextTaskTip)
        val taskTotalAmtInfo: TextView = view.findViewById<TextView>(R.id.taskTotalAmtInfo)
        val editTextTaskAmount: EditText = view.findViewById<EditText>(R.id.editTextTaskAmount)
        var tipText = "0"
        var amountText = "0"

        editTextTaskTip.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                tipText = editTextTaskTip.text.toString()
                amountText = editTextTaskAmount.text.toString()
                // Check if the EditText is not empty, then show the TextView
                if (!p0.isNullOrBlank() && tipText.isNotEmpty() && amountText.isNotEmpty()) {
                    Log.d("CreateTaskFragment","Inside If Text Changed")
                    val totalAmount = tipText.toDouble() + amountText.toDouble()
                    taskTotalAmtInfo.text = "Total Payable amount will be CAD $totalAmount "
                    taskTotalAmtInfo.visibility = View.VISIBLE
                } else {
                    Log.d("CreateTaskFragment","Inside Else Text Changed (Tip Text) ${tipText.isNotEmpty()}")
                    Log.d("CreateTaskFragment","Inside Else Text Changed (Amount Text) ${amountText.isNotEmpty()}")
                    // Hide the TextView if the EditText is empty
                    taskTotalAmtInfo.visibility = View.GONE
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

        editTextTaskAmount.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val editTextTaskTip: EditText = view.findViewById<EditText>(R.id.editTextTaskTip)
                val taskTotalAmtInfo: TextView = view.findViewById<TextView>(R.id.taskTotalAmtInfo)
                val editTextTaskAmount: EditText = view.findViewById<EditText>(R.id.editTextTaskAmount)
                var tipText = "0"
                var amountText = "0"

                tipText = editTextTaskTip.text.toString()
                amountText = editTextTaskAmount.text.toString()
                // Check if the EditText is not empty, then show the TextView
                if (!p0.isNullOrBlank() && tipText.isNotEmpty() && amountText.isNotEmpty()) {
                    Log.d("CreateTaskFragment","Inside If Text Changed")
                    val totalAmount = tipText.toDouble() + amountText.toDouble()
                    taskTotalAmtInfo.text = "Total Payable amount will be CAD $totalAmount "
                    taskTotalAmtInfo.visibility = View.VISIBLE
                } else {
                    Log.d("CreateTaskFragment","Inside Else Text Changed (Tip Text) ${tipText.isNotEmpty()}")
                    Log.d("CreateTaskFragment","Inside Else Text Changed (Amount Text) ${amountText.isNotEmpty()}")
                    // Hide the TextView if the EditText is empty
                    taskTotalAmtInfo.visibility = View.GONE
                }

            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })

    }
/*
    private fun getDetails(amount: Int) {
        Log.d("CreateTaskFragment", "Inside getDetails")

        val activity = activity


        if (activity != null) {
            Log.d("CreateTaskFragment", "Inside getDetails if $amount")
            "https://us-central1-in-class-f09db.cloudfunctions.net/helloWorld?amt=${amount*100}"
                .httpPost().responseJson { _, _, result ->
                    Log.d("CreateTaskFragment", "Inside getDetails httpPost")
                    if (result is Result.Success) {
                        val responseJson = result.get().obj()
                        paymentIntentClientSecret = responseJson.getString("paymentIntent")
                        customerConfig = PaymentSheet.CustomerConfiguration(
                            responseJson.getString("customer"),
                            responseJson.getString("ephemeralKey")
                        )

                        val publishableKey = responseJson.getString("publishableKey")
                        PaymentConfiguration.init(activity, publishableKey)

                        activity.runOnUiThread {
                            Log.d("CreateTaskFragment", "Inside getDetails2")
                            showStripePaymentSheet()
                        }
                    }else{
                        Log.d("CreateTaskFragment", "Inside getDetails httpPost else $result")
                    }
                }
        } else {
            Log.e("CreateTaskFragment", "Activity is null")
        }
    }

    private fun showStripePaymentSheet(){
        Log.d("CreateTaskFragment","Inside showStripePaymentSheet")
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "My merchant name",
                customer = customerConfig,
                // Set `allowsDelayedPaymentMethods` to true if your business handles
                // delayed notification payment methods like US bank accounts.
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        // implemented in the next steps
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
                Log.d("CardPaymentActivity","Success")
            }
            is PaymentSheetResult.Failed -> {
                Log.d("CardPaymentActivity","Failed: ${paymentSheetResult.error}")
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                Log.d("CardPaymentActivity","Completed")
                writePaymentDetailsToFirestore()
            }
        }
    }*/

    /*private fun writePaymentDetailsToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val paymentDetails = hashMapOf(
            "amount" to amountGlobal.toString(),
            "currency" to "CAD",
            "paymentMethod" to "Card",
            "payerId" to (currentUser?.uid ?: "unknown_user"),
            "status" to "Completed",
        )

        Log.d("CreateTaskFragment","One: ${paymentDetails["amount"]}")
        Log.d("CreateTaskFragment","One: ${paymentDetails["payerId"]}")

        db.collection("payments")
            .add(paymentDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentReference = task.result
                    Log.d("CardPaymentActivity", "Payment details written with ID: ${documentReference?.id}")
                    writeTaskDetailsToFirestore()
                } else {
                    val e = task.exception
                    Log.e("CardPaymentActivity", "Error adding payment details", e)
                    // Handle the error appropriately
                }
            }
    }*/

   /* private fun writeTaskDetailsToFirestore(){

        // Add the data to Firestore
        firestore.collection("tasks")
            .add(taskData)
            .addOnSuccessListener {
                // Data added successfully
                // Show pop-up with "OK" button
                view?.findViewById<View>(R.id.loadingView)?.visibility = View.INVISIBLE
                showStatusPopup(true)
            }
            .addOnFailureListener {
                // Handle errors
                // Show pop-up with "Retry" button
                showStatusPopup(false)
            }
    }*/


    /*private fun clearEditTextFields() {
        val taskTitle = view?.findViewById<EditText>(R.id.editTextTaskName)
        val taskDescription = view?.findViewById<EditText>(R.id.editTextTaskDescription)
        val taskAmount = view?.findViewById<EditText>(R.id.editTextTaskAmount)
        val taskTip = view?.findViewById<EditText>(R.id.editTextTaskTip)
        val taskDocuments = view?.findViewById<EditText>(R.id.editTextTaskDocuments)

        taskTitle?.text?.clear()
        taskDescription?.text?.clear()
        taskAmount?.text?.clear()
        taskTip?.text?.clear()
        taskDocuments?.text?.clear()
    }
    */


}