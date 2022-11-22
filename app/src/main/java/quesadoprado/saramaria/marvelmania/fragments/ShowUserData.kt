package quesadoprado.saramaria.marvelmania.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.MainActivity
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.FragmentShowUserDataBinding
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ShowUserData(
    private var auth: FirebaseAuth,
    private var nombreUsuarioND: TextView,
    private var submenuLogin: MenuItem?,
    private var database: FirebaseFirestore
) : Fragment() {
    private var _binding: FragmentShowUserDataBinding?=null
    private val binding get() = _binding!!
    private var user:User?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentShowUserDataBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid: String =auth.currentUser!!.uid
        //obtenemos los datos del usuario con su uid
        obtenerDatosUser(uid)



        binding.emailtext.text=auth.currentUser!!.email
        binding.logout.setOnClickListener {
            FirebaseUtils.firebaseAuth.signOut()
            nombreUsuarioND.text=getString(R.string.sinUsuario)
            submenuLogin!!.title = getString(R.string.inicio_sesion)
            submenuLogin!!.setIcon(R.drawable.login_icon)
            //cambiamos en la base de datos su estado a offline
            DataBaseUtils.cambiarStatusUser(user!!.uid!!,getString(R.string.offline))
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        //cambiar la contraseña del usuario
        binding.botonActualizarPerfil.setOnClickListener {
            if(!comprobarNulos()) {
                obtenerDatosUser(uid)
                if (comprobarAntiguaContrasena(user)) {
                    val userCredential: AuthCredential =EmailAuthProvider.getCredential(user!!.email!!, user!!.pass!!)
                    auth.currentUser!!.reauthenticate(userCredential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            auth.currentUser!!.updatePassword(binding.passtextrepetirnueva.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        DataBaseUtils.cambiarPassUser(user!!,binding.passtextnueva.text.toString())
                                        Snackbar.make(view,getString(R.string.cambioPassCorrecto),Snackbar.LENGTH_SHORT).show()
                                    } else {
                                        Snackbar.make(view,getString(R.string.cambioPassError),Snackbar.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Snackbar.make(view,getString(R.string.error),Snackbar.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Snackbar.make(view,getString(R.string.oldpassNocoincide),Snackbar.LENGTH_SHORT).show()
                }
            }else{
                Snackbar.make(view,getString(R.string.camposRellenos),Snackbar.LENGTH_SHORT).show()
            }
        }
        binding.deleteUser.setOnClickListener {
            val userCredential: AuthCredential =EmailAuthProvider.getCredential(user!!.email!!, user!!.pass!!)
            val builder=AlertDialog.Builder(context)
            //comprobamos que es usuario está seguro de que quiere eliminar su cuenta
            builder.setMessage(getString(R.string.asegurarBorradoUser))
                .setPositiveButton(getString(R.string.si)) { _, _ ->
                    auth.currentUser!!.reauthenticate(userCredential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //eliminamos al usuario tambien de la base de datos
                            DataBaseUtils.eliminarUsuario(auth.currentUser!!.uid)
                            auth.currentUser!!.delete().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    //nos dirigimos al fragment base (biblioteca)
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                    startActivity(intent)
                                }
                            }
                        }
                    }
                }
                .setNegativeButton(getString(R.string.no)) { _, _ ->
                }.show()

        }
    }

    private fun obtenerDatosUser(uid: String) {
        database.collection("users").document(uid).get().addOnSuccessListener {document->
            if (document != null) {
                user=User()
                user= User(document.data?.get("displayName") as String?,
                    document.data?.get("status") as String?,
                    auth.currentUser!!.uid,
                    document.data?.get("password") as String?,
                    document.data?.get("email") as String?
                )
                binding.user.text= user!!.username
            } else {
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun comprobarNulos(): Boolean {
        return binding.passtext.text.trim().toString().isEmpty()||binding.passtextnueva.text.trim().toString().isEmpty()||binding.passtextrepetirnueva.text.trim().toString().isEmpty()
    }

    private fun comprobarAntiguaContrasena(user: User?): Boolean {
        return binding.passtext.text.trim().toString()== user?.pass!!.trim()
    }
}