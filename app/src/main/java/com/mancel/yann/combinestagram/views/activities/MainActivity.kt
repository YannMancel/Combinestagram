package com.mancel.yann.combinestagram.views.activities

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import androidx.annotation.LayoutRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.mancel.yann.combinestagram.R
import com.mancel.yann.combinestagram.models.Photo
import com.mancel.yann.combinestagram.utils.ImageTools
import com.mancel.yann.combinestagram.utils.MessageTools
import com.mancel.yann.combinestagram.viewModels.SharedViewModel
import com.mancel.yann.combinestagram.views.fragments.PhotosBottomDialogFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by Yann MANCEL on 11/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram.views.activities
 *
 * A [BaseActivity] subclass.
 */
class MainActivity : BaseActivity() {

    // FIELDS --------------------------------------------------------------------------------------

    private val _viewModel: SharedViewModel by lazy {
        ViewModelProvider(this@MainActivity).get(SharedViewModel::class.java)
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- BaseActivity --

    @LayoutRes
    override fun getActivityLayout(): Int = R.layout.activity_main

    override fun configureDesign() {
        this.configureSelectedPhotos()
        this.configureListenerOfEachButton()
    }

    // -- LiveData --

    private fun configureSelectedPhotos() {
        this._viewModel.getSelectedPhotos()
            .observe(
                this@MainActivity,
                Observer { photos ->
                    photos?.let { this.updateUI(it) }
                }
            )
    }

    // -- Listener --

    private fun configureListenerOfEachButton() {
        this.addButton.setOnClickListener { this.actionAdd() }
        this.clearButton.setOnClickListener { this.actionClear() }
        this.saveButton.setOnClickListener { this.actionSave() }
    }

    // -- Action --

    private fun actionAdd() {
        with(PhotosBottomDialogFragment.newInstance()) {
            show(this@MainActivity.supportFragmentManager, "bottom fragment")

            this@MainActivity._viewModel.subscribeSelectedPhoto(this.selectedPhoto)
        }
    }

    private fun actionClear() = this._viewModel.clearPhotos()

    private fun actionSave() {
        this._viewModel.saveBitmapFromImageView(
                imageView = this.collageImage,
                context = this.applicationContext
            )
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { fileName ->
                    MessageTools.showMessageWithSnackbar(
                        this.coordinator_layout,
                        "$fileName saved"
                    )
                },
                onError = { e ->
                    MessageTools.showMessageWithSnackbar(
                        this.coordinator_layout,
                        "Error saving file :${e.localizedMessage}"
                    )
                }
            )
    }

    // -- UI --

    private fun updateUI(photos: List<Photo>) {
        this.configureVisibilityOfButton(photos)
        this.configureTitleOfActivity(photos)
        this.configureCollage(photos)
        this.configureThumbnail(photos)
    }

    private fun configureVisibilityOfButton(photos: List<Photo>) {
        this.addButton.isEnabled = (photos.size < 6)
        this.clearButton.isEnabled = photos.isNotEmpty()
        this.saveButton.isEnabled = photos.isNotEmpty() && (photos.size % 2 == 0)
    }

    private fun configureTitleOfActivity(photos: List<Photo>) {
        this.title =
            if (photos.isNotEmpty()) {
                this.resources.getQuantityString(
                    R.plurals.photos_format,
                    photos.size,
                    photos.size
                )
            }
            else {
                this.getString(R.string.collage)
            }
    }

    private fun configureCollage(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            val bitmaps = photos.map { photo ->
                BitmapFactory.decodeResource(this.resources, photo.drawable)
            }
            val newBitmap = ImageTools.combineImages(bitmaps)
            this.collageImage.setImageDrawable(BitmapDrawable(this.resources, newBitmap))
        }
        else {
            this.collageImage.setImageResource(android.R.color.transparent)
        }
    }

    private fun configureThumbnail(photos: List<Photo>) {
        if (photos.isNotEmpty()) {
            val bitmap = BitmapFactory.decodeResource(this.resources, photos.last().drawable)
            this.thumbnail.setImageDrawable(BitmapDrawable(this.resources, bitmap))
        }
        else {
            this.thumbnail.setImageResource(android.R.color.transparent)
        }
    }
}