package com.prospera.app.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "detalle_planilla",
    foreignKeys = [
        ForeignKey(
            entity = PlanillaEntity::class,
            parentColumns = ["id"],
            childColumns = ["planillaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = EmpleadoEntity::class,
            parentColumns = ["id"],
            childColumns = ["empleadoId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["planillaId", "empleadoId"], unique = true),
        Index("empleadoId")
    ]
)
data class DetallePlanillaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planillaId: Long,
    val empleadoId: Long,

    // --- Inputs editables (capturados manualmente por quincena) ---
    val montoHorasExtrasInput: Double = 0.0,   // monto ya calculado, captura manual directa
    val montoComision: Double = 0.0,
    val montoDietas: Double = 0.0,
    val montoPrima: Double = 0.0,
    val otrosDescuentosInput: Double = 0.0,

    // --- Resultados (salida de CalculadoraPlanilla.calcularQuincena) ---
    val salarioBaseQuincena: Double = 0.0,
    val valorHora: Double = 0.0,
    val otrosIngresosGravables: Double = 0.0,
    val otrosIngresosSinDescuento: Double = 0.0,
    val salarioBruto: Double = 0.0,
    val descSeguroSocial: Double = 0.0,
    val descSeguroEducativo: Double = 0.0,
    val descISR: Double = 0.0,
    val otrosDescuentos: Double = 0.0,
    val totalDescuentos: Double = 0.0,
    val salarioNeto: Double = 0.0,
    val alertaDescExcede: Boolean = false
)