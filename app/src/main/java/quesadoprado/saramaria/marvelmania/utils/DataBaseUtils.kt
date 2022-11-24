package quesadoprado.saramaria.marvelmania.utils

import android.annotation.SuppressLint
import quesadoprado.saramaria.marvelmania.data.characters.Character
import quesadoprado.saramaria.marvelmania.data.comics.Comic
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail
import quesadoprado.saramaria.marvelmania.data.series.Serie
import quesadoprado.saramaria.marvelmania.data.util.Coment
import quesadoprado.saramaria.marvelmania.data.util.User
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseAuth
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseDatabase

class DataBaseUtils {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private val database = firebaseDatabase
        private val auth = firebaseAuth

        //FUNCIONES
        //ELIMINACIÓN COMPLETA DE USUARIO CON SUS FAVORITOS
        fun eliminarUsuario(uid: String) {
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

        private fun borrarPersonajesFavUser(uid: String) {
            database.collection("users/$uid/characters")
                .get().addOnCompleteListener { personajes ->
                    if (personajes.isSuccessful) {
                        for (personaje in personajes.result) {
                            database.collection("users/$uid/characters").document(personaje.id)
                                .delete()
                        }
                    }
                }
        }

        private fun borrarComicsFavUser(uid: String) {
            database.collection("users/$uid/comics")
                .get().addOnCompleteListener { comics ->
                    if (comics.isSuccessful) {
                        for (comic in comics.result) {
                            database.collection("users/$uid/comics").document(comic.id).delete()
                        }
                    }
                }
        }

        private fun borrarSeriesFavUser(uid: String) {
            database.collection("users/$uid/series")
                .get().addOnCompleteListener { series ->
                    if (series.isSuccessful) {
                        for (serie in series.result) {
                            database.collection("users/$uid/series").document(serie.id).delete()
                        }
                    }
                }
        }

        //AÑADIR USUARIO Y MODIFICAR SUS DATOS
        fun guardarUsuarioEnBbdd(user: User) {
            database.collection("users").document(user.uid!!).set(
                hashMapOf(
                    "email" to user.email,
                    "displayName" to user.username,
                    "password" to user.pass,
                    "status" to user.status
                )
            )
        }

        fun cambiarPassUser(user: User, password: String) {
            val sfDocRef = database.collection("users").document(user.uid!!)
            database.runTransaction { transaction ->
                transaction.update(sfDocRef, "password", password)
            }
        }

        fun cambiarStatusUser(user: String, status: String) {
            val sfDocRef = database.collection("users").document(user)
            database.runTransaction { transaction ->
                transaction.update(sfDocRef, "status", status)
            }
        }

        //FAVORITOS USER
        fun guardarPersonaje(uid: String, personaje: Character) {
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

        fun eliminarPersonaje(uid: String, character: Character) {
            database.collection("users").document(uid)
                .collection("characters")
                .document(character.id.toString()).delete()
        }

        fun guardarComic(uid: String, comic: Comic) {
            //variaciones de portada
            val imagenes = mutableListOf<Thumbnail>()
            for (i in 0 until comic.images!!.size) {
                val image = Thumbnail(comic.images[i].path, comic.images[i].extension)
                imagenes += image
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

        fun eliminarComic(uid: String, comic: Comic) {
            database.collection("users").document(uid)
                .collection("comics")
                .document(comic.id.toString()).delete()
        }

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

        fun eliminarSerie(uid: String, serie: Serie) {
            database.collection("users").document(uid)
                .collection("series")
                .document(serie.id.toString()).delete()
        }

        //COMENTARIOS
        fun guardarComentario(coment: Coment) {
            database.collection("coments").document().set(
                hashMapOf(
                    "type" to coment.type,
                    "id_type" to coment.id_type,
                    "username" to coment.username,
                    "id_userComent" to coment.id_userComent,
                    "score" to coment.puntuacion,
                    "coment" to coment.comentario,
                    "id_coment_resp" to coment.id_coment_resp.toString()
                )
            )
        }

        fun addVotoUser(upVoteOrDownVote: String, idComent: String?) {
            when (upVoteOrDownVote) {
                "upvote" -> {
                    database.collection("users").document(auth.currentUser!!.uid)
                        .collection("comentsVotes").document(idComent.toString()).set(
                            hashMapOf(
                                "vote" to "upvote"
                            )
                        )
                }
                "downvote" -> {
                    database.collection("users").document(auth.currentUser!!.uid)
                        .collection("comentsVotes").document(idComent.toString()).set(
                            hashMapOf(
                                "vote" to "downvote"
                            )
                        )
                }
            }
        }

        fun delVotoUser(idComent: String?) {
            database.collection("users/${auth.currentUser!!.uid}/comentsVotes")
                .document(idComent.toString()).delete()
        }

        fun cambiarPuntuacionComentario(puntNew: Int, coment: Coment) {
            val sfDocRef = database.collection("coments").document(coment.idComent!!)
            database.runTransaction { transaction ->
                val snapshot = transaction.get(sfDocRef)
                val actuScore = snapshot.getLong("score")!! + puntNew
                transaction.update(sfDocRef, "score", actuScore)
            }
        }
    }
}