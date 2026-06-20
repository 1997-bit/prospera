package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.ActividadReciente
import com.prospera.app.utils.FechaRelativa

class ActividadAdapter(
    private val items: List<ActividadReciente>
) : RecyclerView.Adapter<ActividadAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDescripcion: TextView = view.findViewById(R.id.tvDescripcion)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_actividad, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvDescripcion.text = item.descripcion
        holder.tvFecha.text = FechaRelativa.formatear(item.timestampMillis)
    }

    override fun getItemCount(): Int = items.size
}