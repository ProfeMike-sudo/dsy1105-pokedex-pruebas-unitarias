package com.pokedex.app.repository

import com.pokedex.app.data.model.PokemonDto
import com.pokedex.app.data.remote.PokeApi
import java.util.Locale

/**
 * Punto de acceso a los datos para el ViewModel: decide entre caché y servicio remoto.
 * Recibe PokeApi por constructor para poder sustituir la red por una API falsa en pruebas.
 */
class PokemonRepository(private val api: PokeApi) {
    // Caché en memoria: evita repetir consultas durante la vida del repositorio.
    // El mapa no es una base de datos: se pierde cuando deja de existir este Repository.
    private val cache = mutableMapOf<String, PokemonDto>()

    suspend fun buscar(entrada: String): PokemonDto {
        // Normaliza la entrada: los espacios externos y las mayúsculas no cambian la búsqueda.
        // Locale.ROOT evita que el idioma del dispositivo altere la conversión a minúsculas.
        val clave = entrada.trim().lowercase(Locale.ROOT)
        // ?. ejecuta let solo si existe una ficha. return termina buscar sin usar la red.
        cache[clave]?.let { return it }

        // Si no hay caché, espera la respuesta. Los errores se propagan al ViewModel.
        // Solo se guardan resultados correctos, porque una excepción interrumpe este flujo.
        val pokemon = api.obtenerPokemon(clave)
        // La misma ficha se puede buscar por nombre o por número.
        // Guardar también por nombre e ID evita otra petición si cambia la forma de consultar.
        cache[clave] = pokemon
        cache[pokemon.name] = pokemon
        cache[pokemon.id.toString()] = pokemon
        return pokemon
    }
}
