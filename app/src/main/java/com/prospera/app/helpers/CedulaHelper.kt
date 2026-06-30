package com.prospera.app.helpers

import kotlin.text.Regex
import kotlin.text.RegexOption

object CedulaHelper {

    private val patronCedula = Regex(
        """^P$|^(?:PE|E|N|[2-9]|[2-9][AP]?|1[0-3]?|1[0-3]?[AP]?)$|^(?:PE|E|N|[2-9]|[2-9](?:AV|PI)?|1[0-3]?|1[0-3]?(?:AV|PI)?)-?$|^(?:PE|E|N|[2-9](?:AV|PI)?|1[0-3]?(?:AV|PI)?)-(?:\d{1,4})-?$|^(PE|E|N|[2-9](?:AV|PI)?|1[0-3]?(?:AV|PI)?)-(\d{1,4})-(\d{1,6})$""",
        RegexOption.IGNORE_CASE
    )

    data class ResultadoCedula(
        val esValida: Boolean,
        val entrada: String,
        val esCompleta: Boolean,
        val cedula: List<String>?  // [provincia, letra, tomo, asiento]
    )

    fun validar(cedula: String): ResultadoCedula {
        val matched: MatchResult? = patronCedula.find(cedula)
        var esCompleta = false
        var partes: List<String>? = null

        if (matched != null) {
            val gruposCaptura = matched.groupValues
            
            // Verificamos si es el formato completo (grupos 1, 2 y 3 del regex al final)
            if (gruposCaptura.size >= 4 && 
                gruposCaptura[1].isNotEmpty() && 
                gruposCaptura[2].isNotEmpty() && 
                gruposCaptura[3].isNotEmpty()) {
                
                esCompleta = true
                val listaProcesada: MutableList<String> = mutableListOf()
                val primeraParte = gruposCaptura[1].uppercase()

                // Normalizar provincia y letra
                when {
                    primeraParte == "PE" || primeraParte == "E" || primeraParte == "N" -> {
                        listaProcesada.add("0")
                        listaProcesada.add(primeraParte)
                    }
                    primeraParte.contains("AV") || primeraParte.contains("PI") -> {
                        val num = primeraParte.filter { it.isDigit() }
                        val letra = primeraParte.filter { it.isLetter() }
                        listaProcesada.add(num)
                        listaProcesada.add(letra)
                    }
                    else -> {
                        listaProcesada.add(primeraParte)
                        listaProcesada.add("")
                    }
                }
                
                listaProcesada.add(gruposCaptura[2]) // Tomo
                listaProcesada.add(gruposCaptura[3]) // Asiento
                partes = listaProcesada
            }
        }

        return ResultadoCedula(
            esValida = if (cedula.isEmpty()) true else patronCedula.containsMatchIn(cedula),
            entrada = cedula,
            esCompleta = esCompleta,
            cedula = partes
        )
    }

    fun esValida(cedula: String): Boolean = validar(cedula).esCompleta
    fun esFormatoValido(cedula: String): Boolean = validar(cedula).esValida
}