package com.prospera.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.prospera.app.R
import com.prospera.app.data.UsuarioConRol

class UsuariosAdapter(private val items: List<UsuarioConRol>) :
    RecyclerView.Adapter<UsuariosAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuario)
        val tvEmail: TextView = view.findViewById(R.id.tvEmailUsuario)
        val tvRol: TextView = view.findViewById(R.id.tvRolUsuario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario_empresa, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.tvNombre.text = item.nombre
        holder.tvEmail.text = item.email
        holder.tvRol.text = item.rol.replaceFirstChar { it.uppercase() }
    }

    override fun getItemCount() = items.size
}