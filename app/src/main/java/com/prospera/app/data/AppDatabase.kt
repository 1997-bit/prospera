package com.prospera.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PreferenciasEntity::class,
        UsuarioEntity::class,
        EmpresaEntity::class,
        UsuarioEmpresaEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun preferenciasDao(): PreferenciasDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun usuarioEmpresaDao(): UsuarioEmpresaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prospera_db"
                )
                    .fallbackToDestructiveMigration() // dev only: borra y recrea si cambia el schema
                    .build().also { INSTANCE = it }
            }
    }
}