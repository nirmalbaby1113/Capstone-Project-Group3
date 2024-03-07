package nirmal.baby.capstoneproject.Fragments

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import nirmal.baby.capstoneproject.LoginActivty
import nirmal.baby.capstoneproject.R

class ProfileFragment: Fragment() {

    private lateinit var basicLayoutProfilePage: RelativeLayout
    private lateinit var progressBarProfilePage: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)


        val linearLayoutPayment: LinearLayout = view.findViewById(R.id.paymentSelectionLayout)
        val linearLayoutWallet: LinearLayout = view.findViewById(R.id.walletSelectionLayout)
        val signOutTextView: TextView = view.findViewById(R.id.txtViewSignOut)
        val textViewEmail: TextView = view.findViewById(R.id.profileEmail)
        val textViewDisplayName: TextView = view.findViewById(R.id.profileName)
        basicLayoutProfilePage = view.findViewById(R.id.profilePageBasicLayout)
        progressBarProfilePage = view.findViewById(R.id.progressBarProfilePage)



        val firebaseAuth = FirebaseAuth.getInstance()

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerCreateTask, PaymentInfoFragment())
            .commit()

        basicLayoutProfilePage.visibility = View.GONE
        progressBarProfilePage.visibility = View.VISIBLE

        showProfileBasicDetails(textViewEmail,textViewDisplayName)

        linearLayoutPayment.setOnClickListener {
            updateImageBackground(true,linearLayoutPayment,linearLayoutWallet)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerCreateTask, PaymentInfoFragment())
                .commit()
        }
        
        linearLayoutWallet.setOnClickListener {
            updateImageBackground(false,linearLayoutPayment,linearLayoutWallet)
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerCreateTask, WalletInfoFragment())
                .commit()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .setHostedDomain("*")
            .build()
        val googleSignInClient: GoogleSignInClient = GoogleSignIn.getClient(requireContext(),gso)
        signOutTextView.setOnClickListener {
            signOut(firebaseAuth, googleSignInClient)
        }


        return view
    }

    private fun showProfileBasicDetails(textViewEmail: TextView, textViewDisplayName: TextView) {
        // Get current user from Firebase
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Check if the user is not null
        currentUser?.let {
            // Retrieve email and display name
            val email = it.email
            val displayName = it.displayName

            // Set the values in TextViews
            textViewEmail.text = email
            textViewDisplayName.text = displayName

            basicLayoutProfilePage.visibility = View.VISIBLE
            progressBarProfilePage.visibility = View.GONE
        }
    }

    private fun updateImageBackground(isPaymentImage: Boolean, layoutPayment: LinearLayout, layoutWallet: LinearLayout) {
        // Update the background of Images based on the click
        if (isPaymentImage) {
            layoutPayment.setBackgroundResource(R.drawable.imageview_rounded_white_active)
            layoutWallet.setBackgroundResource(R.drawable.imageview_rounded_white)
        } else {
            layoutWallet.setBackgroundResource(R.drawable.imageview_rounded_white_active)
            layoutPayment.setBackgroundResource(R.drawable.imageview_rounded_white)
        }
    }

    private fun signOut(auth: FirebaseAuth, googleSignInClient: GoogleSignInClient){
        auth.signOut()
        googleSignInClient.signOut()
        startActivity(Intent(requireContext(),LoginActivty::class.java))
        requireActivity().finish()
    }

}