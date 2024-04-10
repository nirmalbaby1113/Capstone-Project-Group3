package nirmal.baby.capstoneproject

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.health.connect.datatypes.units.Length
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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


    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)

        getLastLocation()

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
                Log.d("MainActivity","Check1")
                //storeUserInFirestore()
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

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    location?.let {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        // Store latitude and longitude in SharedPreferences
                        saveLocation(latitude, longitude)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        } else {
            requestPermissions()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun saveLocation(latitude: Double, longitude: Double) {
        //Toast.makeText(this,"Lat $latitude",Toast.LENGTH_SHORT).show()
        Log.d("MainActivity","L: $latitude")
        Log.d("MainActivity","LO: $longitude")

        val editor = sharedPreferences.edit()
        editor.putFloat("latitude", latitude.toFloat())
        editor.putFloat("longitude", longitude.toFloat())
        editor.apply()
    }

    private fun storeUserInFirestore() {
        val db = FirebaseFirestore.getInstance()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val uid = firebaseUser?.uid
        val userDocRef = uid?.let { db.collection("users").document(it) }
        sharedPreferences = getSharedPreferences("LocationPrefs", Context.MODE_PRIVATE)
        Log.d("MainActivity","Check2")
        userDocRef?.get()?.addOnCompleteListener { documentSnapshot ->
            Log.d("MainActivity","Check3")
            if (documentSnapshot.isSuccessful) {
                Log.d("MainActivity","Check4")
                if (!documentSnapshot.result.exists()) {
                    Log.d("MainActivity","Check5")
                    // Document doesn't exist, add user details
                    val userMap = hashMapOf(
                        "latitude" to sharedPreferences.getFloat("latitude", 0.0f),
                        "longitude" to sharedPreferences.getFloat("longitude", 0.0f)
                    )

                    userDocRef.set(userMap)
                        .addOnSuccessListener {

                        }
                        .addOnFailureListener { e ->
                        }

                } else {

                }
            } else {

            }
        }
    }


    companion object {
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }


}