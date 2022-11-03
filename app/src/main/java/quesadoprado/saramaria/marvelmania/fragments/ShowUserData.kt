package quesadoprado.saramaria.marvelmania.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.activities.MainActivity
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.FragmentShowUserDataBinding
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class ShowUserData(
    private var auth: FirebaseAuth,
    private var nombreUsuarioND: TextView,
    private var submenuLogin: MenuItem?,
    private var database: FirebaseFirestore
) : Fragment() {
    private var _binding: FragmentShowUserDataBinding?=null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentShowUserDataBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email: String =auth.currentUser!!.email.toString()
        var user:User?=null
        //obtenemos los datos del usuario con su email
        database.collection("users").document(email).get().addOnSuccessListener {document->
            if (document != null) {
                user= User(document.data?.get("displayName") as String?,
                    document.data?.get("status") as String?,
                    document.data?.get("uid") as String?,
                    document.data?.get("password") as String?
                )
                binding.user.text= user!!.username
            } else {
                Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()
            }
        }


        binding.emailtext.text=email
        binding.logout.setOnClickListener {
            FirebaseUtils.firebaseAuth.signOut()
            nombreUsuarioND.text=getString(R.string.sinUsuario)
            submenuLogin!!.title = getString(R.string.inicio_sesion)
            submenuLogin!!.setIcon(R.drawable.login_icon)
            //cambiamos en la base de datos su estado a offline
            database.collection("users").document(email).set(
                hashMapOf("displayName" to user?.username!!,
                    "status" to "offline",
                    "uid" to user?.uid,
                    "password" to user?.pass
                )
            )
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        binding.botonActualizarPerfil.setOnClickListener {
            if (comprobarAntiguaContraseña(user)){
                val userCredential:AuthCredential=EmailAuthProvider.getCredential(email,user!!.pass!!)
                auth.currentUser!!.reauthenticate(userCredential).addOnCompleteListener {
                    if (it.isSuccessful){
                        auth.currentUser!!.updatePassword(binding.passtextrepetirnueva.text.toString()).addOnCompleteListener {
                            task->
                            if (task.isSuccessful){
                                database.collection("users").document(email).set(
                                    hashMapOf("displayName" to user?.username!!,
                                        "status" to user?.status,
                                        "uid" to user?.uid,
                                        "password" to binding.passtextnueva.text.toString()
                                    )
                                )
                                Toast.makeText(context,getString(R.string.cambioPassCorrecto),Toast.LENGTH_SHORT).show()
                            }else{
                                Log.e(":::TAG", it.exception!!.message.toString())
                                Toast.makeText(context,getString(R.string.cambioPassError),Toast.LENGTH_SHORT).show()
                            }
                        }
                    }else{
                        Toast.makeText(context,getString(R.string.error),Toast.LENGTH_SHORT).show()
                    }
                }


            }else{
                Toast.makeText(context,getString(R.string.oldpassNocoincide),Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun comprobarAntiguaContraseña(user: User?): Boolean {
        return binding.passtext.text.trim().toString()== user?.pass!!.trim()
    }
}