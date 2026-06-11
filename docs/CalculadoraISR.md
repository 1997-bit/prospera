# CedulaHelper

Valida si una cadena de texto es una cédula panameña válida.

Sirve tanto para validar una cédula completa como para verificar el formato mientras el usuario escribe.

Basado en el validador de referencia: github.com/merlos/cedula-panama

## Cuándo usarlo

- Al guardar un empleado: verificar que la cédula es completa y válida.
- En un campo de texto: mostrar feedback en tiempo real mientras el usuario escribe.

## Método: validar

```kotlin
fun validar(cedula: String): ResultadoCedula
```

### Parámetros

| Parámetro | Tipo   | Descripción     |
| --------- | ------ | --------------- |
| cedula    | String | Texto a validar |

### Retorno: ResultadoCedula

```kotlin
data class ResultadoCedula(
    val esValida: Boolean, // true si el texto podría ser parte de una cédula válida
    val entrada: String, // el texto que se pasó como parámetro
    val esCompleta: Boolean, // true solo si la cédula está completa
    val cedula: List<String>? // [provincia, letra, tomo, asiento] o null si no está completa
)
```

### Ejemplo

```kotlin
// Cédula completa
val r = CedulaHelper.validar("8-123-456")
r.esValida // true
r.esCompleta // true
r.cedula // ["8", "", "123", "456"]

// Cédula de extranjero naturalizado
val r2 = CedulaHelper.validar("PE-1234-12345")
r2.cedula // ["0", "PE", "1234", "12345"]

// Mientras escribe (incompleta pero formato válido)
val r3 = CedulaHelper.validar("8-123-")
r3.esValida // true
r3.esCompleta // false
r3.cedula // null

// Inválida
val r4 = CedulaHelper.validar("88-BB")
r4.esValida // false
r4.esCompleta // false
```

### Métodos de acceso rápido

```kotlin
CedulaHelper.esValida("8-123-456") // true  — cédula completa y válida
CedulaHelper.esFormatoValido("8-123-") // true  — formato correcto aunque incompleta
```

### Uso en un campo de texto

```kotlin
// Validación en tiempo real
binding.etCedula.addTextChangedListener {
    val r = CedulaHelper.validar(it.toString())
    binding.tilCedula.error = when {
        it.isNullOrEmpty() -> null
        r.esCompleta -> null
        r.esValida -> null // sigue escribiendo, no mostrar error aun
        else -> "Formato inválido"
    }
}

// Validación al guardar
val r = CedulaHelper.validar(binding.etCedula.text.toString())
if (!r.esCompleta) {
    binding.etCedula.error = "Ingresa una cédula completa"
    return
}
```

### Formatos aceptados

| Tipo                    | Ejemplo        |
| ----------------------- | -------------- |
| Regular                 | 8-1234-12345   |
| Nacido en el extranjero | PE-1234-12345  |
| Extranjero con cédula   | E-1234-12345   |
| Naturalizado            | N-1234-12345   |
| Antes de vigencia       | 1AV-1234-12345 |
| Población indígena      | 1PI-1234-12345 |

### Notas

- La validación no distingue mayúsculas de minúsculas. "pe-123-456" y "PE-123-456" son equivalentes.
- Una cadena vacía retorna `esValida = true` para no marcar error cuando el campo está vacío.
- El campo `cedula[1]` contiene la letra especial (AV, PI, PE, E, N) o una cadena vacía para cédulas regulares.
- El campo `cedula[0]` siempre es el número de provincia. Para PE, E y N se usa "0".
