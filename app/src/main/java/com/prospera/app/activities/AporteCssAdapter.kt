package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.AporteCssRow
import java.util.Locale

class AporteCssAdapter(
    private var filas: List<AporteCssRow>
) : RecyclerView.Adapter<AporteCssAdapter.ViewHolder>() {

    fun actualizarFilas(nuevas: List<AporteCssRow>) {
        filas = nuevas
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEmpleado)
        val tvCedula: TextView = view.findViewById(R.id.tvCedula)
        val tvSeguroSocial: TextView = view.findViewById(R.id.tvSeguroSocial)
        val tvSeguroEducativo: TextView = view.findViewById(R.id.tvSeguroEducativo)
        val tvTotal: TextView = view.findViewById(R.id.tvTotalFila)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_aporte_css, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fila = filas[position]
        holder.tvNombre.text = fila.nombreEmpleado
        holder.tvCedula.text = "Cédula: ${fila.cedula}"
        holder.tvSeguroSocial.text = String.format(Locale("es", "PA"), "SS: B/. %.2f", fila.seguroSocialMes)
        holder.tvSeguroEducativo.text = String.format(Locale("es", "PA"), "SE: B/. %.2f", fila.seguroEducativoMes)
        holder.tvTotal.text = String.format(Locale("es", "PA"), "B/. %.2f", fila.totalCss)
    }

    override fun getItemCount(): Int = filas.size
}