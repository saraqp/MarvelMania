package quesadoprado.saramaria.marvelmania.fragments

import android.app.AlertDialog
import android.content.Context
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
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.MainActivity
import quesadoprado.saramaria.marvelmania.activities.Register
import quesadoprado.saramaria.marvelmania.databinding.FragmentLoginBinding
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase
import quesadoprado.saramaria.marvelmania.utils.UtilsApp

@Suppress("DEPRECATION")
class LoginFragment(
    private var auth: FirebaseAuth, private var nombreUsuarioND: TextView,
    private val imageUser: ImageView
) :
    Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val database = firebaseDatabase
    private lateinit var loginAccountInputsArray: Array<EditText>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //array de los elementos necesarios para posibles errores
        loginAccountInputsArray = arrayOf(binding.ETemail, binding.ETPpassword)
        UtilsApp.mostrarImagenUser(
            getString(R.string.defaultImage),
            null,
            imageUser,
            requireContext()
        )

        binding.btnLogin.setOnClickListener {
            login()
            //ocultamos el teclado para poder ver en caso de que suceda el mensaje de error
            hideKeyBoard(view)
        }

        binding.registrar.setOnClickListener {
            //nos dirigimos a la pantalla de registro
            val intentRegistro = Intent(context, Register::class.java)
            startActivity(intentRegistro)
        }
        binding.forgotPass.setOnClickListener {
            changePass()
        }
    }

    //Cambiar la contraseña del usuario en caso de que no se acuerde
    private fun changePass() {
        /*Mostramos un dialogo para que el usuario escriba su email y enviarle un
        * correo electronico para cambiar la contraseña
         */
        val dialogForgotPass: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_resetpass, null)
        val email = dialogLayout.findViewById<EditText>(R.id.email_resetPass)

        with(dialogForgotPass) {
            setTitle(getString(R.string.cambiarpass))
            setPositiveButton(getString(R.string.cambiarpass)) { _, _ ->
                if (email.text.toString().isNotEmpty()) {
                    auth.sendPasswordResetEmail(email.text.toString())
                        .addOnSuccessListener { task ->
                            Snackbar.make(
                                requireView(),
                                getString(R.string.email_enviado) + email.text.toString(),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                } else {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.campoNoVacio),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
            setNegativeButton(getString(R.string.cancel)) { _, _ ->
            }
            setView(dialogLayout)
            show()
        }
    }

    //Iniciar sesion al usuario
    private fun login() {
        if (notEmpty()) {
            val email = binding.ETemail.text.toString()
            val pass = binding.ETPpassword.text.toString()
            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener { task ->
                    /*recuperamos de la cuenta que se loguea su nombre de usuario y en el
                      navigation drawer cambiamos "anonymous" por el nombre de usuario conectado
                    */
                    cambiarUsernameNavigationDrawer(auth.currentUser!!.uid)
                    //actualizamos la pass por si acaso el usuario olvido la contraseña y la cambio
                    DataBaseUtils.cambiarPassUser(auth.currentUser!!.uid, pass)
                    //dejamos medio segundo y cambiamos a la pantalla de home (biblioteca)
                    val handler = Handler()
                    handler.postDelayed({
                        val intent = Intent(context, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }, 500)
                }.addOnFailureListener {
                    Snackbar.make(
                        binding.contentLogin,
                        getString(R.string.error_autentificar),
                        Snackbar.LENGTH_SHORT
                    ).show()
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
    private fun Fragment.hideKeyBoard(view: View? = activity?.window?.decorView?.rootView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view?.hideKeyBoard(view)
        } else {
            inputMethodManager()?.hideSoftInputFromWindow(view?.applicationWindowToken, 0)
        }
    }

    private fun Fragment.inputMethodManager() =
        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager

    @RequiresApi(Build.VERSION_CODES.R)
    private fun View.hideKeyBoard(view: View) {
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