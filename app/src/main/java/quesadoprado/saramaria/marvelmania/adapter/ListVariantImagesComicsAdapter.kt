package quesadoprado.saramaria.marvelmania.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import quesadoprado.saramaria.marvelmania.R
import quesadoprado.saramaria.marvelmania.data.items.Thumbnail

class ListVariantImagesComicsAdapter(private val list_images: Array<Thumbnail>?) :
    RecyclerView.Adapter<ListVariantImagesComicsAdapter.ViewHolder>() {
    private var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_show_images, parent, false)
        context = parent.context
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageHolder: Thumbnail = list_images?.get(position)!!
        //Url de la imagen que se va a mostrar
        val imageUrl = "${imageHolder.path}/portrait_uncanny.${imageHolder.extension}"

        Glide.with(context!!).load(imageUrl).apply(RequestOptions().override(300, 450))
            .into(holder.image)
    }

    override fun getItemCount() = list_images?.size!!

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.IV_imagen)

    }
}