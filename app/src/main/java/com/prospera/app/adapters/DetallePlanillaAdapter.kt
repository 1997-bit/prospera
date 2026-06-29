package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.databinding.ItemDetallePlanillaBinding
import com.prospera.app.utils.Moneda

data class FilaPlanilla(
    val detalle: DetallePlanillaEntity,
    val empleado: EmpleadoEntity
)

class DetallePlanillaAdapter(
    private var filas: List<FilaPlanilla>,
    private val onRecalcular: (
        detalleId: Long,
        montoHorasExtrasInput: Double,
        montoComision: Double,
        montoDietas: Double,
        montoPrima: Double,
        otrosDescuentosInput: Double
    ) -> Unit
) : RecyclerView.Adapter<DetallePlanillaAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemDetallePlanillaBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDetallePlanillaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fila = filas[position]
        val b = holder.binding

        b.tvNombreEmpleado.text = fila.empleado.nombre
        b.tvCargo.text = fila.empleado.cargo

        // set sin disparar listeners de focus al re-bind
        b.etHorasExtras.setText(fila.detalle.montoHorasExtrasInput.takeIf { it != 0.0 }?.toString() ?: "")
        b.etComision.setText(fila.detalle.montoComision.takeIf { it != 0.0 }?.toString() ?: "")
        b.etDietas.setText(fila.detalle.montoDietas.takeIf { it != 0.0 }?.toString() ?: "")
        b.etPrima.setText(fila.detalle.montoPrima.takeIf { it != 0.0 }?.toString() ?: "")
        b.etOtrosDescuentos.setText(fila.detalle.otrosDescuentosInput.takeIf { it != 0.0 }?.toString() ?: "")

        b.tvResultado.text = "Bruto: ${Moneda.formatear(fila.detalle.salarioBruto)}  " +
                "Desc: ${Moneda.formatear(fila.detalle.totalDescuentos)}  " +
                "Neto: ${Moneda.formatear(fila.detalle.salarioNeto)}" +
                if (fila.detalle.alertaDescExcede) "Descuentos exceden el límite" else ""

        b.btnRecalcular.setOnClickListener {
            onRecalcular(
                fila.detalle.id,
                b.etHorasExtras.text.toString().toDoubleOrNull() ?: 0.0,
                b.etComision.text.toString().toDoubleOrNull() ?: 0.0,
                b.etDietas.text.toString().toDoubleOrNull() ?: 0.0,
                b.etPrima.text.toString().toDoubleOrNull() ?: 0.0,
                b.etOtrosDescuentos.text.toString().toDoubleOrNull() ?: 0.0
            )
        }
    }

    override fun getItemCount() = filas.size

    fun actualizarFilas(nuevas: List<FilaPlanilla>) {
        filas = nuevas
        notifyDataSetChanged()
    }
}