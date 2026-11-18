package com.pokedex.app.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Configura el servicio remoto. object mantiene una instancia compartida en el proceso.
 * Este archivo conoce la URL y el convertidor; la pantalla no necesita esos detalles.
 */
object PokeApiClient {
    // Una sola instancia se reutiliza en todas las búsquedas.
    // lazy posterga la construcción hasta el primer acceso y conserva el resultado.
    val api: PokeApi by lazy {
        // OkHttp realiza el transporte HTTP. Limitamos la llamada completa a 20 segundos.
        // Si vence el tiempo, el ViewModel lo presenta como un problema de conexión.
        val clienteHttp = OkHttpClient.Builder()
            .callTimeout(20, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/") // La barra final es necesaria.
            .client(clienteHttp)
            // Convierte los campos JSON en PokemonDto y sus objetos anidados.
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            // Crea un objeto que implementa las anotaciones de nuestra interfaz PokeApi.
            .create(PokeApi::class.java)
    }
}
