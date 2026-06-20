package com.prospera.app.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "usuario_empresa",
    primaryKeys = ["usuarioId", "empresaId"],
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("empresaId")]
)
data class UsuarioEmpresaEntity(
    val usuarioId: Long,
    val empresaId: Long,
    val rol: String,
    val createdAt: Long = System.currentTimeMillis()
)