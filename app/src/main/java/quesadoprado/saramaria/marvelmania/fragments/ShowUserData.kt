package quesadoprado.saramaria.marvelmania.fragments

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseStorage
import quesadoprado.saramaria.marvelmania.utils.UtilsApp

class ShowUserData(
    private var auth: FirebaseAuth,
    private var nombreUsuarioND: TextView,
    private var imageUser: ImageView,
    private var submenuLogin: MenuItem?,
    private var database: FirebaseFirestore
) : Fragment() {

    private var _binding: FragmentShowUserDataBinding? = null
    private val binding get() = _binding!!
    private var user: User? = null

    private val storage = firebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShowUserDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uid: String = auth.currentUser!!.uid
        //ponemos la imagen del usuario
        UtilsApp.mostrarImagenUser(uid,binding.imageView,imageUser,requireContext())

        //obtenemos los datos del usuario con su uid
        obtenerDatosUser(uid)

        binding.emailtext.text = auth.currentUser!!.email
        binding.logout.setOnClickListener {
            //desconectamos al usuario de firebase Authenticator
            FirebaseUtils.firebaseAuth.signOut()
            //ponemos los datos de Navigation Drawer a default (usuario desconectado)
            nombreUsuarioND.text = getString(R.string.sinUsuario)
            /*cambiamos el acceso directo de Navigation Drawer de "perfil" a "iniciar sesión"
            junto con su icono para que sea mas intuitivo al usuario
            */
            submenuLogin!!.title = getString(R.string.inicio_sesion)
            submenuLogin!!.setIcon(R.drawable.login_icon)

            //cambiamos en la base de datos su estado a offline
            DataBaseUtils.cambiarStatusUser(user!!.uid!!, getString(R.string.offline))
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        //cambiar la contraseña del usuario
        binding.botonActualizarPass.setOnClickListener {
            //comprobamos que ninguno de los cambos esté vacío
            if (!comprobarNulos()) {
                //obtenemos los datos antiguos del usuario
                obtenerDatosUser(uid)
                //comprobamos que la contraseña antigua coincida con el campo "antigua contraseña"
                if (comprobarAntiguaContrasena(user)) {
                    //obtenemos credenciales usuario
                    val userCredential: AuthCredential =
                        EmailAuthProvider.getCredential(user!!.email!!, user!!.pass!!)
                    //autenticamos el inicio de sesion con los datos del usuario
                    auth.currentUser!!.reauthenticate(userCredential).addOnCompleteListener {
                        if (it.isSuccessful) {
                            //cambiamos la contraseña de firebase Authenticator
                            auth.currentUser!!.updatePassword(binding.passtextrepetirnueva.text.toString())
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        //cambiamos el campo contraseña en la base de datos firestore
                                        DataBaseUtils.cambiarPassUser(
                                            user!!,
                                            binding.passtextnueva.text.toString()
                                        )
                                        //avidamos al usuario que la contraseña se ha cambiado correctamente
                                        Snackbar.make(
                                            view,
                                            getString(R.string.cambioPassCorrecto),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        //avisamos al usuario que ha ocurrido un error al cambiar la contraseña
                                        Snackbar.make(
                                            view,
                                            getString(R.string.cambioPassError),
                                            Snackbar.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        } else {
                            Snackbar.make(view, getString(R.string.error), Snackbar.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Snackbar.make(
                        view,
                        getString(R.string.oldpassNocoincide),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(view, getString(R.string.camposRellenos), Snackbar.LENGTH_SHORT)
                    .show()
            }
        }

        binding.deleteUser.setOnClickListener {
            //obtenemos los credenciales del usuario
            val userCredential: AuthCredential =
                EmailAuthProvider.getCredential(user!!.email!!, user!!.pass!!)

            //verificamos que el usuario está seguro de borrar su cuenta
            val builder = AlertDialog.Builder(context)

            builder.setMessage(getString(R.string.asegurarBorradoUser))
                    //En caso afirmativo eliminamos la cuenta
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
                 //en caso negativo no hacemos nada
                .setNegativeButton(getString(R.string.no)) { _, _ ->}
                .show()

        }
        //cambiar imagen usuario
        binding.imageView.setOnClickListener {
            fileManager()


        }
    }

    private fun fileManager() {
        //abrir galeria para seleccionar imagen
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        imagePickerActivityResult.launch(galleryIntent)
    }

    //añadir la imagen nueva del usuario
    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result != null) {
                val imageUri: Uri? = result.data?.data
                if (imageUri != null) {
                    //añadimos nueva imagen del usuario a Firebase storage
                    val uploadTask =storage.child("file/${auth.currentUser!!.uid}").putFile(imageUri)

                    /*obtenemos la imagen del storage y actualizamos tanto visualmente en el
                        fragment el icono como el Navigation Drawer
                     */
                    uploadTask.addOnSuccessListener {
                        UtilsApp.mostrarImagenUser(auth.currentUser!!.uid,binding.imageView,imageUser,requireContext())
                    }.addOnFailureListener {
                        Snackbar.make(
                            requireView(),
                            getString(R.string.error),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }

            }
        }

    private fun obtenerDatosUser(uid: String) {
        //obtenemos de la base de datos los datos del usuario
        database.collection("users").document(uid).get().addOnSuccessListener { document ->
            if (document != null) {
                user = User()
                user = User(
                    document.data?.get("displayName") as String?,
                    document.data?.get("status") as String?,
                    //añadimos su uid
                    auth.currentUser!!.uid,
                    document.data?.get("password") as String?,
                    document.data?.get("email") as String?
                )
                //mostramos su nombre de usuario
                binding.user.text = user!!.username
            } else {
                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun comprobarNulos(): Boolean {
        return binding.passtext.text.trim().toString()
            .isEmpty() || binding.passtextnueva.text.trim().toString()
            .isEmpty() || binding.passtextrepetirnueva.text.trim().toString().isEmpty()
    }

    private fun comprobarAntiguaContrasena(user: User?): Boolean {
        return binding.passtext.text.trim().toString() == user?.pass!!.trim()
    }
}