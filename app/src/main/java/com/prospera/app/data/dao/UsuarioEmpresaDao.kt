package com.prospera.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.prospera.app.data.UsuarioConRol
import com.prospera.app.data.entities.EmpresaEntity
import com.prospera.app.data.entities.UsuarioEmpresaEntity

@Dao
interface UsuarioEmpresaDao {

    @Insert
    suspend fun vincular(rel: UsuarioEmpresaEntity)

    @Query("""
        SELECT e.* FROM empresas e
        INNER JOIN usuario_empresa ue ON ue.empresaId = e.id
        WHERE ue.usuarioId = :usuarioId
    """)
    suspend fun empresasDeUsuario(usuarioId: Long): List<EmpresaEntity>

    @Query("""
        SELECT rol FROM usuario_empresa
        WHERE usuarioId = :usuarioId AND empresaId = :empresaId
        LIMIT 1
    """)
    suspend fun rolDe(usuarioId: Long, empresaId: Long): String?

    @Query("""
        SELECT u.id as id, u.nombre as nombre, u.email as email, ue.rol as rol
        FROM usuarios u
        INNER JOIN usuario_empresa ue ON ue.usuarioId = u.id
        WHERE ue.empresaId = :empresaId
    """)
    suspend fun usuariosDeEmpresa(empresaId: Long): List<UsuarioConRol>
}