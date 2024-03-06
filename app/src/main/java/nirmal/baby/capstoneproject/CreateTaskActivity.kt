package nirmal.baby.capstoneproject

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.TimePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.Fragments.CreateTaskFragment
import nirmal.baby.capstoneproject.Fragments.CreatedTaskListFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CreateTaskActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_task)

        val closeTextView = findViewById<TextView>(R.id.txtViewClose)
        val createTaskButton = findViewById<Button>(R.id.btnCreateTask)
        val viewTasksButton = findViewById<Button>(R.id.btnViewTasks)

        closeTextViewFunctionality(closeTextView)

        // Set initial fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerCreateTask, CreateTaskFragment())
            .commit()

        createTaskButton.setOnClickListener {
            updateButtonBackground(true,createTaskButton,viewTasksButton)
            // Replace with CreateTaskFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerCreateTask, CreateTaskFragment())
                .commit()
        }

        viewTasksButton.setOnClickListener {
            updateButtonBackground(false,createTaskButton,viewTasksButton)
            // Replace with TaskListFragment
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerCreateTask, CreatedTaskListFragment())
                .commit()
        }


    }

    private fun updateButtonBackground(isCreateTask: Boolean, btnCreate: Button, btnView: Button) {
        // Update the background of buttons based on the click
        if (isCreateTask) {
            btnCreate.setBackgroundResource(R.drawable.round_button_task_history_active)
            btnView.setBackgroundResource(R.drawable.round_button_task_history_inactive)
        } else {
            btnView.setBackgroundResource(R.drawable.round_button_task_history_active)
            btnCreate.setBackgroundResource(R.drawable.round_button_task_history_inactive)
        }
    }

    private fun closeTextViewFunctionality(closeTextView: TextView){
        closeTextView.setOnClickListener {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }



}