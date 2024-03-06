package nirmal.baby.capstoneproject.Fragments

import android.app.Activity
import android.app.Dialog
import android.content.Intent
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
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var taskItem: TaskModel? = null
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var imageUri: Uri
    private lateinit var popupImageView: ImageView
    private lateinit var imageChooseLauncher: ActivityResultLauncher<Intent>

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


        if (taskItem?.getTaskStatus() == "Accepted"){
            taskInProgressText.visibility = View.VISIBLE
            acceptButton.text = "Mark As Completed"
            acceptButton.visibility =View.VISIBLE
        } else {
            taskInProgressText.visibility = View.GONE
            acceptButton.visibility =View.VISIBLE
        }

        if (taskItem?.getUserName() == "sampleUser_Nirmal"){
            acceptButton.visibility = View.GONE
        }


        taskTitleDialog.text = this.taskItem?.getTaskTitle()
        taskDescDialog.text = this.taskItem?.getTaskDesc()
        taskAmtDialog.text = this.taskItem?.getTaskAmt()
        taskDocsDialog.text = this.taskItem?.getTaskDocs()

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
                showImageUploadPopup(view)
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

                    //will change this after implementing firebase auth
                    if (currentUserId == null){
                        currentUserId = getString(R.string.sample_userId)
                    }

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

    private fun showImageUploadPopup(view: View) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.pop_layout_upload_image)


        // Customize your popup view components (e.g., buttons, image views, etc.)
        val uploadButton = dialog.findViewById<Button>(R.id.btnUploadImage)
        val submitButton = dialog.findViewById<Button>(R.id.btnSubmitImage)
        val closePop = dialog.findViewById<TextView>(R.id.imageUploadPopupClose)
        popupImageView = dialog.findViewById(R.id.popupImageView)

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

}