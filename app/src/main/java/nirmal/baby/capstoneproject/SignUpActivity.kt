package nirmal.baby.capstoneproject

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class SignUpActivity : AppCompatActivity() {

    private lateinit var editTextFullName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var editTextConfirmPassword: EditText
    private lateinit var toLoginButton: Button
    private lateinit var signUpButton: Button
    private lateinit var requiredTextView: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var formLayout: RelativeLayout
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        toLoginButton = findViewById(R.id.btnLoginBack)
        signUpButton = findViewById(R.id.btnSignUp)
        editTextFullName = findViewById(R.id.createAccountName)
        editTextEmail = findViewById(R.id.createAccountEmail)
        editTextPassword = findViewById(R.id.createAccountPassword)
        editTextConfirmPassword = findViewById(R.id.createAccountPasswordConfirm)
        requiredTextView = findViewById(R.id.txtViewWarning)
        firebaseAuth = FirebaseAuth.getInstance()
        formLayout = findViewById(R.id.signUpFormLayout)
        progressBar = findViewById(R.id.signUpProgressbar)


        signUpButton.setOnClickListener {
            if (editTextFullName.text.isBlank() || editTextFullName.text.isEmpty()) {
                requiredTextView.text = "Full name is required"
                requiredTextView.visibility = View.VISIBLE
            }else if (editTextEmail.text.isBlank() || editTextEmail.text.isEmpty()) {
                requiredTextView.text = "Email is required"
                requiredTextView.visibility = View.VISIBLE
            } else if (editTextPassword.text.isBlank() || editTextPassword.text.isEmpty()) {
                requiredTextView.text = "Password is required"
                requiredTextView.visibility = View.VISIBLE
            } else if (editTextConfirmPassword.text.isBlank() || editTextConfirmPassword.text.isEmpty()) {
                requiredTextView.text = "Please confirm your password"
                requiredTextView.visibility = View.VISIBLE
            } else if (editTextConfirmPassword.text.toString() != editTextPassword.text.toString()) {
                requiredTextView.text = "Password mismatch"
                requiredTextView.visibility = View.VISIBLE
            }else {
                progressBar.visibility = View.VISIBLE
                formLayout.visibility = View.GONE
                signUp(editTextEmail.text.toString(),editTextConfirmPassword.text.toString(), editTextFullName.text.toString())
            }
        }

        toLoginButton.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivty::class.java))
            finish()
        }
    }

    private fun signUp(email: String, password: String, displayName: String){
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    // Sign-up successful, update UI or navigate to the next screen
                    val user: FirebaseUser? = firebaseAuth.currentUser

                    // Set the display name for the user
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                // Display name updated successfully
                                showStatusPopup(success = true)
                            } else {
                                // Failed to update display name
                                showStatusPopup(success = false)
                            }
                        }
                    // You can add additional logic here based on your app's requirements
                } else {
                    // If sign-up fails, display a message to the user.
                    // You can also handle specific errors using task.exception
                    // For example: task.exception?.message
                    // showToast("Sign up failed: ${task.exception?.message}")
                    showStatusPopup(success = false)
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
                okButton.visibility = View.VISIBLE
                txtView.text = "Registration successful"
                okButton.text = "Login"
                okButton.setOnClickListener {
                    startActivity(Intent(this,LoginActivty::class.java))
                    dialog.dismiss()
                    //clearEditTextFields()
                }
            } else {
                // Error in publishing task, show "Retry" button
                okButton.visibility = View.GONE
                retryButton.visibility = View.VISIBLE
                txtView.text = "Registration failed"
                retryButton.setOnClickListener {
                    progressBar.visibility = View.GONE
                    formLayout.visibility = View.VISIBLE
                    dialog.dismiss()
                }
            }

        dialog.show()
    }

}