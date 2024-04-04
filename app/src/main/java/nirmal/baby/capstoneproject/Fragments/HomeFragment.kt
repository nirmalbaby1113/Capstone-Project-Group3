package nirmal.baby.capstoneproject.Fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.MainActivity
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class HomeFragment : Fragment() {


    private var priorityTaskArrayList: ArrayList<TaskModel> = ArrayList()
    private var generalTaskArrayList: ArrayList<TaskModel> = ArrayList()
    private lateinit var priorityTaskRecyclerView: RecyclerView
    private lateinit var generalTaskRecyclerView: RecyclerView
    private lateinit var progressBarLinearLayout: LinearLayout
    private lateinit var baseLinearLayout: LinearLayout
    private lateinit var nameHeadingTextView: TextView
    private lateinit var filterImageView: ImageView
    private var filterText = "none"
    private val MAX_RETRIES = 3
    private var retryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        priorityTaskRecyclerView = view.findViewById(R.id.priorityTaskRecyclerView)
        generalTaskRecyclerView = view.findViewById(R.id.generalTaskListRecyclerView)
        progressBarLinearLayout = view.findViewById(R.id.progressBarLinearLayout)
        baseLinearLayout = view.findViewById(R.id.baseLinearLayout)
        nameHeadingTextView = view.findViewById(R.id.welcomeName)
        filterImageView = view.findViewById(R.id.imageFilter)

        filterImageView.setOnClickListener {
            showFilterPopup()
        }


        nameHeadingTextView.text = "Welcome ${getUserNameFromPrefs()},"


        recyclerViewDataInitializing(priorityTaskRecyclerView, view.findViewById(R.id.generalTaskListRecyclerView))
        fetchTasks {
            // Update UI with the fetched tasks
            updateRecyclerView()
        }

        return view
    }

    private fun recyclerViewDataInitializing(
        priorityTaskRecyclerView: RecyclerView,
        generalTaskRecyclerView: RecyclerView
    ) {
        val priorityTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, priorityTaskArrayList)
        val generalTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, generalTaskArrayList)

        priorityTaskRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        generalTaskRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        priorityTaskRecyclerView.adapter = priorityTaskAdapter
        generalTaskRecyclerView.adapter = generalTaskAdapter
    }

    private fun fetchTasks(callback: () -> Unit) {
        progressBarLinearLayout.visibility = View.VISIBLE
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the "tasks" collection
        val tasksCollection = firestore.collection("tasks")

        // Query to get all documents in the "tasks" collection
        tasksCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the existing list
                priorityTaskArrayList.clear()
                generalTaskArrayList.clear()

                progressBarLinearLayout.visibility = View.INVISIBLE
                baseLinearLayout.visibility = View.VISIBLE

                // Iterate through documents and convert them to TaskModel objects
                for (document in documents) {
                    val documentId = document.id
                    val task = document.toObject(TaskData::class.java)
                    val taskModel = TaskModel(documentId,task.title, task.priority,
                        task.createdBy, task.description, task.amount, task.tip, task.docs, task.status, task.acceptedBy, task.dateDue, task.latitude, task.longitude)

                    //Log.d("FetchTasks", "Doc Id: ${documentId}")

                    // Add the task to the appropriate list based on priority
                    if (taskModel.getTaskStatus() == "Not Accepted" && taskModel.getUserName() != FirebaseAuth.getInstance().currentUser?.displayName.toString()){
                        if (taskModel.getPriorityType() == "High") {
                            priorityTaskArrayList.add(taskModel)
                        } else {
                            Log.d("HomeFragment", "Else in High")
                            if (filterText == "none"){
                                Log.d("HomeFragment", "None")
                                generalTaskArrayList.add(taskModel)
                            }else {
                                Log.d("HomeFragment", "Star Low nd Medium")
                                if (filterText == "Low" && taskModel.getPriorityType() == "Low"){
                                    Log.d("HomeFragment", "Low")
                                    generalTaskArrayList.add(taskModel)
                                }else if (filterText == "Medium" && taskModel.getPriorityType() == "Medium"){
                                    Log.d("HomeFragment", "Medium")
                                    generalTaskArrayList.add(taskModel)
                                }else {
                                    Log.d("HomeFragment", "Medium else")
                                }
                            }

                        }
                    }


                }
                val generalTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, generalTaskArrayList)
                generalTaskRecyclerView.adapter = generalTaskAdapter
                // Log the size of the updated list
                Log.d("HomeFragment", "Updated task list size: ${priorityTaskArrayList.size}")
                Log.d("HomeFragment", "Updated task list size general: ${generalTaskArrayList.size}")
                // Invoke the callback to update the UI
                callback.invoke()
            }
            .addOnFailureListener {
                // Handle errors
                Log.e("FirebaseFetch", "Error fetching tasks:")
                // Retry logic
                if (retryCount < MAX_RETRIES) {
                    // Retry the operation after a delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        fetchTasks(callback)
                    }, 2000) // Retry after a 2-second delay (you can adjust the delay as needed)
                    retryCount++
                } else {
                    // Maximum retries reached, you might want to display an error message or take appropriate action
                    Log.e("FirebaseFetch", "Maximum retries reached.")
                }
            }
    }

    private fun showFilterPopup() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_filter, null)

        val widthInPixels = 1200
        val heightInPixels = 1100
        val popupWindow = PopupWindow(
            popupView,
            widthInPixels,
            heightInPixels,
            true
        )

        // Set up buttons and radio buttons
        val buttonOk = popupView.findViewById<Button>(R.id.buttonOk)
        val buttonCancel = popupView.findViewById<Button>(R.id.buttonCancel)
        val radioGroupPriority = popupView.findViewById<RadioGroup>(R.id.radioGroupPriorityPopUp)
        val radioButtonLowPriority = popupView.findViewById<RadioButton>(R.id.radioButtonLowPopUp)
        val radioButtonMediumPriority = popupView.findViewById<RadioButton>(R.id.radioButtonMediumPopUp)

        // Dismiss the pop-up window when the cancel button is clicked
        buttonCancel.setOnClickListener {
            popupWindow.dismiss()
        }

        // Handle filter action when the OK button is clicked
        buttonOk.setOnClickListener {
            // Get the selected radio button
            val selectedRadioButtonId = radioGroupPriority.checkedRadioButtonId

            if (selectedRadioButtonId != -1) {
                val radioButton = popupView.findViewById<RadioButton>(selectedRadioButtonId)

                // Get the text of the selected radio button
                //filterText = radioButton.text.toString()
                if (radioButton.id == R.id.radioButtonMediumPopUp){
                    Log.d("HomeFragment","Popup selected medium:")
                    filterText = "Medium"
                } else if (radioButton.id == R.id.radioButtonLowPopUp) {
                    Log.d("HomeFragment","Popup selected low:")
                    filterText = "Low"
                } else {
                    Log.d("HomeFragment","Popup selected all:")
                    filterText = "none"
                }


                Log.d("HomeFragment","Popup:$filterText")

                fetchTasks {
                    updateRecyclerView()
                }
                // Apply filter based on the selected priority

                // Dismiss the pop-up window
                popupWindow.dismiss()
            } else {
                // No radio button selected, show a message or take appropriate action
            }
        }

        // Show the pop-up window
        popupWindow.showAtLocation(requireActivity().window.decorView, Gravity.CENTER, 0, 0)
    }




    private fun updateRecyclerView() {
        // Notify the adapter that the data set has changed
        priorityTaskRecyclerView.adapter?.notifyDataSetChanged()

        Log.d("RecyclerViewUpdate", "RecyclerView updated")
    }

    private fun getUserNameFromPrefs(): String? {
        val sharedPreferences = context?.getSharedPreferences("UserNamePrefs", Context.MODE_PRIVATE)
        return sharedPreferences?.getString("username","")
    }
}