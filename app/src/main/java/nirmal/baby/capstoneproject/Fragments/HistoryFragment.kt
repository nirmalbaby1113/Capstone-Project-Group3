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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class HistoryFragment : Fragment() {

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

        recyclerViewInProgress = view.findViewById(R.id.recyclerViewTaskHistoryInProgress)
        progressBarLayout = view.findViewById(R.id.progressBarLinearLayoutTaskHistory)

        recyclerViewDataInitializing(recyclerViewInProgress)
        fetchTasks {
            // Update UI with the fetched tasks
            updateRecyclerView()
        }


        return view
    }

    private fun recyclerViewDataInitializing(
        inProgressTaskRecyclerView: RecyclerView
    ) {
        val inProgressTaskAdapter = TaskAdapter(requireContext(), childFragmentManager, inProgressTaskArrayList, true, false)

        inProgressTaskRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        inProgressTaskRecyclerView.adapter = inProgressTaskAdapter
    }



    private fun fetchTasks(callback: () -> Unit) {
        progressBarLayout.visibility = View.VISIBLE
        recyclerViewInProgress.visibility = View.GONE
        val firestore = FirebaseFirestore.getInstance()
        val tasksCollection = firestore.collection("tasks")

        tasksCollection.get()
            .addOnSuccessListener { documents ->
                // Clear the existing list
                inProgressTaskArrayList.clear()
                progressBarLayout.visibility = View.GONE
                recyclerViewInProgress.visibility = View.VISIBLE

                for (document in documents) {
                    val documentId = document.id
                    val task = document.toObject(TaskData::class.java)
                    val taskModel = TaskModel(documentId,task.title, task.priority,
                        task.createdBy, task.description, task.amount, task.tip, task.docs, task.status, task.acceptedBy, task.dateDue, task.latitude, task.longitude, task.ratings)

                    // Add the task to the appropriate list based on priority
                    if ((taskModel.getTaskStatus() == "Accepted" || taskModel.getTaskStatus() == "Completed") && taskModel.getTaskAcceptedBy() == FirebaseAuth.getInstance().currentUser?.uid.toString()){
                        inProgressTaskArrayList.add(taskModel)
                    }
                }

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
                }
            }
    }

    private fun updateRecyclerView() {
        // Notify the adapter that the data set has changed
        recyclerViewInProgress.adapter?.notifyDataSetChanged()
        Log.d("RecyclerViewUpdate", "RecyclerView updated")
    }



}