package com.prospera.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "planillas",
    foreignKeys = [
        ForeignKey(
            entity = EmpresaEntity::class,
            parentColumns = ["id"],
            childColumns = ["empresaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["empresaId", "periodo", "mes", "anio"], unique = true),
        Index("estado")
    ]
)
data class PlanillaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empresaId: Long,
    val periodo: String, // "1ra_quincena" | "2da_quincena"
    val mes: Int,                 // 1-12
    val anio: Int,
    val estado: String = "borrador",  // "borrador" | "pagada"
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaPago: Long? = null
)