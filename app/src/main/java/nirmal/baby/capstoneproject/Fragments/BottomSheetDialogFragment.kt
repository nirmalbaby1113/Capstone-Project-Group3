package nirmal.baby.capstoneproject.Fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class BottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var taskItem: TaskModel? = null

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
                        "accepted by" to currentUserId
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


        return view
    }


}