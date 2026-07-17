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
        horasExtraDiurnas: Double,
        horasExtraNocturnas: Double,
        montoComision: Double,
        montoDietas: Double,
        descAdelanto: Double,
        descAhorro: Double
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
        b.tvInicialesEmpleado.text = fila.empleado.nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        // set sin disparar listeners de focus al re-bind
        b.etHorasDiurnas.setText(fila.detalle.horasExtraDiurnas.takeIf { it != 0.0 }?.toString() ?: "")
        b.etHorasNocturnas.setText(fila.detalle.horasExtraNocturnas.takeIf { it != 0.0 }?.toString() ?: "")
        b.etComision.setText(fila.detalle.montoComision.takeIf { it != 0.0 }?.toString() ?: "")
        b.etDietas.setText(fila.detalle.montoDietas.takeIf { it != 0.0 }?.toString() ?: "")
        b.etDescAdelanto.setText(fila.detalle.descAdelanto.takeIf { it != 0.0 }?.toString() ?: "")
        b.etDescAhorro.setText(fila.detalle.descAhorro.takeIf { it != 0.0 }?.toString() ?: "")

        b.tvBruto.text = Moneda.formatear(fila.detalle.salarioBruto)
        b.tvDescuentos.text = "-${Moneda.formatear(fila.detalle.totalDescuentos)}"
        b.tvNeto.text = Moneda.formatear(fila.detalle.salarioNeto)
        b.tvAlerta.visibility = if (fila.detalle.alertaDescExcede) View.VISIBLE else View.GONE

        b.btnRecalcular.setOnClickListener {
            onRecalcular(
                fila.detalle.id,
                b.etHorasDiurnas.text.toString().toDoubleOrNull() ?: 0.0,
                b.etHorasNocturnas.text.toString().toDoubleOrNull() ?: 0.0,
                b.etComision.text.toString().toDoubleOrNull() ?: 0.0,
                b.etDietas.text.toString().toDoubleOrNull() ?: 0.0,
                b.etDescAdelanto.text.toString().toDoubleOrNull() ?: 0.0,
                b.etDescAhorro.text.toString().toDoubleOrNull() ?: 0.0
            )
        }
    }

    override fun getItemCount() = filas.size

    fun actualizarFilas(nuevas: List<FilaPlanilla>) {
        filas = nuevas
        notifyDataSetChanged()
    }
}