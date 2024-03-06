package nirmal.baby.capstoneproject

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivty : AppCompatActivity() {


    private lateinit var authFirebase: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_activty)

        val toSignUpTextView: TextView = findViewById(R.id.txtViewSignUp)
        val googleSignInImage: ImageView =findViewById(R.id.signInWithGoogleImage)

        authFirebase = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this,gso)

        googleSignInImage.setOnClickListener {
            signInGoogle()
        }


        toSignUpTextView.setOnClickListener {
            startActivity(Intent(applicationContext, SignUpActivity::class.java))
            finish()
        }
    }

    private fun signInGoogle(){
        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result ->
                if (result.resultCode == Activity.RESULT_OK){
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    handleResult(task)
                }
    }

    private fun handleResult(task: Task<GoogleSignInAccount>){
        if (task.isSuccessful){
            val account: GoogleSignInAccount? = task.result
            if (account != null){
                // Store user details in Firestore
                storeUserInFirestore(account)
            }
        }
    }

    private fun updateUI(account: GoogleSignInAccount){
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        authFirebase.signInWithCredential(credential).addOnCompleteListener{
            if(it.isSuccessful){
                val intent: Intent = Intent(this, MainActivity::class.java)
                intent.putExtra("email",account.email)
                intent.putExtra("name",account.displayName)
                startActivity(intent)
            }
        }
    }

    private fun storeUserInFirestore(account: GoogleSignInAccount) {
        val db = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = account.id ?: firebaseUser?.uid ?: ""
        val userDocRef = db.collection("users").document(uid)

        userDocRef.get().addOnCompleteListener { documentSnapshot ->
            if (documentSnapshot.isSuccessful) {
                if (!documentSnapshot.result.exists()) {
                    // Document doesn't exist, add user details
                    val userMap = hashMapOf(
                        "displayName" to account.displayName,
                        "email" to account.email,
                        // Add other fields as needed
                    )

                    userDocRef.set(userMap)
                        .addOnSuccessListener {
                            // User details added successfully
                            updateUI(account)
                        }
                        .addOnFailureListener { e ->
                            // Handle failure to add user details
                        }
                } else {
                    // Document already exists (user has signed in before)
                    updateUI(account)
                }
            } else {
                // Handle failure to check if the document exists
            }
        }
    }


}