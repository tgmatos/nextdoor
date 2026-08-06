package com.nextdoor.app.data.infra

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore by preferencesDataStore(name = "session")

@Singleton
class TokenStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tokenKey = stringPreferencesKey("jwt")

    val tokenFlow: Flow<String?> = context.sessionDataStore.data.map { it[tokenKey] }

    suspend fun getToken(): String? = context.sessionDataStore.data.first()[tokenKey]

    suspend fun save(token: String) {
        context.sessionDataStore.edit { it[tokenKey] = token }
    }

    suspend fun clear() {
        context.sessionDataStore.edit { it.remove(tokenKey) }
    }
}
