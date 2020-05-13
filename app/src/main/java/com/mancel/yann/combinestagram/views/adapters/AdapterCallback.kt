package com.mancel.yann.combinestagram.views.adapters

import com.mancel.yann.combinestagram.models.Photo

/**
 * Created by Yann MANCEL on 11/05/2020.
 * Name of the project: Combinestagram
 * Name of the package: com.mancel.yann.combinestagram.views.adapters
 */
interface AdapterCallback {

    // METHODS -------------------------------------------------------------------------------------

    /**
     * Called when a view has been clicked.
     */
    fun photoClicked(photo: Photo)
}