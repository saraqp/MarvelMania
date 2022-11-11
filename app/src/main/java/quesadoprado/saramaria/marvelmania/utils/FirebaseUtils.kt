package quesadoprado.saramaria.marvelmania.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {
    val firebaseAuth:FirebaseAuth=FirebaseAuth.getInstance()
    val firebaseDatabase=FirebaseFirestore.getInstance()
}