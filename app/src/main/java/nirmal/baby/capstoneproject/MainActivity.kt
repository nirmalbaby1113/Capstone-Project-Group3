package nirmal.baby.capstoneproject

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import nirmal.baby.capstoneproject.AdapterClass.TaskAdapter
import nirmal.baby.capstoneproject.Data.TaskData
import nirmal.baby.capstoneproject.Fragments.HistoryFragment
import nirmal.baby.capstoneproject.Fragments.HomeFragment
import nirmal.baby.capstoneproject.Fragments.ProfileFragment
import nirmal.baby.capstoneproject.ModelClass.TaskModel

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isOnboardingCompleted()) {
            // Onboarding not completed, launch OnboardingActivity
            val intent = Intent(this, OnBoardActivity::class.java)
            startActivity(intent)
            finish() // Close MainActivity to prevent going back
        } else {

            // Get the current user
            val currentUser = FirebaseAuth.getInstance().currentUser

            // Check if the user is signed in
            if (currentUser != null) {
                // User is signed in
                // You can access the user's information using currentUser
                val uid = currentUser.uid
                val email = currentUser.email

                // Perform actions for a signed-in user
                // Onboarding and LogIn completed, launch the main activity with HomeFragment
                setContentView(R.layout.activity_main)

            } else {
                // User is not signed in
                // Redirect the user to the sign-in screen or perform other actions
                startActivity(Intent(applicationContext,LoginActivty::class.java))
            }
            var userName = intent.getStringExtra("name")
            if (userName != null){
                addUserNameToSharedPref(userName)
            }

            // Check if savedInstanceState is null to avoid fragment duplication on configuration changes
            if (savedInstanceState == null){

                // Load the default fragment (you can change this to the fragment you want to show initially)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }

            // Set up bottom navigation
            val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

            bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_home -> {

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HomeFragment())
                            .commit()
                        true
                    }
                    R.id.menu_history -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, HistoryFragment())
                            .commit()
                        true
                    }
                    R.id.menu_create_task -> {
                            // If it's not a fragment, start the CreateTaskActivity
                        startActivity(Intent(applicationContext, CreateTaskActivity::class.java))
                        menuItem.isChecked = false
                        true
                    }
                    R.id.menu_profile -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, ProfileFragment())
                            .commit()
                        true
                    }
                    // Add more cases for other menu items if needed
                    else -> false
                }
            }
        }
    }

    private fun isOnboardingCompleted(): Boolean {
        // Check shared preferences or any other storage for onboarding completion status
        // Return true if completed, false otherwise
        // Example using shared preferences:
        val sharedPreferences = getSharedPreferences("OnBoardCheckPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getBoolean("isOnboardingCompleted", false)
    }

    private fun addUserNameToSharedPref(name: String){
        val sharedPreferences = getSharedPreferences("UserNamePrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("username", name)
        editor.apply()
    }


}