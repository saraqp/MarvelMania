package quesadoprado.saramaria.marvelmania.activities

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.ActivityMainBinding
import quesadoprado.saramaria.marvelmania.fragments.*
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener{

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var toggle:ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    private var database= firebaseDatabase
    private lateinit var nombreUsuarioND:TextView
    private var submenuLogin:MenuItem?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //NAVIGATION DRAWER
        toggle=ActionBarDrawerToggle(this,binding.drawerLayout,R.string.abierto,R.string.cerrado)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.navView.setNavigationItemSelectedListener (this)

        //obtenemos la cabecera
        val headerView=binding.navView.getHeaderView(0)
        nombreUsuarioND= headerView.findViewById(R.id.user_name)
        submenuLogin=binding.navView.menu.get(4).subMenu!!.get(0)
        if (firebaseAuth.currentUser!=null){
            submenuLogin!!.title = "Account"
            submenuLogin!!.setIcon(R.drawable.ic_account_settings)
        }

        cambiarNombreUser(firebaseAuth.currentUser?.email)

        //Firebase analytics
        firebaseAnalytics=FirebaseAnalytics.getInstance(this)



        //para que salga la biblioteca por default
        setToolBarTitle(getString(R.string.biblioteca))
        changeFragment(LibraryFragment(firebaseAuth, nombreUsuarioND))
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
                changeFragment(CharactersFragment(firebaseAuth))
            }
            R.id.nav_comics->{
                setToolBarTitle(getString(R.string.comics))
                changeFragment(ComicsFragment(firebaseAuth))
            }
            R.id.nav_series->{
                setToolBarTitle(getString(R.string.series))
                changeFragment(SeriesFragment(firebaseAuth))
            }
            R.id.nav_login->{
                if (firebaseAuth.currentUser!=null){
                    setToolBarTitle(getString(R.string.datosUsuario))
                    changeFragment(ShowUserData(firebaseAuth, nombreUsuarioND,submenuLogin, firebaseDatabase))
                }else {
                    setToolBarTitle(getString(R.string.inicio_sesion))
                    changeFragment(LoginFragment(firebaseAuth, nombreUsuarioND))
                }
            }
            R.id.nav_home->{
                setToolBarTitle(getString(R.string.biblioteca))
                changeFragment(LibraryFragment(firebaseAuth,nombreUsuarioND))
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
    private fun cambiarNombreUser(email: String?){
        if (email.isNullOrEmpty()){
            nombreUsuarioND.text=getString(R.string.sinUsuario)
        }else{
            obteneruser(firebaseAuth.currentUser!!.email.toString())
        }
    }
    private fun obteneruser(email:String){
        var user:User?=null
        database.collection("users").document(email).get().addOnSuccessListener { document->
            if (document != null) {
                user= User(document.data?.get("displayName") as String?,
                    document.data?.get("status") as String?, document.data?.get("uid") as String?
                )
                nombreUsuarioND.text=user?.username
            } else {
                Log.d(":::TAG", "No such document")
            }
        }
    }
}


