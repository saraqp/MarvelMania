package quesadoprado.saramaria.marvelmania.fragments

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth


class LibraryFragment(private var auth: FirebaseAuth, private var nombreUsuarioND: TextView) : Fragment() {
    private var _binding: FragmentLibraryBinding?=null
    private val binding get() = _binding!!

    private val currentUser = auth.currentUser
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentLibraryBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(currentUser != null){
            binding.mensajeNoLogueados.visibility=View.GONE


        }else{
            binding.mensajeNoLogueados.text=getString(R.string.loginLibrary)
            binding.mensajeNoLogueados.visibility=View.VISIBLE
        }

    }

}