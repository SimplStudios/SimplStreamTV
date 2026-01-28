package com.simplstudios.simplstream.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.simplstudios.simplstream.domain.model.VideoServerId
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "simplstream_prefs")

@Singleton
class SessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private val KEY_CURRENT_PROFILE_ID = longPreferencesKey("current_profile_id")
        private val KEY_LAST_SEARCH_QUERY = stringPreferencesKey("last_search_query")
        private val KEY_DEFAULT_SERVER = stringPreferencesKey("default_server")
        
        const val NO_PROFILE: Long = -1L
    }
    
    val currentProfileId: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[KEY_CURRENT_PROFILE_ID] ?: NO_PROFILE
    }
    
    val lastSearchQuery: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_SEARCH_QUERY] ?: ""
    }
    
    val defaultServer: Flow<VideoServerId?> = context.dataStore.data.map { prefs ->
        prefs[KEY_DEFAULT_SERVER]?.let { 
            try { 
                VideoServerId.valueOf(it) 
            } catch (e: Exception) { 
                null 
            }
        }
    }
    
    suspend fun getDefaultServerSync(): VideoServerId? {
        return defaultServer.first()
    }
    
    suspend fun setCurrentProfile(profileId: Long) {
        context.dataStore.edit { prefs ->
            prefs[KEY_CURRENT_PROFILE_ID] = profileId
        }
    }
    
    suspend fun clearCurrentProfile() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CURRENT_PROFILE_ID)
        }
    }
    
    suspend fun setLastSearchQuery(query: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_SEARCH_QUERY] = query
        }
    }
    
    suspend fun setDefaultServer(serverId: VideoServerId?) {
        context.dataStore.edit { prefs ->
            if (serverId != null) {
                prefs[KEY_DEFAULT_SERVER] = serverId.name
            } else {
                prefs.remove(KEY_DEFAULT_SERVER)
            }
        }
    }
    
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
