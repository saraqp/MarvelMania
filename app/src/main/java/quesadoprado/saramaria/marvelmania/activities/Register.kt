package quesadoprado.saramaria.marvelmania.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.ActivityRegisterBinding
import quesadoprado.saramaria.marvelmania.utils.Extensions.toast
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class Register : AppCompatActivity() {
    private lateinit var userEmail:String
    private lateinit var userPassword:String
    private var firebaseUserID:String=""
    lateinit var bindind:ActivityRegisterBinding

    private lateinit var createAccountInputsArray:Array<EditText>

    private lateinit var auth:FirebaseAuth

    private val database= firebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        auth= firebaseAuth
        super.onCreate(savedInstanceState)
        bindind=ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        createAccountInputsArray= arrayOf(bindind.user,bindind.email,bindind.password,bindind.repeatpassword)

        bindind.btnRegistrar.setOnClickListener {
            signIn()
        }
    }

    private fun notEmpty():Boolean=bindind.user.text.trim().toString().isNotEmpty()
            && bindind.email.text.trim().toString().isNotEmpty()
            && bindind.password.text.trim().toString().isNotEmpty()
            && bindind.repeatpassword.text.trim().toString().isNotEmpty()


    fun signIn(){
        if (comprobarcontraseña()){
            val username=bindind.user.text.toString()
            userEmail=bindind.email.text.toString().trim()
            userPassword=bindind.password.text.toString().trim()

            auth.createUserWithEmailAndPassword(userEmail,userPassword)
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        firebaseUserID=auth.currentUser!!.uid

                        //Guardar en la base de datos
                        database.collection("users").document(userEmail).set(
                            hashMapOf("uid" to firebaseUserID,
                            "displayName" to username,
                            "password" to userPassword,
                            "status" to "online")
                        )
                        //Enviamos el mensaje de verificacion de email y nos dirigimos al main
                        // activity al fragment biblioteca que es el principal
                        enviarMensajeVerificacionEmail()

                    }else{
                        toast(getString(R.string.error_autentificar)+" error: "+task.exception?.message.toString())
                    }
                }
        }
    }
    private fun enviarMensajeVerificacionEmail() {
        auth.currentUser!!.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this,getString(R.string.email_enviado), Toast.LENGTH_SHORT).show()
                    val handler= Handler()
                    handler.postDelayed({
                        val intent =Intent(this,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    },500)
                    Log.e(":::TAG", "Email sent.")
                }
            }
    }
    fun comprobarcontraseña():Boolean{
        var identicas=false

        if (notEmpty()&& bindind.password.text.trim().toString()==bindind.repeatpassword.text.trim().toString()){
            identicas=true
        }else if (!notEmpty()){
            createAccountInputsArray.forEach { input->
                if (input.text.toString().trim().isEmpty()){
                    input.error="${input.hint} "+getString(R.string.requerido)
                }
            }
        }else{
            toast(getString(R.string.passNoCoincide))
        }
        return identicas
    }
}