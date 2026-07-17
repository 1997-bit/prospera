package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.ConsolidadoMensualRow
import java.util.Locale

class ConsolidadoMensualAdapter(
    private var filas: List<ConsolidadoMensualRow>
) : RecyclerView.Adapter<ConsolidadoMensualAdapter.ViewHolder>() {

    fun actualizarFilas(nuevas: List<ConsolidadoMensualRow>) {
        filas = nuevas
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEmpleado)
        val tvCargo: TextView = view.findViewById(R.id.tvCargoEmpleado)
        val tvBruto: TextView = view.findViewById(R.id.tvBrutoFila)
        val tvDescuentos: TextView = view.findViewById(R.id.tvDescuentosFila)
        val tvNeto: TextView = view.findViewById(R.id.tvNetoFila)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_consolidado_mensual, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fila = filas[position]
        holder.tvNombre.text = fila.nombreEmpleado
        holder.tvCargo.text = fila.cargoEmpleado
        holder.tvBruto.text = String.format(Locale("es", "PA"), "B/. %.2f", fila.brutoMes)
        holder.tvDescuentos.text = String.format(Locale("es", "PA"), "-B/. %.2f", fila.descuentosMes)
        holder.tvNeto.text = String.format(Locale("es", "PA"), "B/. %.2f", fila.netoMes)
    }

    override fun getItemCount(): Int = filas.size
}