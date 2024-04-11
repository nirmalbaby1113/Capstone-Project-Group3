package nirmal.baby.capstoneproject.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.media.Rating
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.MainActivity
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R
import org.w3c.dom.Text

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var taskItem: TaskModel? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private lateinit var popupImageView: ImageView
    private lateinit var okButton: Button
    private lateinit var uploadButton: Button
    private lateinit var submitButton: Button
    private lateinit var closePop: TextView
    private lateinit var completedTextView: TextView
    private lateinit var infoTextViewProof: TextView
    private lateinit var feedbackTextView: TextView
    private lateinit var ratingsStars: RatingBar
    private lateinit var imageChooseLauncher: ActivityResultLauncher<Intent>
    private val firestore = FirebaseFirestore.getInstance()

    fun setTaskItem(taskItem: TaskModel) {
        this.taskItem = taskItem
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("FragmentBottom", "onCreateView executing ${this.taskItem}")

        val view = inflater.inflate(R.layout.bottom_sheet_fragment, container, false)

        val closeButton = view.findViewById<Button>(R.id.btnCloseBottomSheet)
        val acceptButton = view.findViewById<Button>(R.id.btnAcceptBottomSheet)
        val taskTitleDialog = view.findViewById<TextView>(R.id.bottomSheetTaskTitle)
        val taskDescDialog = view.findViewById<TextView>(R.id.bottomSheetTaskDesc)
        val taskAmtDialog = view.findViewById<TextView>(R.id.bottomSheetTaskAmt)
        val taskTipDialog = view.findViewById<TextView>(R.id.bottomSheetTaskAmtEarned)
        val taskDocsDialog = view.findViewById<TextView>(R.id.bottomSheetTaskDocs)
        val taskInProgressText = view.findViewById<TextView>(R.id.bottomSheetInProgressText)
        val taskDue = view.findViewById<TextView>(R.id.bottomSheetTaskDue)


        if (taskItem?.getTaskStatus() == "Accepted"){
            taskInProgressText.visibility = View.VISIBLE
            acceptButton.text = "Mark As Completed"
            acceptButton.visibility =View.VISIBLE
        } else {
            taskInProgressText.visibility = View.GONE
            acceptButton.visibility =View.VISIBLE
        }

        if (taskItem?.getUserName() == FirebaseAuth.getInstance().currentUser?.displayName.toString()){
            acceptButton.visibility = View.GONE
        }


        taskTitleDialog.text = this.taskItem?.getTaskTitle()
        taskDescDialog.text = this.taskItem?.getTaskDesc()
        taskAmtDialog.text = this.taskItem?.getTaskAmt()
        taskDocsDialog.text = this.taskItem?.getTaskDocs()
        taskDue.text = this.taskItem?.getTaskDue()

        if (this.taskItem?.getTaskTip() == "0") {
            taskTipDialog.text = this.taskItem?.getTaskAmt()
        } else {
            taskTipDialog.text = this.taskItem?.getTaskTip()
        }

 

        closeButton.setOnClickListener {
            dismiss()
        }

        // Initialize the ActivityResultLauncher
        imageChooseLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.data?.let {
                    imageUri = it
                    popupImageView.setImageURI(imageUri)
                }
            }
        }

        if (taskItem?.getTaskStatus() == "Accepted"){
            acceptButton.setOnClickListener {

                showImageUploadPopup(view, taskItem!!.getDocumentId())
            }
        } else {
            acceptButton.setOnClickListener {

                Log.d("Firestore", "Before updating")

                taskItem?.let {
                    Log.d("Firestore", "Start of let")
                    val firestore = FirebaseFirestore.getInstance()
                    val documentId = taskItem?.getDocumentId() // Replace with the actual method to get the document ID

                    // Get the current user ID from Firebase Authentication
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    var currentUserId = currentUser?.uid

                    if (documentId != null && currentUserId != null) {
                        // Update the specific fields in Firestore
                        val updates = hashMapOf<String, Any>(
                            "status" to "Accepted",
                            "acceptedBy" to currentUserId
                        )

                        firestore.collection("tasks")
                            .document(documentId)
                            .update(updates)
                            .addOnSuccessListener {
                                // Update successful
                                Log.d("Firestore", "DocumentSnapshot successfully updated!")
                                dismiss()
                            }
                            .addOnFailureListener { e ->
                                // Handle the error
                                Log.w("Firestore", "Error updating document", e)
                            }
                    }
                }
            }
        }




        return view
    }

    private fun showImageUploadPopup(view: View, documentId: String) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.pop_layout_upload_image)


        // Customize your popup view components (e.g., buttons, image views, etc.)
        uploadButton = dialog.findViewById<Button>(R.id.btnUploadImage)
        submitButton = dialog.findViewById<Button>(R.id.btnSubmitImage)
        closePop = dialog.findViewById<TextView>(R.id.imageUploadPopupClose)
        popupImageView = dialog.findViewById(R.id.popupImageView)
        okButton = dialog.findViewById(R.id.btnOK)
        completedTextView = dialog.findViewById(R.id.txtViewConformation)
        infoTextViewProof = dialog.findViewById(R.id.txtViewProof)
        ratingsStars = dialog.findViewById(R.id.ratingBar)
        feedbackTextView = dialog.findViewById(R.id.txtViewFeedback)

        // Set click listener for the upload button (you need to implement image upload logic here)
        uploadButton.setOnClickListener {
            // Implement your image upload logic here
            // You can use libraries like Firebase Storage to upload images
            // Example: Upload image to Firebase Storage
            // val storageRef = FirebaseStorage.getInstance().reference
            // val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
            // val uploadTask = imageRef.putFile(/* your image URI */)
            openImageChooser()

        }

        // Set click listener for the submit button (you need to implement submission logic here)
        submitButton.setOnClickListener {
            // Implement your submission logic here
            // You can use Firestore to save the image information along with other details
            // Example: Save image information to Firestore
            // val firestore = FirebaseFirestore.getInstance()
            // val documentId = taskItem?.getDocumentId()
            // firestore.collection("tasks").document(documentId)
            //     .update("imageUrl", /* your image URL */)

            addFeedback(documentId, dialog)

        }

        closePop.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openImageChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }


        // Launch the image chooser using the launcher
        imageChooseLauncher.launch(Intent.createChooser(intent, "Select Picture"))
    }

    private fun addFeedback(documentId: String, dialog: Dialog){
        Log.d("BottomSheet","Doc ID: $documentId")


        val userRef = firestore.collection("tasks").document(documentId)

        Log.d("BottomSheet","Doc UserRef: $userRef")

        userRef.update(
            mapOf(
                "ratings" to ratingsStars.rating.toString(),
                "status" to "Completed"
            )
        ).addOnSuccessListener {
            uploadButton.visibility = View.GONE
            submitButton.visibility = View.GONE
            popupImageView.visibility = View.GONE
            infoTextViewProof.visibility = View.GONE
            ratingsStars.visibility = View.GONE
            feedbackTextView.visibility = View.GONE

            okButton.visibility = View.VISIBLE
            completedTextView.visibility = View.VISIBLE

            okButton.setOnClickListener {
                val intent: Intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            }

        }.addOnFailureListener {

            uploadButton.visibility = View.GONE
            submitButton.visibility = View.GONE
            popupImageView.visibility = View.GONE
            infoTextViewProof.visibility = View.GONE
            ratingsStars.visibility = View.GONE
            feedbackTextView.visibility = View.GONE


            okButton.text = "Close"
            completedTextView.text = "Please Try Again..."
            okButton.visibility = View.VISIBLE
            completedTextView.visibility = View.VISIBLE

            okButton.setOnClickListener {
             dialog.dismiss()
            }
        }

    }

    private fun elementsModifyAfterSubmit(){

    }

}