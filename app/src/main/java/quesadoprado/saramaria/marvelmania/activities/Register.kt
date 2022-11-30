package quesadoprado.saramaria.marvelmania.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.ActivityRegisterBinding
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth

class Register : AppCompatActivity() {
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    lateinit var bindind: ActivityRegisterBinding
    private lateinit var createAccountInputsArray: Array<EditText>
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = firebaseAuth
        super.onCreate(savedInstanceState)
        bindind = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(bindind.root)
        //guardamos en una lista los editText para mostrar los posibles errores correctamente
        createAccountInputsArray =
            arrayOf(bindind.user, bindind.email, bindind.password, bindind.repeatpassword)

        bindind.btnRegistrar.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        if (comprobarCamposVaciosYContrasenaCorrecta()) {
            val username = bindind.user.text.toString()
            userEmail = bindind.email.text.toString().trim()
            userPassword = bindind.password.text.toString().trim()

            //Se crea el usuario con Firebase Authenticator
            auth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnSuccessListener { task ->
                    val firebaseUserID = auth.currentUser!!.uid
                    //Guardar en la base de datos
                    val user = User(
                        username,
                        getString(R.string.online),
                        firebaseUserID,
                        userPassword,
                        userEmail
                    )
                    //Ponemos de imagen por default el logo de la aplicación
                    val imageUri =
                        Uri.parse("android.resource://${this.packageName}/${R.mipmap.icon}")
                    //guardamos el usuario en la base de datos con la contraseña para poder cambiarla correctamente en un futuro
                    DataBaseUtils.guardarUsuarioEnBbdd(user, imageUri)
                    //Enviamos el mensaje de verificacion de email
                    enviarMensajeVerificacionEmail()
                }.addOnFailureListener { task ->
                    Snackbar.make(
                        bindind.contentRegister,
                        getString(R.string.error_autentificar) + " error: " + task.message.toString(),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        }
    }

    //Enviamos un mensaje al correo electrónico dado por el usuario para verificar su cuenta
    private fun enviarMensajeVerificacionEmail() {
        auth.currentUser!!.sendEmailVerification()
            .addOnSuccessListener { _ ->
                Snackbar.make(
                    bindind.contentRegister,
                    getString(R.string.email_enviado) + auth.currentUser!!.email,
                    Snackbar.LENGTH_LONG
                ).show()
                val handler = Handler()
                handler.postDelayed({
                    //nos dirigimos al main con la cuenta de usuario iniciada
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }, 500)
            }
    }

    private fun comprobarCamposVaciosYContrasenaCorrecta(): Boolean {
        var identicas = false

        if (notEmpty() && bindind.password.text.trim()
                .toString() == bindind.repeatpassword.text.trim().toString()
        ) {
            identicas = true
        } else if (!notEmpty()) {
            //mostrar un mensaje de que el campo que esta vacío es requerido
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} " + getString(R.string.requerido)
                }
            }
        } else {
            Snackbar.make(
                bindind.contentRegister,
                getString(R.string.passNoCoincide),
                Snackbar.LENGTH_SHORT
            ).show()
        }
        return identicas
    }

    private fun notEmpty(): Boolean = bindind.user.text.trim().toString().isNotEmpty()
            && bindind.email.text.trim().toString().isNotEmpty()
            && bindind.password.text.trim().toString().isNotEmpty()
            && bindind.repeatpassword.text.trim().toString().isNotEmpty()
}