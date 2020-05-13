package com.mancel.yann.combinestagram.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mancel.yann.combinestagram.R
import com.mancel.yann.combinestagram.models.Photo
import com.mancel.yann.combinestagram.utils.PhotoStore
import com.mancel.yann.combinestagram.views.adapters.AdapterCallback
import com.mancel.yann.combinestagram.views.adapters.PhotoAdapter
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.layout_photo_bottom_sheet.*

/**
 * Created by Yann MANCEL on 11/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram.views.fragments
 *
 * A [BottomSheetDialogFragment] subclass which implements [AdapterCallback].
 */
class PhotosBottomDialogFragment : BottomSheetDialogFragment(), AdapterCallback {

    // FIELDS --------------------------------------------------------------------------------------

    private lateinit var _adapter: PhotoAdapter

    private val _selectedPhotoSubject = PublishSubject.create<Photo>()
    val selectedPhoto: Observable<Photo>
        get() = _selectedPhotoSubject.hide()

    // METHODS -------------------------------------------------------------------------------------

    companion object {
        fun newInstance() = PhotosBottomDialogFragment()
    }

    // -- BottomSheetDialogFragment --

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.layout_photo_bottom_sheet, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.configureRecyclerView()
    }

    override fun onDestroyView() {
        this._selectedPhotoSubject.onComplete()
        super.onDestroyView()
    }

    // -- AdapterCallback interface --

    override fun photoClicked(photo: Photo) = this._selectedPhotoSubject.onNext(photo)

    // -- RecyclerView --

    private fun configureRecyclerView() {
        // Adapter
        this._adapter = PhotoAdapter(_callback = this@PhotosBottomDialogFragment).apply {
            updateData(PhotoStore.photos)
        }

        // RecyclerView
        with(this.photosRecyclerView) {
            layoutManager = GridLayoutManager(
                this@PhotosBottomDialogFragment.requireContext(),
                3
            )
            adapter = this@PhotosBottomDialogFragment._adapter
        }
    }
}