package com.example.razvojmobilnihaplikacijaprezentacija

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension na Context
val Context.photoDataStore by preferencesDataStore(name = "photo_prefs")

object PhotoPreferenceKeys {
    val PHOTO_URIS = stringSetPreferencesKey("photo_uris")
}

suspend fun savePhotoUris(context: Context, uris: List<String>) {
    context.photoDataStore.edit { prefs ->
        prefs[PhotoPreferenceKeys.PHOTO_URIS] = uris.toSet()
    }
}

fun getPhotoUris(context: Context): Flow<List<String>> {
    return context.photoDataStore.data.map { prefs ->
        prefs[PhotoPreferenceKeys.PHOTO_URIS]?.toList() ?: emptyList()
    }
}
