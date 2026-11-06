package com.example.cuidadoyvirtudapp.domain

import java.text.Normalizer

/**
 * Lógica pura (sin Android) de dos funciones de acompañamiento:
 *  1. Motivos: lo que la persona cuenta cuando se siente "más o menos" o "mal". La IA NO lee ni interpreta ese texto
 *     para detectar riesgo; solo se agrupa en una categoría para que el funcionario lo encuentre fácil.
 *  2. Compañía: sugerir a otra persona del programa que también quiere conversar. Nunca se comparten datos
 *     de contacto; la presentación la decide y organiza un humano (ver Repo.decidirEncuentro).
 */
object Social {
    /** Opciones de respaldo para quien no quiere o no puede hablar: respuestas codificadas, sin texto libre. */
    val OPCIONES_MOTIVO = listOf(
        "😴" to "Dormí mal",
        "🤕" to "Me duele algo",
        "😔" to "Me siento solo/a",
        "👪" to "Mi familia",
        "💊" to "Mis remedios",
        "💬" to "Otro motivo",
    )

    private val REGLAS = listOf(
        "Dolor o malestar físico" to listOf("duele", "dolor", "rodilla", "espalda", "cabeza", "mareo", "cansad", "enferm", "resfr", "fiebre", "estomago"),
        "Sueño" to listOf("dormi", "sueno", "insomnio", "desvel", "pesadilla"),
        "Soledad o tristeza" to listOf("solo", "sola", "soledad", "triste", "lloro", "llore", "nadie", "deprim"),
        "Familia" to listOf("hijo", "hija", "nieto", "nieta", "familia", "hermano", "hermana", "esposo", "esposa", "viudo", "viuda"),
        "Medicación" to listOf("remedio", "pastilla", "medicamento", "receta"),
        "Preocupación económica" to listOf("plata", "dinero", "cuenta", "deuda", "pension", "gasto"),
    )

    private fun limpiar(t: String): String =
        Normalizer.normalize(t.lowercase(), Normalizer.Form.NFD).replace(Regex("\\p{Mn}+"), "")

    /** Agrupa un relato en una categoría simple. Solo para ordenar la ficha; no es un diagnóstico. */
    fun categoria(texto: String): String {
        val t = limpiar(texto)
        return REGLAS.firstOrNull { (_, claves) -> claves.any { t.contains(it) } }?.first ?: "Otro motivo"
    }

    /**
     * Elige a quién sugerir. Solo entre quienes YA aceptaron conocer a otras personas y hoy también quieren compañía
     * (esa preselección la hace quien llama). Se prefiere la misma zona (para que puedan encontrarse); desempate estable por id.
     */
    fun elegirCandidato(yo: Persona, candidatos: List<Persona>): Persona? =
        candidatos.filter { it.id != yo.id }
            .sortedWith(compareBy<Persona>({ if (it.zona == yo.zona) 0 else 1 }, { it.id }))
            .firstOrNull()
}
