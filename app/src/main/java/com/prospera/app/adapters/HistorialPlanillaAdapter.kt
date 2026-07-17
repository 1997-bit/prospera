package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.HistorialPlanillaRow
import java.util.Locale

class HistorialPlanillaAdapter(
    private var filas: List<HistorialPlanillaRow>
) : RecyclerView.Adapter<HistorialPlanillaAdapter.ViewHolder>() {

    fun actualizarFilas(nuevas: List<HistorialPlanillaRow>) {
        filas = nuevas
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPeriodo: TextView = view.findViewById(R.id.tvPeriodo)
        val tvNeto: TextView = view.findViewById(R.id.tvNetoPagado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_planilla, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fila = filas[position]
        val etiquetaPeriodo = if (fila.periodo == "1ra_quincena") "1ra quincena" else "2da quincena"
        holder.tvPeriodo.text = "$etiquetaPeriodo · ${fila.mes}/${fila.anio}"
        holder.tvNeto.text = String.format(Locale("es", "PA"), "B/. %.2f", fila.salarioNeto)
    }

    override fun getItemCount(): Int = filas.size
}