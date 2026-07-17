package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.entities.EmpleadoEntity

class ColaboradorSeleccionableAdapter(
    private var colaboradores: List<EmpleadoEntity>,
    private val onClick: (EmpleadoEntity) -> Unit
) : RecyclerView.Adapter<ColaboradorSeleccionableAdapter.ViewHolder>() {

    fun actualizarLista(nueva: List<EmpleadoEntity>) {
        colaboradores = nueva
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombre)
        val tvCargoCedula: TextView = view.findViewById(R.id.tvCargoCedula)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_colaborador_seleccionable, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val c = colaboradores[position]
        holder.tvNombre.text = c.nombre
        holder.tvCargoCedula.text = "${c.cargo} · Cédula: ${c.cedula}"
        holder.itemView.setOnClickListener { onClick(c) }
    }

    override fun getItemCount(): Int = colaboradores.size
}