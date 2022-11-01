package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.MainActivity
import quesadoprado.saramaria.marvelmania.activities.Register
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseUser

class LoginFragment(private var auth: FirebaseAuth) : Fragment() {
    private var conectado=false
    private val currentUser = firebaseUser

    private lateinit var loginAccountInputsArray:Array<EditText>

    private var emailET: EditText? = null
    private var passwordET:EditText? = null

    override fun onStart() {
        super.onStart()
        if(currentUser?.uid!=null){
            conectado=true
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (!conectado){
            return inflater.inflate(R.layout.fragment_login,container,false)
        }else{
            return inflater.inflate(R.layout.login_conect,container,false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!conectado) {

            emailET=view.findViewById(R.id.ETemail)
            passwordET=view.findViewById(R.id.ETPpassword)
            loginAccountInputsArray= arrayOf(emailET!!,passwordET!!)
            val login:Button= view.findViewById(R.id.btnLogin)
            login.setOnClickListener{
                login()
            }

            val registrar: TextView = view.findViewById(R.id.registrar)
            registrar.setOnClickListener {
                val intentRegistro= Intent(context,Register::class.java)
                startActivity(intentRegistro)
            }

        }else{
            //si el usuario esta logueado

        }
    }
    private fun login() {
        if (notEmpty()){
            val email=emailET!!.text.toString()
            val pass=passwordET!!.text.toString()
            auth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful){

                        val intent =Intent(context,MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }else{
                        Toast.makeText(context,getString(R.string.error_autentificar),Toast.LENGTH_SHORT).show()
                    }
                }
        }else if (!notEmpty()){
            loginAccountInputsArray.forEach { input->
                if (input.text.toString().trim().isEmpty()){
                    input.error="${input.hint} "+getString(R.string.requerido)
                }
            }
        }


    }
    private fun notEmpty():Boolean=emailET?.text?.trim().toString().isNotEmpty()
            && passwordET?.text?.trim().toString().isNotEmpty()


}