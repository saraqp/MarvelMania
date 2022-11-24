package quesadoprado.saramaria.marvelmania.fragments

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.MainActivity
import quesadoprado.saramaria.marvelmania.activities.Register
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.FragmentLoginBinding
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

@Suppress("DEPRECATION")
class LoginFragment(private var auth: FirebaseAuth, private var nombreUsuarioND: TextView) :
    Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val database = firebaseDatabase
    private lateinit var loginAccountInputsArray: Array<EditText>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginAccountInputsArray = arrayOf(binding.ETemail, binding.ETPpassword)
        binding.btnLogin.setOnClickListener {
            login()
            hideKeyBoard(view)
        }
        binding.registrar.setOnClickListener {
            val intentRegistro = Intent(context, Register::class.java)
            startActivity(intentRegistro)
        }
    }

    private fun login() {
        if (notEmpty()) {
            val email = binding.ETemail.text.toString()
            val pass = binding.ETPpassword.text.toString()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        /*recuperamos de la cuenta que se loguea su nombre de usuario y en el
                          navigation drawer cambiamos "anonymous" por el nombre de usuario conectado
                        */
                        cambiarUsernameNavigationDrawer(auth.currentUser!!.uid)

                        //dejamos medio segundo y cambiamos a la pantalla de home (biblioteca)
                        val handler = Handler()
                        handler.postDelayed({
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }, 500)
                    } else {
                        Snackbar.make(
                            binding.contentLogin,
                            getString(R.string.error_autentificar),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
        } else if (!notEmpty()) {
            //mostrar un error por cada campo vacio
            loginAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} " + getString(R.string.requerido)
                }
            }
        }
    }

    //OCULTAR TECLADO
    @RequiresApi(Build.VERSION_CODES.R)
    private fun Fragment.hideKeyBoard(view: View?=activity?.window?.decorView?.rootView){
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.R){
            view?.hideKeyBoard(view)
        }else{
            inputMethodManager()?.hideSoftInputFromWindow(view?.applicationWindowToken,0)
        }
    }
    private fun Fragment.inputMethodManager()= context?.getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager

    @RequiresApi(Build.VERSION_CODES.R)
    private fun View.hideKeyBoard(view: View){
        windowInsetsController?.hide(WindowInsets.Type.ime())
        view.clearFocus()
    }
    //NAVIGATION DRAWER
    private fun cambiarUsernameNavigationDrawer(uid: String) {
        val sfDocRef = database.collection("users").document(uid)
        database.runTransaction { transaction ->
            val snapshot = transaction.get(sfDocRef)
            val actuUsername = snapshot.getLong("displayName")!!
            DataBaseUtils.cambiarStatusUser(uid, getString(R.string.online))
            nombreUsuarioND.text = actuUsername.toString()
        }
    }

    private fun notEmpty(): Boolean = binding.ETemail.text?.trim().toString().isNotEmpty()
            && binding.ETPpassword.text?.trim().toString().isNotEmpty()


}