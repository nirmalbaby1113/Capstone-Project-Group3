package nirmal.baby.capstoneproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
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


class CardPaymentActivity : AppCompatActivity() {

    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerConfig: PaymentSheet.CustomerConfiguration
    private lateinit var paymentIntentClientSecret: String
    private lateinit var amount: String
    private lateinit var payBtnSample: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_payment)

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        payBtnSample = findViewById(R.id.paySampleButton)


        // Set up the RecyclerView for displaying saved cards
        val recyclerViewSavedCards: RecyclerView = findViewById(R.id.recyclerViewSavedCards)
        recyclerViewSavedCards.layoutManager = LinearLayoutManager(this)

        // Dummy data for testing; replace with actual saved card data
        val savedCards = listOf(
            SavedCard("1234 5678 9012 3456", "12/24", "John Doe"),
            SavedCard("9876 5432 1098 7654", "05/22", "Jane Smith")
        )

        val savedCardsAdapter = SavedCardsAdapter(savedCards)
        recyclerViewSavedCards.adapter = savedCardsAdapter

        payBtnSample.setOnClickListener {
            amount = "101"
            getDetails()
        }

    }

    private fun getDetails(){
        "https://us-central1-in-class-f09db.cloudfunctions.net/helloWorld?amt=$amount".httpPost().responseJson { _, _, result ->
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
            }
        }
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
        val db = FirebaseFirestore.getInstance()
        val paymentDetails = hashMapOf(
            "amount" to amount,
            "currency" to "CAD",
            "paymentMethod" to "Card",
            "paymentIntent" to paymentIntentClientSecret,
            "payerId" to FirebaseAuth.getInstance().currentUser,
            "status" to "Completed",
            "timestamp" to FieldValue.serverTimestamp(),
            // Add other details as needed
        )

        db.collection("payments")
            .add(paymentDetails)
            .addOnSuccessListener { documentReference ->
                Log.d("CardPaymentActivity", "Payment details written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("CardPaymentActivity", "Error adding payment details", e)
            }
    }

}