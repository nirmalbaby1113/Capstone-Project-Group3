package nirmal.baby.capstoneproject.Fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class HomeFragment : Fragment() {

    private var priorityTaskArrayList: ArrayList<TaskModel> = ArrayList()
    private var generalTaskArrayList: ArrayList<TaskModel> = ArrayList()
    private lateinit var priorityTaskRecyclerView: RecyclerView
    private lateinit var progressBarLinearLayout: LinearLayout
    private lateinit var baseLinearLayout: LinearLayout
    private val MAX_RETRIES = 3
    private var retryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        priorityTaskRecyclerView = view.findViewById(R.id.priorityTaskRecyclerView)
        progressBarLinearLayout = view.findViewById(R.id.progressBarLinearLayout)
        baseLinearLayout = view.findViewById(R.id.baseLinearLayout)

        progressBarLinearLayout.visibility = View.VISIBLE

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
                        task.createdBy, task.description, task.amount, task.tip, task.docs, task.status, task.acceptedBy)

                    Log.d("FetchTasks", "Doc Id: ${documentId}")

                    // Add the task to the appropriate list based on priority
                    if (taskModel.getTaskStatus() == "Not Accepted"){
                        if (taskModel.getPriorityType() == "High") {
                            priorityTaskArrayList.add(taskModel)
                        } else {
                            generalTaskArrayList.add(taskModel)
                        }
                    }


                }
                // Log the size of the updated list
                Log.d("FetchTasks", "Updated task list size: ${priorityTaskArrayList.size}")

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
        priorityTaskRecyclerView.adapter?.notifyDataSetChanged()

        Log.d("RecyclerViewUpdate", "RecyclerView updated")
    }
}