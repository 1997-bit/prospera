package com.prospera.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EmpresaDao {

    @Insert
    suspend fun insertar(empresa: EmpresaEntity): Long

    @Query("SELECT * FROM empresas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): EmpresaEntity?
}