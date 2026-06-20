package com.prospera.app.data

import android.content.Context
import org.mindrot.jbcrypt.BCrypt

class AuthRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    val usuarioDao = db.usuarioDao()
    private val empresaDao = db.empresaDao()
    private val usuarioEmpresaDao = db.usuarioEmpresaDao()

    /** Usado en RegistroActivity: primer registro, crea cuenta + empresa, admin automático */
    suspend fun registrarPrimeraEmpresa(
        nombre: String,
        email: String,
        passwordPlano: String,
        nombreEmpresa: String
    ): Long {
        val passwordHash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt())
        val usuarioId = usuarioDao.insertar(
            UsuarioEntity(nombre = nombre, email = email, passwordHash = passwordHash)
        )
        val empresaId = empresaDao.insertar(EmpresaEntity(nombre = nombreEmpresa))
        usuarioEmpresaDao.vincular(
            UsuarioEmpresaEntity(usuarioId = usuarioId, empresaId = empresaId, rol = "admin")
        )
        return empresaId
    }

    /** Usado en LoginActivity */
    suspend fun login(email: String, passwordPlano: String): UsuarioEntity? {
        val usuario = usuarioDao.buscarPorEmail(email) ?: return null
        return if (BCrypt.checkpw(passwordPlano, usuario.passwordHash)) usuario else null
    }

    /** Usado en GestionarUsuariosActivity: admin crea cuenta+contraseña directo */
    suspend fun crearUsuarioParaEmpresa(
        empresaId: Long,
        nombre: String,
        email: String,
        passwordPlano: String,
        rol: String
    ): Long {
        val passwordHash = BCrypt.hashpw(passwordPlano, BCrypt.gensalt())
        val usuarioId = usuarioDao.insertar(
            UsuarioEntity(nombre = nombre, email = email, passwordHash = passwordHash)
        )
        usuarioEmpresaDao.vincular(
            UsuarioEmpresaEntity(usuarioId = usuarioId, empresaId = empresaId, rol = rol)
        )
        return usuarioId
    }

    suspend fun empresasDeUsuario(usuarioId: Long) = usuarioEmpresaDao.empresasDeUsuario(usuarioId)
    suspend fun rolDe(usuarioId: Long, empresaId: Long) = usuarioEmpresaDao.rolDe(usuarioId, empresaId)
    suspend fun usuariosDeEmpresa(empresaId: Long) = usuarioEmpresaDao.usuariosDeEmpresa(empresaId)
}