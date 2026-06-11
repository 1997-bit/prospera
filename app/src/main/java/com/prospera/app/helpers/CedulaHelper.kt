package com.prospera.app.helpers

object CedulaHelper {

    private val REGEX = Regex(
        """^P$|^(?:PE|E|N|[23456789]|[23456789](?:A|P)?|1[0123]?|1[0123]?(?:A|P)?)$|^(?:PE|E|N|[23456789]|[23456789](?:AV|PI)?|1[0123]?|1[0123]?(?:AV|PI)?)-?$|^(?:PE|E|N|[23456789](?:AV|PI)?|1[0123]?(?:AV|PI)?)-(?:\d{1,4})-?$|^(PE|E|N|[23456789](?:AV|PI)?|1[0123]?(?:AV|PI)?)-(\d{1,4})-(\d{1,6})$""",
        RegexOption.IGNORE_CASE
    )

    data class ResultadoCedula(
        val esValida: Boolean,
        val entrada: String,
        val esCompleta: Boolean,
        val cedula: List<String>?  // [provincia, letra, tomo, asiento] o null
    )

    fun validar(cedula: String): ResultadoCedula {
        val matched = REGEX.find(cedula)

        var esCompleta = false
        var partes: List<String>? = null

        if (matched != null) {
            // groups(0) = full match, groups 1-3 = capture groups
            val grupos = matched.groupValues.drop(1).toMutableList() // [parte1, parte2, parte3]

            if (grupos[0].isNotEmpty()) {
                esCompleta = true

                // PE, E, N -> provincia = "0"
                if (grupos[0].uppercase().matches(Regex("^(PE|E|N)$"))) {
                    grupos.add(0, "0")
                }

                // provincia numérica sin letra -> insertar "" en posición 1
                if (grupos[0].matches(Regex("^(1[0123]?|[23456789])?$"))) {
                    grupos.add(1, "")
                }

                // provincia con AV o PI -> separar número y letras
                if (grupos[0].uppercase().matches(Regex("^(1[0123]?|[23456789])(AV|PI)$"))) {
                    val tmp = Regex("(\\d+)(\\w+)").find(grupos[0])
                    if (tmp != null) {
                        grupos.removeAt(0)
                        grupos.add(0, tmp.groupValues[1]) // número
                        grupos.add(1, tmp.groupValues[2]) // AV o PI
                    }
                }

                partes = grupos.take(4)
            }
        }

        return ResultadoCedula(
            esValida = if (cedula.isEmpty()) true else REGEX.containsMatchIn(cedula),
            entrada = cedula,
            esCompleta = esCompleta,
            cedula = if (esCompleta) partes else null
        )
    }

    // Shortcuts
    fun esValida(cedula: String): Boolean = validar(cedula).esCompleta
    fun esFormatoValido(cedula: String): Boolean = validar(cedula).esValida
}