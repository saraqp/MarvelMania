package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth


class LibraryFragment(private var auth: FirebaseAuth) : Fragment() {
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
            binding.textView.text="usuario logueado"
            binding.logout.setOnClickListener {
                firebaseAuth.signOut()
                binding.textView.text="debe iniciar sesión para ver su biblioteca"
            }
        }else{
            binding.textView.text="debe iniciar sesión para ver su biblioteca"
        }

    }

}