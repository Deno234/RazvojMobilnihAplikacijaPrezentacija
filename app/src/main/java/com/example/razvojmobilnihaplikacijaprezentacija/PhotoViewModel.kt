package com.example.razvojmobilnihaplikacijaprezentacija // Prilagodite vašem paketu

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class PhotoViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        private const val IMAGE_URIS_KEY = "imageUrisKey"
        private const val SELECTED_FOR_DELETION_KEY = "selectedForDeletionKey"
        private const val IS_IN_DELETE_MODE_KEY = "isInDeleteModeKey"
    }

    // Lista URI-ja slika u galeriji
    val imageUris: SnapshotStateList<Uri> = mutableStateListOf<Uri>().also { list ->
        savedStateHandle.get<List<String>>(IMAGE_URIS_KEY)?.let { savedUriStrings ->
            list.addAll(savedUriStrings.map { Uri.parse(it) })
        }
    }

    // Lista URI-ja slika odabranih za brisanje
    val selectedImagesForDeletion: SnapshotStateList<Uri> = mutableStateListOf<Uri>().also { list ->
        savedStateHandle.get<List<String>>(SELECTED_FOR_DELETION_KEY)?.let { savedSelectedUris ->
            list.addAll(savedSelectedUris.map { Uri.parse(it) })
        }
    }

    // Zastavica za mod brisanja
    var isInDeleteMode: Boolean
        get() = savedStateHandle.get<Boolean>(IS_IN_DELETE_MODE_KEY) ?: false
        set(value) {
            savedStateHandle[IS_IN_DELETE_MODE_KEY] = value
        }

    fun addUris(newUris: List<Uri>) {
        val urisToAdd = newUris.filter { uri -> !imageUris.contains(uri) }
        if (urisToAdd.isNotEmpty()) {
            imageUris.addAll(urisToAdd)
            saveImageUrisToSavedState()
        }
    }

    fun removeSelectedUris() {
        if (imageUris.removeAll(selectedImagesForDeletion.toSet())) {
            saveImageUrisToSavedState()
        }
        selectedImagesForDeletion.clear()
        saveSelectedForDeletionToSavedState() // Spremi praznu listu
        isInDeleteMode = false // Automatski izađi iz moda brisanja
    }

    fun clearAllUris() {
        imageUris.clear()
        selectedImagesForDeletion.clear()
        saveImageUrisToSavedState()
        saveSelectedForDeletionToSavedState()
        isInDeleteMode = false
    }

    fun enterDeleteMode(imageUri: Uri) {
        isInDeleteMode = true
        if (!selectedImagesForDeletion.contains(imageUri)) {
            selectedImagesForDeletion.add(imageUri)
            saveSelectedForDeletionToSavedState()
        }
    }

    fun exitDeleteModeAndClearSelection() {
        isInDeleteMode = false
        selectedImagesForDeletion.clear()
        saveSelectedForDeletionToSavedState()
    }

    fun toggleImageSelectionForDeletion(imageUri: Uri) {
        if (selectedImagesForDeletion.contains(imageUri)) {
            selectedImagesForDeletion.remove(imageUri)
            if (selectedImagesForDeletion.isEmpty()) {
                // Opcionalno: Ako je lista prazna, automatski izađi iz delete moda
                // isInDeleteMode = false
            }
        } else {
            selectedImagesForDeletion.add(imageUri)
        }
        saveSelectedForDeletionToSavedState()
    }

    private fun saveImageUrisToSavedState() {
        savedStateHandle[IMAGE_URIS_KEY] = imageUris.map { it.toString() }
    }

    private fun saveSelectedForDeletionToSavedState() {
        savedStateHandle[SELECTED_FOR_DELETION_KEY] = selectedImagesForDeletion.map { it.toString() }
    }
}
