package com.prospera.app.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.prospera.app.data.ConsolidadoMensualRow
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import com.prospera.app.data.entities.EmpleadoEntity
import com.prospera.app.data.HistorialPlanillaRow
import com.prospera.app.data.AporteCssRow



object GeneradorPdfReporte {

    private const val ANCHO_PAGINA = 595  // A4 a 72dpi aprox.
    private const val ALTO_PAGINA = 842
    private const val MARGEN = 40f

    fun generarConsolidado(
        context: Context,
        periodoTexto: String,      // ej. "Julio 2026"
        filas: List<ConsolidadoMensualRow>,
        totalBruto: Double,
        totalDescuentos: Double,
        totalNeto: Double
    ): File {
        val documento = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, 1).create()
        var pagina = documento.startPage(paginaInfo)
        var canvas = pagina.canvas

        val paintTitulo = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            isFakeBoldText = true
        }
        val paintSubtitulo = Paint().apply {
            color = Color.DKGRAY
            textSize = 12f
        }
        val paintHeaderTabla = Paint().apply {
            color = Color.BLACK
            textSize = 10f
            isFakeBoldText = true
        }
        val paintTexto = Paint().apply {
            color = Color.BLACK
            textSize = 10f
        }
        val paintLinea = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = MARGEN

        canvas.drawText("Planilla Consolidada", MARGEN, y, paintTitulo)
        y += 20f
        canvas.drawText(periodoTexto, MARGEN, y, paintSubtitulo)
        y += 30f

        // Encabezados de columna
        val colNombre = MARGEN
        val colCargo = MARGEN + 150f
        val colBruto = MARGEN + 280f
        val colDesc = MARGEN + 370f
        val colNeto = MARGEN + 460f

        canvas.drawText("Nombre", colNombre, y, paintHeaderTabla)
        canvas.drawText("Cargo", colCargo, y, paintHeaderTabla)
        canvas.drawText("Bruto", colBruto, y, paintHeaderTabla)
        canvas.drawText("Desc.", colDesc, y, paintHeaderTabla)
        canvas.drawText("Neto", colNeto, y, paintHeaderTabla)
        y += 6f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 16f

        for (fila in filas) {
            if (y > ALTO_PAGINA - 80f) {
                // Nueva página si nos quedamos sin espacio
                documento.finishPage(pagina)
                val nuevaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, documento.pages.size + 1).create()
                pagina = documento.startPage(nuevaInfo)
                canvas = pagina.canvas
                y = MARGEN
            }

            canvas.drawText(recortar(fila.nombreEmpleado, 22), colNombre, y, paintTexto)
            canvas.drawText(recortar(fila.cargoEmpleado, 18), colCargo, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.brutoMes), colBruto, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.descuentosMes), colDesc, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.netoMes), colNeto, y, paintTexto)
            y += 18f
        }

        y += 10f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 20f

        canvas.drawText("Total bruto: ${formatoMoneda(totalBruto)}", colNombre, y, paintHeaderTabla)
        y += 16f
        canvas.drawText("Total descuentos: ${formatoMoneda(totalDescuentos)}", colNombre, y, paintHeaderTabla)
        y += 16f
        canvas.drawText("Total neto: ${formatoMoneda(totalNeto)}", colNombre, y, paintHeaderTabla)

        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "consolidado_${System.currentTimeMillis()}.pdf")
        FileOutputStream(archivo).use { salida ->
            documento.writeTo(salida)
        }
        documento.close()

        return archivo
    }

    private fun formatoMoneda(valor: Double): String =
        String.Companion.format(Locale("es", "PA"), "B/. %.2f", valor)

    private fun recortar(texto: String, maxChars: Int): String =
        if (texto.length > maxChars) texto.take(maxChars - 1) + "…" else texto

    fun generarReporteColaboradores(
        context: Context,
        colaboradores: List<EmpleadoEntity>
    ): File {
        val documento = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, 1).create()
        var pagina = documento.startPage(paginaInfo)
        var canvas = pagina.canvas

        val paintTitulo = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintSubtitulo = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val paintHeaderTabla = Paint().apply { color = Color.BLACK; textSize = 9f; isFakeBoldText = true }
        val paintTexto = Paint().apply { color = Color.BLACK; textSize = 9f }
        val paintLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var y = MARGEN

        canvas.drawText("Reporte de Colaboradores", MARGEN, y, paintTitulo)
        y += 20f
        val activos = colaboradores.count { it.activo }
        canvas.drawText("$activos activos · ${colaboradores.size - activos} inactivos", MARGEN, y, paintSubtitulo)
        y += 30f

        val colNombre = MARGEN
        val colCedula = MARGEN + 130f
        val colCargo = MARGEN + 230f
        val colDepto = MARGEN + 330f
        val colSalario = MARGEN + 420f
        val colEstado = MARGEN + 490f

        canvas.drawText("Nombre", colNombre, y, paintHeaderTabla)
        canvas.drawText("Cédula", colCedula, y, paintHeaderTabla)
        canvas.drawText("Cargo", colCargo, y, paintHeaderTabla)
        canvas.drawText("Depto.", colDepto, y, paintHeaderTabla)
        canvas.drawText("Salario", colSalario, y, paintHeaderTabla)
        canvas.drawText("Estado", colEstado, y, paintHeaderTabla)
        y += 6f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 16f

        for (c in colaboradores) {
            if (y > ALTO_PAGINA - 60f) {
                documento.finishPage(pagina)
                val nuevaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, documento.pages.size + 1).create()
                pagina = documento.startPage(nuevaInfo)
                canvas = pagina.canvas
                y = MARGEN
            }

            canvas.drawText(recortar(c.nombre, 18), colNombre, y, paintTexto)
            canvas.drawText(c.cedula, colCedula, y, paintTexto)
            canvas.drawText(recortar(c.cargo, 14), colCargo, y, paintTexto)
            canvas.drawText(recortar(c.departamento, 12), colDepto, y, paintTexto)
            canvas.drawText(String.format(Locale("es", "PA"), "%.2f", c.salarioBase), colSalario, y, paintTexto)
            canvas.drawText(if (c.activo) "Activo" else "Inactivo", colEstado, y, paintTexto)
            y += 16f
        }

        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "colaboradores_${System.currentTimeMillis()}.pdf")
        FileOutputStream(archivo).use { salida -> documento.writeTo(salida) }
        documento.close()

        return archivo
    }
    fun generarExpedienteIndividual(
        context: Context,
        empleado: EmpleadoEntity,
        historial: List<HistorialPlanillaRow>
    ): File {
        val documento = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, 1).create()
        var pagina = documento.startPage(paginaInfo)
        var canvas = pagina.canvas

        val paintTitulo = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintSubtitulo = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val paintSeccion = Paint().apply { color = Color.BLACK; textSize = 12f; isFakeBoldText = true }
        val paintEtiqueta = Paint().apply { color = Color.DKGRAY; textSize = 10f }
        val paintValor = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }
        val paintTexto = Paint().apply { color = Color.BLACK; textSize = 10f }
        val paintLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var y = MARGEN

        canvas.drawText("Expediente de Colaborador", MARGEN, y, paintTitulo)
        y += 20f
        canvas.drawText("${empleado.nombre} · ${if (empleado.activo) "Activo" else "Inactivo"}", MARGEN, y, paintSubtitulo)
        y += 30f

        canvas.drawText("Datos personales y laborales", MARGEN, y, paintSeccion)
        y += 6f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 18f

        val datos = listOf(
            "Cédula" to empleado.cedula,
            "Cargo" to empleado.cargo,
            "Departamento" to empleado.departamento,
            "Salario base" to formatoMoneda(empleado.salarioBase),
            "Año de ingreso" to empleado.anioInicio.toString(),
            "Estado civil" to empleado.estadoCivil.replaceFirstChar { it.uppercase() }
        )
        for ((etiqueta, valor) in datos) {
            canvas.drawText(etiqueta, MARGEN, y, paintEtiqueta)
            canvas.drawText(valor, MARGEN + 160f, y, paintValor)
            y += 18f
        }

        y += 16f
        canvas.drawText("Historial de planillas pagadas", MARGEN, y, paintSeccion)
        y += 6f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 18f

        if (historial.isEmpty()) {
            canvas.drawText("Sin planillas pagadas registradas.", MARGEN, y, paintTexto)
            y += 18f
        } else {
            for (fila in historial) {
                if (y > ALTO_PAGINA - 60f) {
                    documento.finishPage(pagina)
                    val nuevaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, documento.pages.size + 1).create()
                    pagina = documento.startPage(nuevaInfo)
                    canvas = pagina.canvas
                    y = MARGEN
                }
                val etiquetaPeriodo = if (fila.periodo == "1ra_quincena") "1ra quincena" else "2da quincena"
                canvas.drawText("$etiquetaPeriodo · ${fila.mes}/${fila.anio}", MARGEN, y, paintTexto)
                canvas.drawText(formatoMoneda(fila.salarioNeto), MARGEN + 300f, y, paintTexto)
                y += 16f
            }
        }

        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "expediente_${empleado.cedula}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(archivo).use { salida -> documento.writeTo(salida) }
        documento.close()

        return archivo
    }

    fun generarReporteCss(
        context: Context,
        periodoTexto: String,
        filas: List<AporteCssRow>,
        totalCss: Double
    ): File {
        val documento = PdfDocument()
        val paginaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, 1).create()
        var pagina = documento.startPage(paginaInfo)
        var canvas = pagina.canvas

        val paintTitulo = Paint().apply { color = Color.BLACK; textSize = 18f; isFakeBoldText = true }
        val paintSubtitulo = Paint().apply { color = Color.DKGRAY; textSize = 12f }
        val paintHeaderTabla = Paint().apply { color = Color.BLACK; textSize = 10f; isFakeBoldText = true }
        val paintTexto = Paint().apply { color = Color.BLACK; textSize = 10f }
        val paintLinea = Paint().apply { color = Color.LTGRAY; strokeWidth = 1f }

        var y = MARGEN

        canvas.drawText("Reporte CSS (Caja de Seguro Social)", MARGEN, y, paintTitulo)
        y += 20f
        canvas.drawText(periodoTexto, MARGEN, y, paintSubtitulo)
        y += 30f

        val colNombre = MARGEN
        val colCedula = MARGEN + 160f
        val colSS = MARGEN + 300f
        val colSE = MARGEN + 390f
        val colTotal = MARGEN + 470f

        canvas.drawText("Nombre", colNombre, y, paintHeaderTabla)
        canvas.drawText("Cédula", colCedula, y, paintHeaderTabla)
        canvas.drawText("S. Social", colSS, y, paintHeaderTabla)
        canvas.drawText("S. Educ.", colSE, y, paintHeaderTabla)
        canvas.drawText("Total", colTotal, y, paintHeaderTabla)
        y += 6f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 16f

        for (fila in filas) {
            if (y > ALTO_PAGINA - 80f) {
                documento.finishPage(pagina)
                val nuevaInfo = PdfDocument.PageInfo.Builder(ANCHO_PAGINA, ALTO_PAGINA, documento.pages.size + 1).create()
                pagina = documento.startPage(nuevaInfo)
                canvas = pagina.canvas
                y = MARGEN
            }

            canvas.drawText(recortar(fila.nombreEmpleado, 22), colNombre, y, paintTexto)
            canvas.drawText(fila.cedula, colCedula, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.seguroSocialMes), colSS, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.seguroEducativoMes), colSE, y, paintTexto)
            canvas.drawText(formatoMoneda(fila.totalCss), colTotal, y, paintTexto)
            y += 18f
        }

        y += 10f
        canvas.drawLine(MARGEN, y, ANCHO_PAGINA - MARGEN, y, paintLinea)
        y += 20f
        canvas.drawText("Total aportes CSS: ${formatoMoneda(totalCss)}", colNombre, y, paintHeaderTabla)

        documento.finishPage(pagina)

        val carpeta = File(context.cacheDir, "reportes").apply { mkdirs() }
        val archivo = File(carpeta, "reporte_css_${System.currentTimeMillis()}.pdf")
        FileOutputStream(archivo).use { salida -> documento.writeTo(salida) }
        documento.close()

        return archivo
    }
}