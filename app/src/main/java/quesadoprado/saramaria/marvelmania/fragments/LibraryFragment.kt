package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ViewPagerAdapter
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding

class LibraryFragment(private val auth: FirebaseAuth) : Fragment() {
    private var _binding: FragmentLibraryBinding?=null
    private val binding get() = _binding!!

    private val currentUser = auth.currentUser
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View {
        _binding= FragmentLibraryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //si el usuario esta conectado se le muestran sus favoritos
        if(currentUser != null){
            binding.mensajeNoLogueados.visibility=View.GONE
            binding.tabLayout.visibility=View.VISIBLE
            binding.viewPager.visibility=View.VISIBLE
            val adapter=ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(ListFavoritesCharacters(),getString(R.string.personajes))
            adapter.addFragment(ListFavoritesComics(),getString(R.string.comics))
            adapter.addFragment(ListFavoritesSeries(),getString(R.string.series))
            binding.viewPager.adapter=adapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
            //si no esta conectado se le muestra un mensaje diciendo que tiene que conectarse
        }else{
            binding.mensajeNoLogueados.text=getString(R.string.loginLibrary)
            binding.mensajeNoLogueados.visibility=View.VISIBLE
            binding.tabLayout.visibility=View.GONE
            binding.viewPager.visibility=View.GONE
        }

    }
}