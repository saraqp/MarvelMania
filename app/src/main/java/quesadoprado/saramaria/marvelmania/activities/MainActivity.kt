package quesadoprado.saramaria.marvelmania.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.databinding.ActivityMainBinding
import quesadoprado.saramaria.marvelmania.fragments.*


class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener{
    private lateinit var auth: FirebaseAuth
    lateinit var toggle:ActionBarDrawerToggle
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggle=ActionBarDrawerToggle(this,binding.drawerLayout,R.string.abierto,R.string.cerrado)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener (this)

        //para que salga este fragment por default
        setToolBarTitle(getString(R.string.biblioteca))
        changeFragment(LibraryFragment(auth))

        //Firebase
        val analytics= FirebaseAnalytics.getInstance(this)
        val bundle=Bundle()
        bundle.putString("message","Integración de Firebase Completa")
        analytics.logEvent("InitScreen",bundle)
        //autentificador
        auth= Firebase.auth


    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.nav_characters->{
                setToolBarTitle(getString(R.string.personajes))

                changeFragment(CharactersFragment(auth))
            }
            R.id.nav_comics->{
                setToolBarTitle(getString(R.string.comics))
                changeFragment(ComicsFragment(auth))
            }
            R.id.nav_series->{
                setToolBarTitle(getString(R.string.series))
                changeFragment(SeriesFragment(auth))
            }
            R.id.nav_login->{
                setToolBarTitle(getString(R.string.inicio_sesion))
                changeFragment(LoginFragment(auth))
            }
            R.id.nav_home->{
                setToolBarTitle(getString(R.string.biblioteca))
                changeFragment(LibraryFragment(auth))
            }
        }
        return true
    }
    fun setToolBarTitle(title:String){
        supportActionBar?.title=title
    }
    fun changeFragment(frag: Fragment){
        val fragment=supportFragmentManager.beginTransaction()
        fragment.replace(R.id.fragmentcontainer,frag).commit()
    }

}


