package quesadoprado.saramaria.marvelmania.utils

import android.annotation.SuppressLint
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class DataBaseUtils {
    companion object{
        @SuppressLint("StaticFieldLeak")
        private val database=firebaseDatabase
        @Synchronized
        fun eliminarUsuario(uid:String){
            //ELIMINAMOS FAVORITOS
            //personajes
            borrarPersonajesFavUser(uid)
            //comics
            borrarComicsFavUser(uid)
            //series
            borrarSeriesFavUser(uid)
            //ELIMINAMOS USUARIO
            database.collection("users").document(uid).delete()

        }
        @Synchronized
        private fun borrarPersonajesFavUser(uid: String) {
            database.collection("users/$uid/characters")
                .get().addOnCompleteListener{personajes->
                    if (personajes.isSuccessful){
                        for (personaje in personajes.result) {
                            database.collection("users/$uid/characters").document(personaje.id).delete()
                        }
                    }
            }
        }
        @Synchronized
        private fun borrarComicsFavUser(uid: String){
            database.collection("users/$uid/comics")
                .get().addOnCompleteListener { comics->
                    if (comics.isSuccessful){
                        for (comic in comics.result){
                            database.collection("users/$uid/comics").document(comic.id).delete()
                        }
                    }
                }
        }
        @Synchronized
        private fun borrarSeriesFavUser(uid: String){
            database.collection("users/$uid/series")
                .get().addOnCompleteListener { series->
                    if (series.isSuccessful){
                        for (serie in series.result){
                            database.collection("users/$uid/series").document(serie.id).delete()
                        }
                    }
                }
        }
        @Synchronized
        fun guardarUsuarioEnBbdd(user:User){
            database.collection("users").document(user.uid!!).set(
                hashMapOf("email" to user.email,
                    "displayName" to user.username,
                    "password" to user.pass,
                    "status" to user.status)
            )
        }
        @Synchronized
        fun cambiarPassUser(user: User,password:String){
            database.collection("users").document(user.uid!!).set(
                hashMapOf(
                    "displayName" to user.username!!,
                    "status" to user.status,
                    "email" to user.email,
                    "password" to password
                )
            )
        }
        @Synchronized
        fun cambiarStatusUser(user: User, status:String){
            database.collection("users").document(user.uid!!).set(
                hashMapOf("displayName" to user.username!!,
                    "status" to status,
                    "email" to user.email,
                    "password" to user.pass
                )
            )
        }
        @Synchronized
        fun guardarPersonaje(uid:String,personaje:Character){
            //guardamos en una coleccion "characters" la información de los personajes
            database.collection("users").document(uid)
                .collection("characters").document(personaje.id.toString()).set(
                    hashMapOf(
                        "id" to personaje.id,
                        "name" to personaje.name,
                        "description" to personaje.description,
                        "thumbnail" to personaje.thumbnail
                    )
                )
        }
        @Synchronized
        fun eliminarPersonaje(uid: String, character: Character) {
             database.collection("users").document(uid)
                 .collection("characters")
                 .document(character.id.toString()).delete()
        }
        @Synchronized
        fun guardarComic(uid: String, comic: Comic) {
            //variaciones de portada
            val imagenes= mutableListOf<Thumbnail>()
            for (i in 0 until comic.images!!.size){
                val image = Thumbnail(comic.images[i].path,comic.images[i].extension)
                imagenes+=image
            }
            //guardamos en una coleccion llamada "comics" la información de los comics
            database.collection("users").document(uid)
                .collection("comics").document(comic.id.toString()).set(
                    hashMapOf(
                        "id" to comic.id,
                        "title" to comic.title,
                        "description" to comic.description,
                        "variantDescription" to comic.variantDescription,
                        "pageCount" to comic.pageCount,
                        "format" to comic.format,
                        "images" to imagenes,
                        "thumbnail" to comic.thumbnail
                    )
                )
        }
        @Synchronized
        fun eliminarComic(uid: String, comic: Comic){
            database.collection("users").document(uid)
                .collection("comics")
                .document(comic.id.toString()).delete()
        }
        @Synchronized
        fun guardarSerie(uid: String, serie: Serie) {
            database.collection("users").document(uid)
                .collection("series").document(serie.id.toString()).set(
                    hashMapOf(
                        "id" to serie.id,
                        "title" to serie.title,
                        "description" to serie.description,
                        "startYear" to serie.startYear,
                        "endYear" to serie.endYear,
                        "rating" to serie.rating,
                        "thumbnail" to serie.thumbnail,
                        "next" to serie.next,
                        "previous" to serie.previous
                    )
                )
        }
        @Synchronized
        fun eliminarSerie(uid: String, serie: Serie) {
            database.collection("users").document(uid)
                .collection("series")
                .document(serie.id.toString()).delete()
        }
    }
}