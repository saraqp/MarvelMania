package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.FragmentCharactersBinding
import quesadoprado.saramaria.marvelmania.databinding.FragmentComicsBinding
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding


class LibraryFragment : Fragment() {
    private var _binding: FragmentLibraryBinding?=null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding= FragmentLibraryBinding.inflate(inflater,container,false)
        return binding.root
    }

}