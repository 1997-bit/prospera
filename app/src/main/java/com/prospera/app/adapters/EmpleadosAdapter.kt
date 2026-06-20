package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.EmpleadoEntity

class EmpleadosAdapter(
    private var items: List<EmpleadoEntity>,
    private val onClick: (EmpleadoEntity) -> Unit
) : RecyclerView.Adapter<EmpleadosAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreEmpleado)
        val tvDetalle: TextView = view.findViewById(R.id.tvDetalleEmpleado)
        val tvIniciales: TextView = view.findViewById(R.id.tvInicialesEmpleado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_empleado, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvDetalle.text = "${item.cargo} · ${item.cedula}"
        holder.tvIniciales.text = item.nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun actualizar(nuevos: List<EmpleadoEntity>) {
        items = nuevos
        notifyDataSetChanged()
    }
}