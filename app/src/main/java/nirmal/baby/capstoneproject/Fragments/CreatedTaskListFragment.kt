package nirmal.baby.capstoneproject.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class CreatedTaskListFragment : Fragment() {

    private lateinit var recyclerViewTaskList: RecyclerView
    private lateinit var progressBarCreateTaskList : ProgressBar
    private lateinit var txtViewEmptyInfo: TextView
    private var createdTaskListArrayList: ArrayList<TaskModel> = ArrayList()
    private val MAX_RETRIES = 3
    private var retryCount = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_created_task_list, container, false)

        recyclerViewTaskList = view.findViewById(R.id.recyclerViewTaskHistoryInProgress)
        progressBarCreateTaskList = view.findViewById(R.id.loadingProgressBarCreatedTaskList)
        txtViewEmptyInfo = view.findViewById(R.id.taskEmptyInfoTxtView)

        recyclerViewDataInitializing(recyclerViewTaskList)
        fetchTasks {
            // Update UI with the fetched tasks
            updateRecyclerView()
        }

        return view
    }

    private fun recyclerViewDataInitializing(
        createdTaskListRecyclerView: RecyclerView
    ) {
        val createdTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, createdTaskListArrayList, false, false)

        createdTaskListRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        createdTaskListRecyclerView.adapter = createdTaskAdapter
    }


    private fun fetchTasks(callback: () -> Unit) {
        progressBarCreateTaskList.visibility = View.VISIBLE
        recyclerViewTaskList.visibility = View.GONE
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the "tasks" collection
        val tasksCollection = firestore.collection("tasks")

        // Query to get all documents in the "tasks" collection
        tasksCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the existing list
                createdTaskListArrayList.clear()
                progressBarCreateTaskList.visibility = View.GONE
                recyclerViewTaskList.visibility = View.VISIBLE
                // Iterate through documents and convert them to TaskModel objects
                for (document in documents) {
                    val documentId = document.id
                    val task = document.toObject(TaskData::class.java)
                    val taskModel = TaskModel(documentId,task.title, task.priority,
                        task.createdBy, task.description, task.amount, task.tip, task.docs, task.status, task.acceptedBy, task.dateDue, task.latitude, task.longitude, task.ratings)

                    Log.d("FetchTasks", "Doc Id: ${documentId}")

                    // Add the task to the appropriate list based on priority
                    if (taskModel.getUserName() == FirebaseAuth.getInstance().currentUser?.displayName.toString()){
                        Log.d("FetchTasks", "If latest")
                        createdTaskListArrayList.add(taskModel)
                    }else{
                        Log.d("FetchTasks", "Else latest Check: Status: ${taskModel.getTaskStatus()}, Accepted: ${taskModel.getTaskAcceptedBy()}, Title : ${taskModel.getTaskTitle()}")
                    }


                }

                if (createdTaskListArrayList.size == 0){
                    txtViewEmptyInfo.visibility = View.VISIBLE
                } else {
                    txtViewEmptyInfo.visibility = View.GONE
                }
                // Log the size of the updated list
              //  Log.d("FetchTasks", "Updated task list size: ${inProgressTaskArrayList.size}")

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
        recyclerViewTaskList.adapter?.notifyDataSetChanged()

        Log.d("RecyclerViewUpdate", "RecyclerView updated")
    }

}