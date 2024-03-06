package nirmal.baby.capstoneproject

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val toLoginButton: Button = findViewById(R.id.btnLoginBack)

        toLoginButton.setOnClickListener {
            startActivity(Intent(applicationContext, LoginActivty::class.java))
            finish()
        }
    }
}