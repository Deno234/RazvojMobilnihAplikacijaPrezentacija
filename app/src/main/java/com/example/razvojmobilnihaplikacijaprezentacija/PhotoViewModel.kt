package com.example.razvojmobilnihaplikacijaprezentacija

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.core.net.toUri

class PhotoViewModel(application: Application) : AndroidViewModel(application) {

    val imageUris: SnapshotStateList<Uri> = mutableStateListOf()
    val selectedImagesForDeletion: SnapshotStateList<Uri> = mutableStateListOf()

    var isInDeleteMode by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch { // korutina -> asinkrono izvršavanje
            getPhotoUris(application).collectLatest { uriStrings ->
                imageUris.clear()
                imageUris.addAll(uriStrings.map { it.toUri() })
            }
        }
    }

    private fun persistImages() {
        viewModelScope.launch {
            savePhotoUris(getApplication(), imageUris.map { it.toString() })
        }
    }

    private fun setDeleteMode(enabled: Boolean) {
        isInDeleteMode = enabled
    }

    fun addUris(newUris: List<Uri>) {
        val urisToAdd = newUris.filter { uri -> !imageUris.contains(uri) }
        if (urisToAdd.isNotEmpty()) {
            imageUris.addAll(urisToAdd)
            persistImages()
        }
    }

    fun removeSelectedUris() {
        if (imageUris.removeAll(selectedImagesForDeletion.toSet())) {
            persistImages()
        }
        selectedImagesForDeletion.clear()
        setDeleteMode(false)
    }

    fun enterDeleteMode(imageUri: Uri) {
        setDeleteMode(true)
        if (!selectedImagesForDeletion.contains(imageUri)) {
            selectedImagesForDeletion.add(imageUri)
        }
    }

    fun exitDeleteModeAndClearSelection() {
        setDeleteMode(false)
        selectedImagesForDeletion.clear()
    }

    fun toggleImageSelectionForDeletion(imageUri: Uri) {
        if (selectedImagesForDeletion.contains(imageUri)) {
            selectedImagesForDeletion.remove(imageUri)
        } else {
            selectedImagesForDeletion.add(imageUri)
        }
    }
}

