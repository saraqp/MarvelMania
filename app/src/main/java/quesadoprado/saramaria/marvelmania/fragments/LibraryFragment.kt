package quesadoprado.saramaria.marvelmania.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.adapter.ViewPagerAdapter
import quesadoprado.saramaria.marvelmania.databinding.FragmentLibraryBinding
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils

class LibraryFragment(private val auth: FirebaseAuth, private val imageUser: ImageView) :
    Fragment() {
    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private val currentUser = auth.currentUser
    private val storage = FirebaseUtils.firebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mostrarImagenUser()
        //si el usuario esta conectado se le muestran sus favoritos
        if (currentUser != null) {
            binding.mensajeNoLogueados.visibility = View.GONE
            binding.tabLayout.visibility = View.VISIBLE
            binding.viewPager.visibility = View.VISIBLE
            val adapter = ViewPagerAdapter(childFragmentManager)
            adapter.addFragment(ListFavoritesCharactersFragment(), getString(R.string.personajes))
            adapter.addFragment(ListFavoritesComicsFragment(), getString(R.string.comics))
            adapter.addFragment(ListFavoritesSeriesFragment(), getString(R.string.series))
            binding.viewPager.adapter = adapter
            binding.tabLayout.setupWithViewPager(binding.viewPager)
            //si no esta conectado se le muestra un mensaje diciendo que tiene que conectarse
        } else {
            binding.mensajeNoLogueados.text = getString(R.string.loginLibrary)
            binding.mensajeNoLogueados.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.GONE
            binding.viewPager.visibility = View.GONE
        }

    }

    private fun mostrarImagenUser() {
        if (auth.currentUser!=null) {
            storage.child("file/${auth.currentUser!!.uid}").downloadUrl.addOnSuccessListener {
                Glide.with(this)
                    .load(it)
                    .apply(RequestOptions().override(512, 512))
                    .circleCrop()
                    .into(imageUser)
            }.addOnFailureListener {
                Glide.with(this)
                    .load(R.mipmap.icon)
                    .apply(RequestOptions().override(512, 512))
                    .circleCrop()
                    .into(imageUser)
            }
        }else{
            Glide.with(this)
                .load(R.mipmap.icon)
                .apply(RequestOptions().override(512, 512))
                .circleCrop()
                .into(imageUser)
        }
    }
}