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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_info, container, false)

        val licenseStatus: TextView = view.findViewById(R.id.licenseVerificationTextView)
        val btnVerify: Button = view.findViewById(R.id.btnVerifyLicense)
        val progressBar: ProgressBar = view.findViewById(R.id.loadingProgressBarLicenseVerification)
        val licenseNumberField: EditText = view.findViewById(R.id.editTextLicenseNumber)

        progressBar.visibility = View.VISIBLE
        licenseStatus.visibility = View.GONE
        btnVerify.visibility = View.GONE
        licenseNumberField.visibility = View.GONE


        fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)



        return view
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
                        // User logged in using Firebase Email/Password authentication
                        // Get reference to the user's document in Firestore

                        val userRef =
                            userId?.let {
                                FirebaseFirestore.getInstance().collection("users").document(
                                    it
                                )
                            }

                        // Fetch the verification status from Firestore
                        userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                            val verificationStatus = documentSnapshot.getBoolean("verification")
                            val licenseNo = documentSnapshot.getString("licenseNumber")

                            Log.d("paymentInfo","no: $licenseNo")
                            Log.d("paymentInfo","no: $verificationStatus")

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

                            // Hide progress bar after data is fetched
                            progressBar.visibility = View.GONE
                            // Show the UI elements
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                        }?.addOnFailureListener { exception ->
                            // Handle failure to fetch verification status
                            // For example, show an error message or log the error
                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                            btnVerify.setOnClickListener {
                                // Retry fetching data
                                progressBar.visibility = View.VISIBLE
                                licenseStatus.visibility = View.GONE
                                btnVerify.visibility = View.GONE
                                licenseNumberField.visibility = View.GONE
                                fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)
                            }
                        }



                    }
                    "google.com" -> {
                        // User logged in using Google Sign-In
                        // Get reference to the user's document in Firestore
                        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
                        val googleAccountId = account?.id
                        val userRef = googleAccountId?.let {
                            FirebaseFirestore.getInstance().collection("users").document(
                                it
                            )
                        }

                        // Fetch the verification status from Firestore
                        userRef?.get()?.addOnSuccessListener { documentSnapshot ->
                            val verificationStatus = documentSnapshot.getBoolean("verification")
                            val licenseNo = documentSnapshot.getString("licenseNumber")

                            Log.d("paymentInfo","no: $licenseNo")
                            Log.d("paymentInfo","no: $verificationStatus")

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

                            // Hide progress bar after data is fetched
                            progressBar.visibility = View.GONE
                            // Show the UI elements
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                        }?.addOnFailureListener { exception ->
                            // Handle failure to fetch verification status
                            // For example, show an error message or log the error
                            progressBar.visibility = View.GONE
                            licenseStatus.visibility = View.VISIBLE
                            btnVerify.visibility = View.VISIBLE
                            licenseNumberField.visibility = View.VISIBLE
                            btnVerify.setOnClickListener {
                                // Retry fetching data
                                progressBar.visibility = View.VISIBLE
                                licenseStatus.visibility = View.GONE
                                btnVerify.visibility = View.GONE
                                licenseNumberField.visibility = View.GONE
                                fetchData(btnVerify, progressBar, licenseStatus, licenseNumberField, view)
                            }
                        }
                    }
                    // Add cases for other authentication providers if needed
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
        Log.d("paymentInfo","Google Id: ${googleSignInAccount?.id}")

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
                                // Create a reference to the user's document in Firestore

                                val userRef = firestore.collection("users").document(it)
                                Log.d("paymentInfo","Inside Else User: ${userRef.id}")
                                // Update the document with the license number and verification status
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
                            // User logged in using Google Sign-In
                            googleSignInAccount?.let { account ->
                                val googleAccountId = account.id
                                Log.d("paymentInfo", "Google Account ID: $googleAccountId")
                                // Create a reference to the user's document in FireStore using the Google Account ID
                                val userRef = googleAccountId?.let {
                                    firestore.collection("users").document(
                                        it
                                    )
                                }
                                //Log.d("paymentInfo", "Inside Else User: ${userRef.id}")
                                // Update the document with the license number and verification status
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
                //clearEditTextFields()
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