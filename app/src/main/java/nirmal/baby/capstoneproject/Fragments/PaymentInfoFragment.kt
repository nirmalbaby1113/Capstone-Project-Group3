package nirmal.baby.capstoneproject.Fragments

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.CardPaymentActivity
import nirmal.baby.capstoneproject.CreateTaskActivity
import nirmal.baby.capstoneproject.R

class PaymentInfoFragment: Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var licenseStatus: TextView
    private lateinit var btnVerify: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var licenseNumberField: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_info, container, false)

        licenseStatus = view.findViewById(R.id.licenseVerificationTextView)
        btnVerify = view.findViewById(R.id.btnVerifyLicense)
        progressBar = view.findViewById(R.id.loadingProgressBarLicenseVerification)
        licenseNumberField = view.findViewById(R.id.editTextLicenseNumber)

        initialVisibility(progressBar, licenseStatus, btnVerify, licenseNumberField)
        fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)

        return view
    }

    private fun initialVisibility(progressBar: ProgressBar, licenseStatus: TextView, btnVerify: Button, licenseNumberField: EditText) {
        progressBar.visibility = View.VISIBLE
        licenseStatus.visibility = View.GONE
        btnVerify.visibility = View.GONE
        licenseNumberField.visibility = View.GONE
    }

    // Function to fetch data from Firestore
    private fun fetchData(btnVerify:Button, progressBar:ProgressBar, licenseStatus:TextView, licenseNumberField:EditText, view: View) {
        // Get current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid

        currentUser?.let { user ->
            for (userInfo in user.providerData) {
                when (userInfo.providerId) {
                    "password" -> {
                        val userRef =
                            userId?.let {
                                FirebaseFirestore.getInstance().collection("users").document(
                                    it
                                )
                            }

                        userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                            val verificationStatus = documentSnapshot.getBoolean("verification")
                            val licenseNo = documentSnapshot.getString("licenseNumber")

                            if (licenseNo == null) {
                                Log.d("paymentInfo","False")
                                btnVerify.isEnabled = true
                                btnVerify.setBackgroundResource(R.drawable.round_button_task_history_active)
                                btnVerify.setOnClickListener {
                                    addLicenseNumberToFireStore(view)
                                }
                            } else {
                                Log.d("paymentInfo","True")
                                btnVerify.isEnabled = false
                                licenseStatus.text = "Status: Verification InProgress"
                                btnVerify.setBackgroundResource(R.drawable.round_button_task_history_inactive)
                            }
                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                        }?.addOnFailureListener { exception ->
                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                            btnVerify.setOnClickListener {
                                progressBar.visibility = View.VISIBLE
                                licenseStatus.visibility = View.GONE
                                btnVerify.visibility = View.GONE
                                licenseNumberField.visibility = View.GONE
                                fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)
                            }
                        }
                    }
                    "google.com" -> {
                        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                        val googleAccountId = account?.id
                        val userRef = googleAccountId?.let {
                            FirebaseFirestore.getInstance().collection("users").document(
                                it
                            )
                        }
                        userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                            val verificationStatus = documentSnapshot.getBoolean("verification")
                            val licenseNo = documentSnapshot.getString("licenseNumber")
                            if (licenseNo == null) {
                                Log.d("paymentInfo","False")
                                btnVerify.isEnabled = true
                                btnVerify.setBackgroundResource(R.drawable.round_button_task_history_active)
                                btnVerify.setOnClickListener {
                                    addLicenseNumberToFireStore(view)
                                }
                            } else {
                                Log.d("paymentInfo","True")
                                btnVerify.isEnabled = false
                                licenseStatus.text = "Status: Verification InProgress"
                                btnVerify.setBackgroundResource(R.drawable.round_button_task_history_inactive)
                            }

                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                        }?.addOnFailureListener { exception ->
                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                            btnVerify.setOnClickListener {
                                progressBar.visibility = View.VISIBLE
                                licenseStatus.visibility = View.GONE
                                btnVerify.visibility = View.GONE
                                licenseNumberField.visibility = View.GONE
                                fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)
                            }
                        }
                    }
                }
            }
        } ?: run {
            // User is not logged in
            Log.d("paymetnInfo", "User is not logged in")
        }
        userId?.let { uid ->

        }
    }




    private fun addLicenseNumberToFireStore(view: View) {
        val licenseRequired: TextView = view.findViewById(R.id.licenseNumberTextViewRequired)
        val licenseNumberField: EditText = view.findViewById(R.id.editTextLicenseNumber)
        val licenseNumber = licenseNumberField.text.toString()

        // Getting current user ID
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId = currentUser?.uid
        val googleSignInAccount = GoogleSignIn.getLastSignedInAccount(requireContext())

        if (licenseNumberField.text.isEmpty()) {
            licenseRequired.visibility = View.VISIBLE
        }else{
            licenseRequired.visibility = View.INVISIBLE

            currentUser?.let { user ->
                for (userInfo in user.providerData) {
                    when (userInfo.providerId) {
                        "password" -> {
                            // User logged in using Firebase Email/Password authentication
                            userId?.let {
                                val userRef = firestore.collection("users").document(it)
                                userRef.update(
                                    mapOf(
                                        "licenseNumber" to licenseNumber,
                                        "licenseVerified" to false
                                    )
                                ).addOnSuccessListener {
                                    // Handle success
                                    showStatusPopup(true)

                                }.addOnFailureListener { e ->
                                    // Handle failures
                                    showStatusPopup(false)
                                }
                            }

                        }
                        "google.com" -> {
                            googleSignInAccount?.let { account ->
                                val googleAccountId = account.id
                                Log.d("paymentInfo", "Google Account ID: $googleAccountId")
                                val userRef = googleAccountId?.let {
                                    firestore.collection("users").document(
                                        it
                                    )
                                }
                                userRef?.update(
                                    mapOf(
                                        "licenseNumber" to licenseNumber,
                                        "licenseVerified" to false
                                    )
                                )?.addOnSuccessListener {
                                    // Handle success
                                    showStatusPopup(true)
                                }?.addOnFailureListener { e ->
                                    // Handle failures
                                    showStatusPopup(false)
                                }
                            }

                        }

                    }
                }
            } ?: run {
                // User is not logged in
                Log.d("LoginInfo", "User is not logged in")
            }
        }
    }


    private fun showStatusPopup(success: Boolean) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.status_popup)
        val okButton = dialog.findViewById<Button>(R.id.okButton)
        val retryButton = dialog.findViewById<Button>(R.id.retryButton)
        val txtView = dialog.findViewById<TextView>(R.id.statusMessageTextView)
        txtView.visibility = View.VISIBLE

        if (success) {
            Log.d("paymentInfo","Status If")
            retryButton.visibility = View.GONE
            okButton.visibility = View.VISIBLE
            txtView.text = "Verification in Progress.."
            okButton.text = "OK"
            okButton.setOnClickListener {
                dialog.dismiss()
            }
        } else {
            Log.d("paymentInfo","Status Else")
            txtView.text = "PLease try again.."
            okButton.visibility = View.GONE
            retryButton.visibility = View.VISIBLE
            retryButton.setOnClickListener {
                dialog.dismiss()
            }
        }

        dialog.show()
    }



}