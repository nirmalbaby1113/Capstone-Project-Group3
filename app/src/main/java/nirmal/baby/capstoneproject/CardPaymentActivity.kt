package nirmal.baby.capstoneproject

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Handler
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.json.responseJson
import com.github.kittinunf.result.onError
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import nirmal.baby.capstoneproject.AdapterClass.SavedCardsAdapter
import nirmal.baby.capstoneproject.Data.SavedCard
import org.json.JSONException
import org.json.JSONObject
import com.github.kittinunf.result.Result
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text


class CardPaymentActivity : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var cardPayBtn: Button
    private lateinit var title : String
    private lateinit var description : String
    private lateinit var amount : String
    private lateinit var tip : String
    private lateinit var priority: String
    private lateinit var docs:String
    private lateinit var timeDue: String
    private lateinit var dateDue: String
    private lateinit var createdBy: String
    private lateinit var txtViewTotalTaskAmount: TextView
    private lateinit var txtViewTipTask: TextView
    private lateinit var txtViewTaskAmount: TextView
    private lateinit var imgBackToCreateTask: ImageView
    private lateinit var fireStore: FirebaseFirestore
    private var taskData: HashMap<String, String> = hashMapOf()
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutCardPaymentbasic: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        cardPayBtn = findViewById(R.id.btnPayCard)
        txtViewTotalTaskAmount = findViewById(R.id.txtViewTotalTaskValue)
        txtViewTaskAmount = findViewById(R.id.txtViewTaskAmountValue)
        txtViewTipTask = findViewById(R.id.txtViewTaskTipValue)
        imgBackToCreateTask = findViewById(R.id.backToCreateTask)
        fireStore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.cardPaymentProgressBar)
        layoutCardPaymentbasic = findViewById(R.id.cardPaymentLayout)

        imgBackToCreateTask.setOnClickListener {
            startActivity(Intent(this,CreateTaskActivity::class.java))
            finish()
        }

        val receivedData = intent.extras
        receivedData?.let {
            title = it.getString("title").toString()
            description = it.getString("description").toString()
            amount = it.getString("amount").toString()
            tip = it.getString("tip").toString()
            priority = it.getString("priority").toString()
            docs = it.getString("docs").toString()
            dateDue = it.getString("dateDue").toString()
            timeDue = it.getString("timeDue").toString()
            createdBy = it.getString("createdBy").toString()
        }

        taskData = hashMapOf(
            "acceptedBy" to "",
            "title" to title,
            "description" to description,
            "amount" to amount,
            "tip" to tip,
            "priority" to priority,
            "docs" to docs,
            "dateDue" to dateDue,
            "timeDue" to timeDue,
            "status" to "In Progress",
            "createdBy" to createdBy//firebaseAuth.currentUser?.uid // Assuming you want to associate the task with the current user
        )

        txtViewTotalTaskAmount.text = "CA$${amount.toInt() + tip.toInt()}"
        txtViewTaskAmount.text = "CA$$amount"
        txtViewTipTask.text = "CA$$tip"




        cardPayBtn.setOnClickListener {
            layoutCardPaymentbasic.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            getDetails(amount.toInt() + tip.toInt())
        }


    }

    private fun getDetails(amount: Int){
        Log.d("CardPaymentActivity","getDetails")
        "https://us-central1-in-class-f09db.cloudfunctions.net/helloWorld?amt=${amount.toInt()*100}".httpPost().responseJson { _, _, result ->
            if (result is Result.Success) {
                val responseJson = result.get().obj()
                paymentIntentClientSecret = responseJson.getString("paymentIntent")
                customerConfig = PaymentSheet.CustomerConfiguration(
                    responseJson.getString("customer"),
                    responseJson.getString("ephemeralKey")
                )

                val publishableKey = responseJson.getString("publishableKey")
                PaymentConfiguration.init(this, publishableKey)

                runOnUiThread {
                    showStripePaymentSheet()
                }
            }else{
                layoutCardPaymentbasic.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Log.d("CardPaymentActivity","getDetails Else")
            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        // implemented in the next steps
        when(paymentSheetResult) {
            is PaymentSheetResult.Canceled -> {
                print("Canceled")
                layoutCardPaymentbasic.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                Log.d("CardPaymentActivity","Success")
            }
            is PaymentSheetResult.Failed -> {
                Log.d("CardPaymentActivity","Failed: ${paymentSheetResult.error}")
                layoutCardPaymentbasic.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
            is PaymentSheetResult.Completed -> {
                // Display for example, an order confirmation screen
                Log.d("CardPaymentActivity","Completed")
                writePaymentDetailsToFirestore()
            }
        }
    }

    private fun showStripePaymentSheet(){
        paymentSheet.presentWithPaymentIntent(
            paymentIntentClientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "My merchant name",
                customer = customerConfig,
                // Set allowsDelayedPaymentMethods to true if your business handles
                // delayed notification payment methods like US bank accounts.
                allowsDelayedPaymentMethods = true
            )
        )
    }

    private fun writePaymentDetailsToFirestore() {

        val paymentDetails = hashMapOf(
            "amount" to (amount.toInt() + tip.toInt()),
            "currency" to "CAD",
            "paymentMethod" to "Card",
            "payerId" to createdBy,
            "status" to "Completed",
            // Add other details as needed
        )

        fireStore.collection("payments")
            .add(paymentDetails)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val documentReference = task.result
                    Log.d("CardPaymentActivity", "Payment details written with ID: ${documentReference?.id}")
                    writeTaskDetailsToFirestore()
                } else {
                    val e = task.exception
                    Log.e("CardPaymentActivity", "Error adding payment details", e)
                    layoutCardPaymentbasic.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    // Handle the error appropriately
                }
            }
    }

    private fun showStatusPopup(success: Boolean) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.status_popup)

        val okButton = dialog.findViewById<Button>(R.id.okButton)
        val retryButton = dialog.findViewById<Button>(R.id.retryButton)
        val txtView = dialog.findViewById<TextView>(R.id.statusMessageTextView)

        if (success) {
            // Task published successfully, show "OK" button
            retryButton.visibility = View.GONE
            txtView.text = "Payment Successful"
            okButton.setOnClickListener {
                startActivity(Intent(this,CreateTaskActivity::class.java))
                dialog.dismiss()
                //clearEditTextFields()
            }
        } else {
            // Error in publishing task, show "Retry" button
            okButton.visibility = View.GONE
            retryButton.setOnClickListener {
                dialog.dismiss()
                layoutCardPaymentbasic.visibility = View.VISIBLE
                progressBar.visibility = View.GONE

            }
        }

        dialog.show()
    }

    private fun writeTaskDetailsToFirestore(){

        // Add the data to Firestore
        fireStore.collection("tasks")
            .add(taskData)
            .addOnSuccessListener {
                // Data added successfully
                // Show pop-up with "OK" button
                //view?.findViewById<View>(R.id.loadingView)?.visibility = View.INVISIBLE
                showStatusPopup(true)
            }
            .addOnFailureListener {
                // Handle errors
                // Show pop-up with "Retry" button
                showStatusPopup(false)
            }
    }

}