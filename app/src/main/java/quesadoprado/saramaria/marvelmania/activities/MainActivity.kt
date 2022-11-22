package quesadoprado.saramaria.marvelmania.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.databinding.ActivityMainBinding
import quesadoprado.saramaria.marvelmania.fragments.*
import quesadoprado.saramaria.marvelmania.utils.DataBaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class MainActivity : AppCompatActivity(),NavigationView.OnNavigationItemSelectedListener{

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var toggle:ActionBarDrawerToggle
    private lateinit var binding: ActivityMainBinding
    private var database= firebaseDatabase
    private lateinit var nombreUsuarioND:TextView
    private lateinit var imageUser:ImageView
    private var submenuLogin:MenuItem?=null

    private val storage= FirebaseUtils.firebaseStorage

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser!=null){
            DataBaseUtils.cambiarStatusUser(firebaseAuth.currentUser!!.uid,getString(R.string.online))
        }
    }
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
        imageUser=headerView.findViewById(R.id.image_view)
        submenuLogin= binding.navView.menu[4].subMenu!![0]

        if (firebaseAuth.currentUser!=null){
            submenuLogin!!.title = getString(R.string.perfil)
            submenuLogin!!.setIcon(R.drawable.ic_account_settings)
            obtenerImageUser()
        }else{
            ponerImagenDefault()
        }

        cambiarNombreUser(firebaseAuth.currentUser?.uid)

        //Firebase analytics
        firebaseAnalytics=FirebaseAnalytics.getInstance(this)



        //para que salga la biblioteca por default
        setToolBarTitle(getString(R.string.biblioteca))
        changeFragment(LibraryFragment(firebaseAuth,imageUser))
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun ponerImagenDefault() {
        Glide.with(this)
            .load(getDrawable(R.mipmap.icon))
            .apply(RequestOptions().override(512, 512))
            .circleCrop()
            .into(imageUser)
    }

    private fun obtenerImageUser() {
        storage.child("file/${firebaseAuth.currentUser!!.uid}").downloadUrl.addOnSuccessListener {
            Glide.with(this)
                .load(it)
                .apply(RequestOptions().override(512, 512))
                .circleCrop()
                .into(imageUser)
        }.addOnFailureListener {
            ponerImagenDefault()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }

    //controlar cierre de aplicación
    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)){
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }else {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.titleAlertExit))
                .setMessage(getString(R.string.msgAlertExit))
                .setPositiveButton(getString(R.string.si)){_,_->
                    DataBaseUtils.cambiarStatusUser(firebaseAuth.currentUser!!.uid,getString(R.string.offline))
                    super.onBackPressed()
                }.setNegativeButton(getString(R.string.no)){dialog,_->
                    dialog.dismiss()
                }.show()
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        when(item.itemId){
            R.id.nav_characters->{
                setToolBarTitle(getString(R.string.personajes))
                changeFragment(CharactersFragment(firebaseAuth,imageUser))
            }
            R.id.nav_comics->{
                setToolBarTitle(getString(R.string.comics))
                changeFragment(ComicsFragment(firebaseAuth,imageUser))
            }
            R.id.nav_series->{
                setToolBarTitle(getString(R.string.series))
                changeFragment(SeriesFragment(firebaseAuth,imageUser,nombreUsuarioND.text.toString()))
            }
            R.id.nav_login->{
                if (firebaseAuth.currentUser!=null){
                    setToolBarTitle(getString(R.string.datosUsuario))
                    changeFragment(ShowUserData(firebaseAuth, nombreUsuarioND,imageUser,submenuLogin, firebaseDatabase))
                }else {
                    setToolBarTitle(getString(R.string.inicio_sesion))
                    changeFragment(LoginFragment(firebaseAuth, nombreUsuarioND))
                }
            }
            R.id.nav_home->{
                setToolBarTitle(getString(R.string.biblioteca))
                changeFragment(LibraryFragment(firebaseAuth,imageUser))
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
    private fun cambiarNombreUser(uid: String?){
        if (uid.isNullOrEmpty()){
            nombreUsuarioND.text=getString(R.string.sinUsuario)
        }else{
            obteneruser(firebaseAuth.currentUser!!.uid)
        }
    }
    private fun obteneruser(uid:String){
        var user:User?
        database.collection("users").document(uid).get().addOnSuccessListener { document->
            if (document != null) {
                user= User(document.data?.get("displayName") as String?,
                    document.data?.get("status") as String?,
                    document.data?.get("uid") as String?,
                    document.data?.get("email") as String?
                )
                nombreUsuarioND.text=user?.username
            } else {
                Log.e("ERROR", "No such document")
            }
        }
    }
}


