package quesadoprado.saramaria.marvelmania.utils

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseUtils {
    val firebaseAuth:FirebaseAuth=FirebaseAuth.getInstance()
    @SuppressLint("StaticFieldLeak")
    val firebaseDatabase=FirebaseFirestore.getInstance()
    val firebaseStorage=FirebaseStorage.getInstance().reference
}