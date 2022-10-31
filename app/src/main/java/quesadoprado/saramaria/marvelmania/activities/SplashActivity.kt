package quesadoprado.saramaria.marvelmania.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler

class SplashActivity : AppCompatActivity() {

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //hacer que el activity se muestre durante 1 segundo y despues no se pueda vuelver a éste
        handler=Handler()
        handler.postDelayed({
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },1000)

    }
}