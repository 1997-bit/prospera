package com.prospera.app.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.prospera.app.data.entities.PreferenciasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciasDao {

    @Query("SELECT * FROM preferencias WHERE id = 1 LIMIT 1")
    fun observar(): Flow<PreferenciasEntity?>

    @Query("SELECT * FROM preferencias WHERE id = 1 LIMIT 1")
    suspend fun obtener(): PreferenciasEntity?

    @Upsert
    suspend fun guardar(prefs: PreferenciasEntity)
}