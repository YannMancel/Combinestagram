package com.mancel.yann.combinestagram.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mancel.yann.combinestagram.R
import com.mancel.yann.combinestagram.models.Photo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_photo.view.*
import java.lang.ref.WeakReference

/**
 * Created by Yann MANCEL on 11/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram.views.adapters
 *
 * A [RecyclerView.Adapter] subclass.
 */
class PhotoAdapter(
    private val _callback: AdapterCallback? = null
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    // FIELDS --------------------------------------------------------------------------------------

    private val _photos = mutableListOf<Photo>()

    // METHODS -------------------------------------------------------------------------------------

    // -- RecyclerView.Adapter --

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        // Creates the View thanks to the inflater
        val view = LayoutInflater.from(parent.context)
                                 .inflate(R.layout.item_photo, parent, false)

        return PhotoViewHolder(view, WeakReference(this._callback))
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(this._photos[position])
    }

    override fun getItemCount(): Int = this._photos.size

    // -- Photo --

    /**
     * Updates data of [PhotoAdapter]
     * @param newPhotos a [List] of [Photo]
     */
    fun updateData(newPhotos: List<Photo>) {
        // New data
        with(this._photos) {
            clear()
            addAll(newPhotos)
        }

        this.notifyDataSetChanged()
    }

    // NESTED CLASSES ------------------------------------------------------------------------------

    /**
     * A [RecyclerView.ViewHolder] subclass.
     */
    class PhotoViewHolder(
        itemView: View,
        private val _weakRef: WeakReference<AdapterCallback?>
    ) : RecyclerView.ViewHolder(itemView) {

        fun bind(photo: Photo) {
            // Listener
            itemView.setOnClickListener {
                this._weakRef.get()?.photoClicked(photo)
            }

            // Image
            Picasso.get()
                   .load(photo.drawable)
                   .placeholder(R.drawable.ic_image)
                   .into(itemView.photo)
        }
    }
}