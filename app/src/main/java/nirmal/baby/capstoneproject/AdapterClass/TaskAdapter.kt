package nirmal.baby.capstoneproject.AdapterClass

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import nirmal.baby.capstoneproject.Fragments.BottomSheetDialogFragment
import nirmal.baby.capstoneproject.ModelClass.TaskModel
import nirmal.baby.capstoneproject.R

class TaskAdapter(private val context: Context, private val fragmentManager: FragmentManager, private val taskListData: List<TaskModel>) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(R.layout.task_card, parent, false)
        return ViewHolder(listItem)
    }

    override fun getItemCount(): Int {
        return taskListData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val taskItem = taskListData[position]
        holder.priorityTextView.text = taskItem.getPriorityType()
        holder.taskTitleNameTextView.text = taskItem.getTaskTitle()
        holder.userNameTextView.text = taskItem.getUserName()

        if (taskItem.getTaskTip() == "0"){
            holder.cardEarnings.text = "Earn : $${taskItem.getTaskAmt()}"
        } else {
            holder.cardEarnings.text = "Earn : $${taskItem.getTaskTip()}"
        }

        // Set background and text color based on priority type
        when (taskItem.getPriorityType()) {
            "Low" -> {
                holder.priorityTextView.setBackgroundResource(R.drawable.round_button_green)
                holder.priorityTextView.text = "Low"
            }
            "Medium" -> {
                holder.priorityTextView.setBackgroundResource(R.drawable.round_button_orange)
                holder.priorityTextView.text = "Medium"
            }
            "High" -> {
                holder.priorityTextView.setBackgroundResource(R.drawable.round_button_red)
                holder.priorityTextView.text = "High"
            }
            // Add more cases if needed
            else -> {
                // Default case
            }
        }

        holder.moreInfoTextView.setOnClickListener {
            handleMoreInfoButtonCall(taskItem)
        }

    }

    class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val priorityTextView : TextView = itemView.findViewById(R.id.cardPriority)
        val taskTitleNameTextView : TextView = itemView.findViewById(R.id.cardTaskTitle)
        val userNameTextView : TextView = itemView.findViewById(R.id.cardUserName)
        val moreInfoTextView : TextView = itemView.findViewById(R.id.cardMoreInfo)
        val cardEarnings: TextView = itemView.findViewById(R.id.cardEarnings)
    }

    private fun handleMoreInfoButtonCall(taskItem: TaskModel){
        val bottomSheetFragment = BottomSheetDialogFragment()
        bottomSheetFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomSheetDialogTheme)
        bottomSheetFragment.setTaskItem(taskItem)
        bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
    }



}