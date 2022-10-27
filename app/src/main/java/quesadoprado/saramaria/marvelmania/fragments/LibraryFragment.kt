package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding


class LibraryFragment(auth: FirebaseAuth) : Fragment() {
    private val auth=auth
    private var _binding: FragmentLibraryBinding?=null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentLibraryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentUser = auth.currentUser
        if(currentUser != null){
            binding.textView.text="usuario logueado"
        }else{
            binding.textView.text="debe iniciar sesión para ver su biblioteca"
        }
    }

}