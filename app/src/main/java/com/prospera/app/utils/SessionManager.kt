package com.prospera.app.utils

import android.content.Context

object SessionManager {
    private const val PREFS = "prospera_session"
    private const val KEY_USUARIO_ID = "usuario_id"
    private const val KEY_EMPRESA_ID = "empresa_id"

    fun login(context: Context, usuarioId: Long, empresaId: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong(KEY_USUARIO_ID, usuarioId)
            .putLong(KEY_EMPRESA_ID, empresaId)
            .apply()
    }

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_USUARIO_ID, -1L) != -1L

    fun getUsuarioId(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_USUARIO_ID, -1L)

    fun getEmpresaId(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getLong(KEY_EMPRESA_ID, -1L)

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }
}