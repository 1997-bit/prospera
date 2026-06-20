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
        UsuarioEmpresaEntity::class,
        EmpleadoEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun preferenciasDao(): PreferenciasDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun usuarioEmpresaDao(): UsuarioEmpresaDao
    abstract fun empleadoDao(): EmpleadoDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prospera_db"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }
}