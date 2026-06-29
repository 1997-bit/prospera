package com.prospera.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "empleados",
    foreignKeys = [
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("empresaId"), Index("cedula")]
)
data class EmpleadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val nombre: String,
    val cedula: String,
    val cargo: String,
    val departamento: String,
    val estadoCivil: String,       // "soltero" | "casado" | "unido"
    val salarioBase: Double,
    val anioInicio: Int,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)