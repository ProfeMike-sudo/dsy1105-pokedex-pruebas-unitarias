package com.pokedex.app.data.remote

import com.pokedex.app.data.model.PokemonDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Contrato HTTP: describe qué pedir y qué objeto Kotlin debe devolver Retrofit.
 * Retrofit genera la implementación al llamar a create(PokeApi::class.java).
 */
interface PokeApi {
    // La ruta se agrega a https://pokeapi.co/api/v2/.
    // GET consulta datos. La ruta no comienza con / para conservar /api/v2/ de la base.
    @GET("pokemon/{nombre}")
    // @Path reemplaza {nombre}: "pikachu" y "25" son entradas válidas.
    // suspend permite esperar desde una corrutina; Retrofit ejecuta la red sin bloquear la UI.
    // Una respuesta HTTP no exitosa lanza HttpException, que maneja el ViewModel.
    suspend fun obtenerPokemon(@Path("nombre") nombre: String): PokemonDto
}
