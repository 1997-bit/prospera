package com.prospera.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferencias")
data class PreferenciasEntity(
    @PrimaryKey val id: Int = 1,
    val modoOscuro: Boolean = false,
    val notificacionesActivas: Boolean = true,
    val nombreUsuario: String = "Administrador",
    val correoUsuario: String = "",
    val cssEmpleado: Double = 0.0975,
    val segEducativo: Double = 0.0125,
    val isrDeduccionCasado: Double = 800.0
)