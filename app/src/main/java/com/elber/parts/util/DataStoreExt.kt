package com.elber.parts.util

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore("prefs")
private val SEEN_ONBOARD = booleanPreferencesKey("seen_onboard")

suspend fun setSeenOnboard(context: Context, value: Boolean = true) {
    context.dataStore.edit { it[SEEN_ONBOARD] = value }
}
suspend fun hasSeenOnboard(context: Context): Boolean {
    val prefs = context.dataStore.data.first()
    return prefs[SEEN_ONBOARD] ?: false
}
