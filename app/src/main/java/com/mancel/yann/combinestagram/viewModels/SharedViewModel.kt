package com.mancel.yann.combinestagram.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mancel.yann.combinestagram.models.Photo
import com.mancel.yann.combinestagram.views.fragments.PhotosBottomDialogFragment
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.TimeUnit

/**
 * Created by Yann MANCEL on 11/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram.viewModels
 *
 * A [ViewModel] subclass.
 */
class SharedViewModel : ViewModel() {

    // FIELDS --------------------------------------------------------------------------------------

    private val _disposables = CompositeDisposable()
    private val _imagesSubject = BehaviorSubject.createDefault(mutableListOf<Photo>())
    private val _selectedPhotos = MutableLiveData<List<Photo>>()

    enum class ThumbnailStatus {READY, ERROR}
    private val _thumbnailStatus = MutableLiveData<ThumbnailStatus>()

    enum class CollageStatus {NOT_FULL, FULL_FULLED}
    private val _collageStatus = MutableLiveData<CollageStatus>()

    companion object {
        const val MAX_PHOTOS = 9
        const val TIMEOUT_PHOTO = 250L
    }

    // CONSTRUCTORS --------------------------------------------------------------------------------

    init {
        this._imagesSubject.subscribe {
            this._selectedPhotos.value = it
        }.addTo(this._disposables)
    }

    // METHODS -------------------------------------------------------------------------------------

    // -- ViewModel --

    override fun onCleared() {
        this._disposables.dispose()
        super.onCleared()
    }

    // -- LiveData

    fun getSelectedPhotos(): LiveData<List<Photo>> = this._selectedPhotos

    fun getThumbnailStatus(): LiveData<ThumbnailStatus> = this._thumbnailStatus

    fun getCollageStatus(): LiveData<CollageStatus> = this._collageStatus

    // -- Photo --

    private fun addPhoto(photo: Photo) {
        with(this._imagesSubject) {
            value?.add(photo)
            onNext(value ?: mutableListOf())
        }
    }

    fun clearPhotos() {
        with(this._imagesSubject) {
            value?.clear()
            onNext(value ?: mutableListOf())
        }
    }

    // -- Single --

    /**
     * Creates a [Single] that saves the content of an [ImageView] in argument
     * into the external storage
     */
    fun saveBitmapFromImageView(
        imageView: ImageView,
        context: Context
    ) : Single<String> {
        return Single.create { emitter ->
            val tmpImg = "${System.currentTimeMillis()}.png"

            val os: OutputStream?

            val collagesDirectory = File(context.getExternalFilesDir(null), "collages")
            if (!collagesDirectory.exists()) {
                collagesDirectory.mkdirs()
            }

            val file = File(collagesDirectory, tmpImg)

            try {
                os = FileOutputStream(file)
                val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
                os.flush()
                os.close()

                emitter.onSuccess(tmpImg)
            } catch(e: IOException) {
                Timber.e(e, "Problem saving collage")
                emitter.onError(e)
            }
        }
    }

    // -- Observable --

    /**
     * Subscribes to a shared Observable
     */
    fun subscribeSelectedPhoto(fragment: PhotosBottomDialogFragment) {
        val sharedObservable = fragment.selectedPhoto.share()

        sharedObservable
            .doOnComplete { Timber.d("Completed selecting photos") }
            .takeWhile { this._imagesSubject.value?.size ?: 0 < MAX_PHOTOS }
            .filter { newPhoto ->
                val bitmap = BitmapFactory.decodeResource(fragment.resources, newPhoto.drawable)
                bitmap.width > bitmap.height
            }
            .filter { newPhoto ->
                val photos = this._imagesSubject.value ?: mutableListOf()
                !photos.any { it.drawable == newPhoto.drawable }
            }
            .debounce(TIMEOUT_PHOTO, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe { newPhoto ->
                this.addPhoto(newPhoto)

                this._collageStatus.postValue(
                    if (this._imagesSubject.value?.size ?: 0 == MAX_PHOTOS)
                        CollageStatus.FULL_FULLED
                    else
                        CollageStatus.NOT_FULL
                )
            }
            .addTo(this._disposables)

        sharedObservable
            .ignoreElements()
            .subscribe {
                this._thumbnailStatus.postValue(ThumbnailStatus.READY)
            }
            .addTo(this._disposables)
    }
}