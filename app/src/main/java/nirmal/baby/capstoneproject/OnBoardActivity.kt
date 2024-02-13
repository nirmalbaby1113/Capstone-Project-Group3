package nirmal.baby.capstoneproject

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import nirmal.baby.capstoneproject.AdapterClass.OnBoardingItemsAdapter
import nirmal.baby.capstoneproject.Data.OnBoardingItem
import java.text.FieldPosition

class OnBoardActivity : AppCompatActivity() {

    private lateinit var onBoardingItemsAdapter: OnBoardingItemsAdapter
    private lateinit var indicatorsContainer: LinearLayout
    private lateinit var buttonStarted: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_board)

        buttonStarted = findViewById(R.id.btnGetStarted)

        val sharedPreferences = getSharedPreferences("OnBoardCheckPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("isOnboardingCompleted", true)
        editor.apply()

        setOnBoardingItems()
        setUpIndicator()
        setUpCurrentIndicator(0)
    }


    private fun setOnBoardingItems(){
        onBoardingItemsAdapter = OnBoardingItemsAdapter(
            listOf(
                OnBoardingItem(
                    onBoardingImage = R.drawable.welcome,
                    title = "Welcome to PocketBuddy",
                    description = "Join TaskHub, post tasks, earn cash. Experience a new way to get things done!"
                ),
                OnBoardingItem(
                    onBoardingImage = R.drawable.post_task,
                    title = "Post Your Task",
                    description = "Post tasks on PocketBuddy with ease. Share task details, completion proof, and your payment. Get things done effortlessly!"
                ),
                OnBoardingItem(
                    onBoardingImage = R.drawable.accept_task,
                    title = "Discover and Accept Tasks",
                    description = "Explore, choose, and complete tasks on PocketBuddy. Your quick opportunity marketplace!"
                ),
                OnBoardingItem(
                    onBoardingImage = R.drawable.complete,
                    title = "Start Your Earnings",
                    description = "Step into a collaborative era with PocketBuddy. Click 'Get Started' or 'Next' to join – more than an app, it's your ultimate platform for getting things done!"
                )
            )
        )

        val onBoardingViewPager = findViewById<ViewPager2>(R.id.onBoardingViewPager)
        onBoardingViewPager.adapter = onBoardingItemsAdapter
        onBoardingViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setUpCurrentIndicator(position)
            }
        })
        (onBoardingViewPager.getChildAt(0) as RecyclerView).overScrollMode =
            RecyclerView.OVER_SCROLL_NEVER
        findViewById<ImageView>(R.id.imageNext).setOnClickListener {
            if (onBoardingViewPager.currentItem + 1 < onBoardingItemsAdapter.itemCount){
                onBoardingViewPager.currentItem += 1
            }else{
                navigateToMainActivity()
            }
        }

        findViewById<TextView>(R.id.textSkip).setOnClickListener {
            navigateToMainActivity()
        }

        findViewById<TextView>(R.id.btnGetStarted).setOnClickListener {
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity(){
        startActivity(Intent(applicationContext, MainActivity::class.java))
        finish()
    }
    private fun setUpIndicator(){
        indicatorsContainer = findViewById(R.id.indicatorsContainer)
        val indicators = arrayOfNulls<ImageView>(onBoardingItemsAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT,
            WRAP_CONTENT)
        layoutParams.setMargins(8,0,8,0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i]?.let {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_background
                    )
                )
                it.layoutParams= layoutParams
                indicatorsContainer.addView(it)
            }
        }
    }

    private fun setUpCurrentIndicator(position: Int){
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount){
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if (i == position){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active_background
                    )
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive_background
                    )
                )
            }
        }
    }
}