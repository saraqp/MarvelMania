package quesadoprado.saramaria.marvelmania.activities

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.ActivityRegisterBinding

class Register : AppCompatActivity() {
    lateinit var bindind:ActivityRegisterBinding
    lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindind=ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        auth=Firebase.auth
        inicializar()
    }
    fun inicializar(){
        val user=bindind.user.text.toString()
        val email=bindind.email.text.toString()
        val pass=bindind.password.text.toString()
        val repeatpass=bindind.repeatpassword.text.toString()
        val progressBar=ProgressDialog(this)
        bindind.btnRegistrar.setOnClickListener {
            if (bindind.user.text==null||bindind.email.text==null||bindind.password.text==null||bindind.repeatpassword.text==null){
                Toast.makeText(this,getString(R.string.camposRellenos),Toast.LENGTH_SHORT).show()
            }else{
                if(comprobarcontraseña(pass,repeatpass)){
                    progressBar.setMessage(getString(R.string.user_register))
                    progressBar.show()
                    auth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener(this){ task->
                            if(task.isSuccessful) {
                                val user: FirebaseUser = auth.currentUser!!
                                Toast.makeText(this, user.toString(), Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this, "NO FURULA", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }
    fun comprobarcontraseña(pass: String, repeatpass: String):Boolean{
        return pass == repeatpass
    }
}