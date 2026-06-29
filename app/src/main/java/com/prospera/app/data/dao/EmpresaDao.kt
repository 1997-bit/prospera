package com.prospera.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.prospera.app.data.entities.EmpresaEntity

@Dao
interface EmpresaDao {

    @Insert
    suspend fun insertar(empresa: EmpresaEntity): Long

    @Query("SELECT * FROM empresas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): EmpresaEntity?
}