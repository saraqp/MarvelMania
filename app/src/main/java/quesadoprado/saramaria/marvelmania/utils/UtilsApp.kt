package quesadoprado.saramaria.marvelmania.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import quesadoprado.saramaria.marvelmania.utils.FirebaseUtils.firebaseStorage

object UtilsApp {
    private val storage = firebaseStorage
    fun mostrarImagenUser(
        urlFile: String?,
        imageView: ImageView?,
        imageND: ImageView,
        context: Context
    ) {
        storage.child("file/$urlFile").downloadUrl.addOnSuccessListener {
            if (imageView != null) {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().override(512, 512))
                    .circleCrop()
                    .into(imageView)
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().override(512, 512))
                    .circleCrop()
                    .into(imageND)
            } else {
                Glide.with(context)
                    .load(it)
                    .apply(RequestOptions().override(512, 512))
                    .circleCrop()
                    .into(imageND)
            }
        }
    }
}