package com.prospera.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.prospera.app.data.dao.EmpleadoDao
import com.prospera.app.data.dao.EmpresaDao
import com.prospera.app.data.dao.PlanillaDao
import com.prospera.app.data.dao.PreferenciasDao
import com.prospera.app.data.dao.UsuarioDao
import com.prospera.app.data.dao.UsuarioEmpresaDao
import com.prospera.app.data.entities.DetallePlanillaEntity
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.data.entities.EmpresaEntity
import com.prospera.app.data.entities.PlanillaEntity
import com.prospera.app.data.entities.PreferenciasEntity
import com.prospera.app.data.entities.UsuarioEmpresaEntity
import com.prospera.app.data.entities.UsuarioEntity

@Database(
    entities = [
        PreferenciasEntity::class,
        UsuarioEntity::class,
        EmpresaEntity::class,
        UsuarioEmpresaEntity::class,
        EmpleadoEntity::class,
        PlanillaEntity::class,
        DetallePlanillaEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun preferenciasDao(): PreferenciasDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun empresaDao(): EmpresaDao
    abstract fun usuarioEmpresaDao(): UsuarioEmpresaDao
    abstract fun empleadoDao(): EmpleadoDao
    abstract fun planillaDao(): PlanillaDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS planillas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        empresaId INTEGER NOT NULL,
                        periodo TEXT NOT NULL,
                        mes INTEGER NOT NULL,
                        anio INTEGER NOT NULL,
                        estado TEXT NOT NULL,
                        fechaCreacion INTEGER NOT NULL,
                        fechaPago INTEGER,
                        FOREIGN KEY(empresaId) REFERENCES empresas(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_planillas_empresaId_periodo_mes_anio ON planillas(empresaId, periodo, mes, anio)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_planillas_estado ON planillas(estado)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS detalle_planilla (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planillaId INTEGER NOT NULL,
                        empleadoId INTEGER NOT NULL,
                        montoHorasExtrasInput REAL NOT NULL,
                        montoComision REAL NOT NULL,
                        montoDietas REAL NOT NULL,
                        montoPrima REAL NOT NULL,
                        otrosDescuentosInput REAL NOT NULL,
                        salarioBaseQuincena REAL NOT NULL,
                        valorHora REAL NOT NULL,
                        otrosIngresosGravables REAL NOT NULL,
                        otrosIngresosSinDescuento REAL NOT NULL,
                        salarioBruto REAL NOT NULL,
                        descSeguroSocial REAL NOT NULL,
                        descSeguroEducativo REAL NOT NULL,
                        descISR REAL NOT NULL,
                        otrosDescuentos REAL NOT NULL,
                        totalDescuentos REAL NOT NULL,
                        salarioNeto REAL NOT NULL,
                        alertaDescExcede INTEGER NOT NULL,
                        FOREIGN KEY(planillaId) REFERENCES planillas(id) ON DELETE CASCADE,
                        FOREIGN KEY(empleadoId) REFERENCES empleados(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_detalle_planilla_planillaId_empleadoId ON detalle_planilla(planillaId, empleadoId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_detalle_planilla_empleadoId ON detalle_planilla(empleadoId)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS detalle_planilla_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planillaId INTEGER NOT NULL,
                        empleadoId INTEGER NOT NULL,
                        horasExtraDiurnas REAL NOT NULL,
                        horasExtraNocturnas REAL NOT NULL,
                        montoComision REAL NOT NULL,
                        montoDietas REAL NOT NULL,
                        montoPrima REAL NOT NULL,
                        otrosDescuentosInput REAL NOT NULL,
                        salarioBaseQuincena REAL NOT NULL,
                        valorHora REAL NOT NULL,
                        montoHorasExtrasCalculado REAL NOT NULL,
                        otrosIngresosGravables REAL NOT NULL,
                        otrosIngresosSinDescuento REAL NOT NULL,
                        salarioBruto REAL NOT NULL,
                        descSeguroSocial REAL NOT NULL,
                        descSeguroEducativo REAL NOT NULL,
                        descISR REAL NOT NULL,
                        otrosDescuentos REAL NOT NULL,
                        totalDescuentos REAL NOT NULL,
                        salarioNeto REAL NOT NULL,
                        alertaDescExcede INTEGER NOT NULL,
                        FOREIGN KEY(planillaId) REFERENCES planillas(id) ON DELETE CASCADE,
                        FOREIGN KEY(empleadoId) REFERENCES empleados(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO detalle_planilla_new (
                        id, planillaId, empleadoId,
                        horasExtraDiurnas, horasExtraNocturnas,
                        montoComision, montoDietas, montoPrima, otrosDescuentosInput,
                        salarioBaseQuincena, valorHora, montoHorasExtrasCalculado,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    )
                    SELECT
                        id, planillaId, empleadoId,
                        0.0, 0.0,
                        montoComision, montoDietas, montoPrima, otrosDescuentosInput,
                        salarioBaseQuincena, valorHora, 0.0,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    FROM detalle_planilla
                """.trimIndent())
                db.execSQL("DROP TABLE detalle_planilla")
                db.execSQL("ALTER TABLE detalle_planilla_new RENAME TO detalle_planilla")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_detalle_planilla_planillaId_empleadoId ON detalle_planilla(planillaId, empleadoId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_detalle_planilla_empleadoId ON detalle_planilla(empleadoId)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE detalle_planilla ADD COLUMN montoBonificacion REAL NOT NULL DEFAULT 0.0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS detalle_planilla_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planillaId INTEGER NOT NULL,
                        empleadoId INTEGER NOT NULL,
                        horasExtraDiurnas REAL NOT NULL,
                        horasExtraNocturnas REAL NOT NULL,
                        montoComision REAL NOT NULL,
                        montoDietas REAL NOT NULL,
                        montoPrima REAL NOT NULL,
                        descMuebleria REAL NOT NULL,
                        descAdelanto REAL NOT NULL,
                        descAhorro REAL NOT NULL,
                        salarioBaseQuincena REAL NOT NULL,
                        valorHora REAL NOT NULL,
                        montoHorasExtrasCalculado REAL NOT NULL,
                        montoBonificacion REAL NOT NULL,
                        otrosIngresosGravables REAL NOT NULL,
                        otrosIngresosSinDescuento REAL NOT NULL,
                        salarioBruto REAL NOT NULL,
                        descSeguroSocial REAL NOT NULL,
                        descSeguroEducativo REAL NOT NULL,
                        descISR REAL NOT NULL,
                        otrosDescuentos REAL NOT NULL,
                        totalDescuentos REAL NOT NULL,
                        salarioNeto REAL NOT NULL,
                        alertaDescExcede INTEGER NOT NULL,
                        FOREIGN KEY(planillaId) REFERENCES planillas(id) ON DELETE CASCADE,
                        FOREIGN KEY(empleadoId) REFERENCES empleados(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO detalle_planilla_new (
                        id, planillaId, empleadoId,
                        horasExtraDiurnas, horasExtraNocturnas,
                        montoComision, montoDietas, montoPrima,
                        descMuebleria, descAdelanto, descAhorro,
                        salarioBaseQuincena, valorHora, montoHorasExtrasCalculado, montoBonificacion,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    )
                    SELECT
                        id, planillaId, empleadoId,
                        horasExtraDiurnas, horasExtraNocturnas,
                        montoComision, montoDietas, montoPrima,
                        otrosDescuentosInput, 0.0, 0.0,
                        salarioBaseQuincena, valorHora, montoHorasExtrasCalculado, montoBonificacion,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    FROM detalle_planilla
                """.trimIndent())
                db.execSQL("DROP TABLE detalle_planilla")
                db.execSQL("ALTER TABLE detalle_planilla_new RENAME TO detalle_planilla")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_detalle_planilla_planillaId_empleadoId ON detalle_planilla(planillaId, empleadoId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_detalle_planilla_empleadoId ON detalle_planilla(empleadoId)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS detalle_planilla_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        planillaId INTEGER NOT NULL,
                        empleadoId INTEGER NOT NULL,
                        horasExtraDiurnas REAL NOT NULL,
                        horasExtraNocturnas REAL NOT NULL,
                        montoComision REAL NOT NULL,
                        montoDietas REAL NOT NULL,
                        descAdelanto REAL NOT NULL,
                        descAhorro REAL NOT NULL,
                        salarioBaseQuincena REAL NOT NULL,
                        valorHora REAL NOT NULL,
                        montoHorasExtrasCalculado REAL NOT NULL,
                        montoBonificacion REAL NOT NULL,
                        otrosIngresosGravables REAL NOT NULL,
                        otrosIngresosSinDescuento REAL NOT NULL,
                        salarioBruto REAL NOT NULL,
                        descSeguroSocial REAL NOT NULL,
                        descSeguroEducativo REAL NOT NULL,
                        descISR REAL NOT NULL,
                        otrosDescuentos REAL NOT NULL,
                        totalDescuentos REAL NOT NULL,
                        salarioNeto REAL NOT NULL,
                        alertaDescExcede INTEGER NOT NULL,
                        FOREIGN KEY(planillaId) REFERENCES planillas(id) ON DELETE CASCADE,
                        FOREIGN KEY(empleadoId) REFERENCES empleados(id) ON DELETE NO ACTION
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO detalle_planilla_new (
                        id, planillaId, empleadoId,
                        horasExtraDiurnas, horasExtraNocturnas,
                        montoComision, montoDietas,
                        descAdelanto, descAhorro,
                        salarioBaseQuincena, valorHora, montoHorasExtrasCalculado, montoBonificacion,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    )
                    SELECT
                        id, planillaId, empleadoId,
                        horasExtraDiurnas, horasExtraNocturnas,
                        montoComision, montoDietas,
                        descAdelanto, descAhorro,
                        salarioBaseQuincena, valorHora, montoHorasExtrasCalculado, montoBonificacion,
                        otrosIngresosGravables, otrosIngresosSinDescuento,
                        salarioBruto, descSeguroSocial, descSeguroEducativo, descISR,
                        otrosDescuentos, totalDescuentos, salarioNeto, alertaDescExcede
                    FROM detalle_planilla
                """.trimIndent())
                db.execSQL("DROP TABLE detalle_planilla")
                db.execSQL("ALTER TABLE detalle_planilla_new RENAME TO detalle_planilla")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_detalle_planilla_planillaId_empleadoId ON detalle_planilla(planillaId, empleadoId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_detalle_planilla_empleadoId ON detalle_planilla(empleadoId)")
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "prospera_db"
                )
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                    .build().also { INSTANCE = it }
            }
    }
}