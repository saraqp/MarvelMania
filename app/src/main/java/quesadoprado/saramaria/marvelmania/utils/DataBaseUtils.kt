package quesadoprado.saramaria.marvelmania.utils

import android.annotation.SuppressLint
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.items.ItemCSum
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class DataBaseUtils {
    companion object{
        @SuppressLint("StaticFieldLeak")
        private val database=firebaseDatabase
        //TODO: añadir el borrar comics y series al borrar usuario
        @Synchronized
        fun eliminarUsuario(uid:String){
            //ELIMINAMOS FAVORITOS
            //eliminamos personajes
            borrarPersonajesFavUser(uid)
            //ELIMINAMOS USUARIO
            database.collection("users").document(uid).delete()

        }
        @Synchronized
        private fun borrarPersonajesFavUser(uid: String) {
            database.collection("users/$uid/characters").get().addOnCompleteListener{personajes->
                if (personajes.isSuccessful){
                    for (personaje in personajes.result) {
                        if (personaje.exists()) {
                            //eliminamos las series de los personajes
                            database.collection("users/$uid/characters/${personaje.id}/series").get().addOnCompleteListener { series->
                                if (series.isSuccessful){
                                    for (serie in series.result){
                                        if (serie.exists()){
                                            var nombreSerie= serie.data["name"] as String
                                            database.collection("users/$uid/characters/${personaje.id}/series").document(nombreSerie).delete()
                                        }
                                    }
                                }
                            }
                            database.collection("users/$uid/characters").document(personaje.id).delete()
                        }
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
                ).addOnSuccessListener {
                    //como tiene series guardamos tambien en una subcoleccion las series que tiene
                    for (i in 0 until personaje.series!!.items!!.size) {
                        val serie = personaje.series.items?.get(i)
                        database.collection("users/$uid/characters").document(personaje.id.toString())
                            .collection("series").document(serie!!.name!!).set(
                                hashMapOf(
                                    "name" to serie.name,
                                    "resourceUri" to serie.resourceURI
                                )
                            )
                    }
                }
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
                ).addOnSuccessListener{
                    //guardamos sus personajes en otra coleccion
                    for (i in 0 until comic.characters!!.items!!.size){
                        val personaje= comic.characters.items?.get(i)
                        database.collection("users/$uid/comics").document(comic.id.toString())
                            .collection("characters").document(personaje!!.name!!).set(
                                hashMapOf(
                                    "name" to personaje.name,
                                    "resourceUri" to personaje.resourceURI,
                                    "rol" to personaje.role
                                )
                            )
                    }
                }
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
                ).addOnSuccessListener {
                    //insertamos tambien los comics que le pertenecen
                    for (i in 0 until serie.comics!!.items!!.size){
                        val comic=serie.comics.items?.get(i)
                        database.collection("users/$uid/series/${serie.id.toString()}/comics")
                            .document(comic!!.name!!).set(
                                hashMapOf(
                                    "name" to comic.name,
                                    "resourceUri" to comic.resourceURI
                                )
                            )
                    }
                    //tambien guardamos sus personajes
                    for (i in 0 until serie.characters!!.items!!.size){
                        val personaje= serie.characters.items?.get(i)
                        database.collection("users/$uid/series/${serie.id.toString()}/characters")
                            .document(personaje!!.name!!).set(
                                hashMapOf(
                                    "name" to personaje.name,
                                    "resourceUri" to personaje.resourceURI,
                                    "rol" to personaje.role
                                )
                            )
                    }
                }
        }
        @Synchronized
        fun eliminarSerie(uid: String, serie: Serie) {
            database.collection("users").document(uid)
                .collection("series")
                .document(serie.id.toString()).delete()
        }
    }
}