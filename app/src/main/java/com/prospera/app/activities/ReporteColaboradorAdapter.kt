package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.entities.EmpleadoEntity
import java.util.Locale

class ReporteColaboradorAdapter(
    private var colaboradores: List<EmpleadoEntity>
) : RecyclerView.Adapter<ReporteColaboradorAdapter.ViewHolder>() {

    fun actualizarLista(nueva: List<EmpleadoEntity>) {
        colaboradores = nueva
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvEstado: TextView = view.findViewById(R.id.tvEstado)
        val tvCargoDepartamento: TextView = view.findViewById(R.id.tvCargoDepartamento)
        val tvCedula: TextView = view.findViewById(R.id.tvCedula)
        val tvSalario: TextView = view.findViewById(R.id.tvSalario)
        val tvAntiguedad: TextView = view.findViewById(R.id.tvAntiguedad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reporte_colaborador, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = colaboradores[position]
        val context = holder.itemView.context

        holder.tvNombre.text = c.nombre
        holder.tvCargoDepartamento.text = "${c.cargo} · ${c.departamento}"
        holder.tvCedula.text = "Cédula: ${c.cedula}"
        holder.tvSalario.text = String.format(Locale("es", "PA"), "Salario: B/. %.2f", c.salarioBase)
        holder.tvAntiguedad.text = "Desde ${c.anioInicio} · ${c.estadoCivil.replaceFirstChar { it.uppercase() }}"

        if (c.activo) {
            holder.tvEstado.text = "ACTIVO"
            holder.tvEstado.setBackgroundResource(R.drawable.bg_chip_activo)
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.md_on_primary_container))
        } else {
            holder.tvEstado.text = "INACTIVO"
            holder.tvEstado.setBackgroundResource(R.drawable.bg_chip_inactivo)
            holder.tvEstado.setTextColor(ContextCompat.getColor(context, R.color.md_on_surface_variant))
        }
    }

    override fun getItemCount(): Int = colaboradores.size
}