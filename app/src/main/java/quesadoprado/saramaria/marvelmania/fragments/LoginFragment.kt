package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding

class LoginFragment : Fragment() {
    private lateinit var auth:FirebaseAuth

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
        auth= Firebase.auth
    }

}