package nirmal.baby.capstoneproject.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class HistoryFragment : Fragment() {

    private lateinit var btnInProgress: Button
    private lateinit var btnCompleted: Button
    private lateinit var recyclerViewInProgress: RecyclerView
    private lateinit var progressBarLayout: LinearLayout
    private var inProgressTaskArrayList: ArrayList<TaskModel> = ArrayList()
    private val MAX_RETRIES = 3
    private var retryCount = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        btnInProgress = view.findViewById(R.id.btnInProgress)
        btnCompleted = view.findViewById(R.id.btnCompleted)
        recyclerViewInProgress = view.findViewById(R.id.recyclerViewTaskHistoryInProgress)
        progressBarLayout = view.findViewById(R.id.progressBarLinearLayoutTaskHistory)

        recyclerViewDataInitializing(recyclerViewInProgress)
        fetchTasks {
            // Update UI with the fetched tasks
            updateRecyclerView()
        }

        btnInProgress.setOnClickListener {
            // Handle the click for the "In Progress" button
            updateButtonBackground(true)
        }

        /*   btnCompleted.setOnClickListener {
               // Handle the click for the "Completed" button
               updateButtonBackground(false)
           }*/

        return view
    }

    private fun recyclerViewDataInitializing(
        inProgressTaskRecyclerView: RecyclerView
    ) {
        val inProgressTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, inProgressTaskArrayList)

        inProgressTaskRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        inProgressTaskRecyclerView.adapter = inProgressTaskAdapter
    }

    private fun updateButtonBackground(isInProgress: Boolean) {
        // Update the background of buttons based on the click
        if (isInProgress) {
            btnInProgress.setBackgroundResource(R.drawable.round_button_task_history_active)
            btnCompleted.setBackgroundResource(R.drawable.round_button_task_history_inactive)
        } else {
            btnCompleted.setBackgroundResource(R.drawable.round_button_task_history_active)
            btnInProgress.setBackgroundResource(R.drawable.round_button_task_history_inactive)
        }
    }

    private fun fetchTasks(callback: () -> Unit) {
        progressBarLayout.visibility = View.VISIBLE
        recyclerViewInProgress.visibility = View.GONE
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the "tasks" collection
        val tasksCollection = firestore.collection("tasks")

        // Query to get all documents in the "tasks" collection
        tasksCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the existing list
                inProgressTaskArrayList.clear()
                progressBarLayout.visibility = View.GONE
                recyclerViewInProgress.visibility = View.VISIBLE
                // Iterate through documents and convert them to TaskModel objects
                for (document in documents) {
                    val documentId = document.id
                    val task = document.toObject(TaskData::class.java)
                    val taskModel = TaskModel(documentId,task.title, task.priority,
                        task.createdBy, task.description, task.amount, task.tip, task.docs, task.status, task.acceptedBy)

                    Log.d("FetchTasks", "Doc Id: ${documentId}")

                    // Add the task to the appropriate list based on priority
                    if (taskModel.getTaskStatus() == "Accepted" && taskModel.getTaskAcceptedBy() == "sampleUser_Nirmal"){
                        Log.d("FetchTasks", "If latest")
                        inProgressTaskArrayList.add(taskModel)
                    }else{
                        Log.d("FetchTasks", "Else latest Check: Status: ${taskModel.getTaskStatus()}, Accepted: ${taskModel.getTaskAcceptedBy()}, Title : ${taskModel.getTaskTitle()}")
                    }


                }
                // Log the size of the updated list
                Log.d("FetchTasks", "Updated task list size: ${inProgressTaskArrayList.size}")

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

    private fun updateRecyclerView() {
        // Notify the adapter that the data set has changed
        recyclerViewInProgress.adapter?.notifyDataSetChanged()

        Log.d("RecyclerViewUpdate", "RecyclerView updated")
    }



}