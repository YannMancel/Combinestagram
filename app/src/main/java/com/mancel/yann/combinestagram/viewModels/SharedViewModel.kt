package com.mancel.yann.combinestagram.viewModels

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mancel.yann.combinestagram.models.Photo
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

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

    // -- Photo --

    private fun addPhoto(photo: Photo) {
        with(this._imagesSubject) {
            value?.add(photo)
            onNext(value!!)
        }
    }

    fun clearPhotos() {
        with(this._imagesSubject) {
            value?.clear()
            onNext(value!!)
        }
    }

    // -- Single --

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

    fun subscribeSelectedPhoto(selectedPhoto: Observable<Photo>) {
        selectedPhoto
            .doOnComplete { Timber.d("Completed selecting photos") }
            .subscribe {
                this.addPhoto(it)
            }
            .addTo(this._disposables)
    }
}