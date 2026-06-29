package com.prospera.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empresas")
data class EmpresaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val ruc: String? = null,
    val region: Int = 1,
    val horasSemanales: Double = 48.0,
    val semanasMes: Double = 4.3333,
    val claseRiesgo: Int = 1,
    val gradoRiesgo: Int = 8,
    val activo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)