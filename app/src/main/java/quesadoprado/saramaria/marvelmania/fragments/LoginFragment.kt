package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R

class LoginFragment(private val auth: FirebaseAuth) : Fragment() {
    var conectado=false
    val currentUser = auth.currentUser
    override fun onStart() {
        super.onStart()
        if(currentUser != null){
            conectado=true
        }
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (conectado==false){
            return inflater.inflate(R.layout.fragment_login,container,false)
        }else{
            return inflater.inflate(R.layout.login_conect,container,false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (conectado==false) {
            val registrar: TextView = view.findViewById(R.id.registrar)
            registrar.setOnClickListener {

            }
        }else{
            Toast.makeText(context,"ESTO FUNCIONA2",Toast.LENGTH_SHORT).show()
        }
    }

}