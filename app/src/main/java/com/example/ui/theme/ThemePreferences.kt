package com.example.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_prefs")

object ThemePreferences {
    private val THEME_KEY = intPreferencesKey("selected_theme")
    
    // 1: Light, 2: Dark
    private val _themeMode = MutableStateFlow(1)
    val themeMode: StateFlow<Int> = _themeMode
    
    private val scope = CoroutineScope(Dispatchers.Main)

    fun init(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.data.map { preferences ->
                preferences[THEME_KEY] ?: 1
            }.collect { mode ->
                _themeMode.value = mode
            }
        }
    }
    
    fun setTheme(context: Context, mode: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            context.dataStore.edit { preferences ->
                preferences[THEME_KEY] = mode
            }
        }
    }
    
    fun resetTheme(context: Context) {
        setTheme(context, 1)
    }
}
