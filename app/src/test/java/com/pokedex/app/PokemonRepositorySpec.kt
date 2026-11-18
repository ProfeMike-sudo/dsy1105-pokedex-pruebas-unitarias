package com.pokedex.app

import com.pokedex.app.data.model.*
import com.pokedex.app.data.remote.PokeApi
import com.pokedex.app.repository.PokemonRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException

class PokemonRepositorySpec : FunSpec({
    // Dato de ejemplo que usarán todas las pruebas: la ficha de Pikachu.
    val ficha = PokemonDto(25, "pikachu", 4, 60, SpritesDto(null), listOf(PokemonTypeDto(1, TypeDto("electric"))))

    // Situación: se busca escribiendo "  PIKACHU ".
    // Esperamos: la API recibe "pikachu", sin espacios y en minúsculas.
    test("normaliza la entrada antes de consultar la API") {
        // API falsa creada con MockK: no usa internet.
        val api = mockk<PokeApi>()
        // coEvery programa al doble: "cada vez que te pidan pikachu, devuelve esta ficha".
        coEvery { api.obtenerPokemon("pikachu") } returns ficha

        PokemonRepository(api).buscar("  PIKACHU ") shouldBe ficha

        // coVerify comprueba que la API recibió exactamente "pikachu", una sola vez.
        coVerify(exactly = 1) { api.obtenerPokemon("pikachu") }
    }

    // Situación: se busca por nombre y después por número.
    // Esperamos: internet se usa una sola vez; la segunda respuesta sale de la memoria (caché).
    test("la segunda búsqueda por número sale de la caché") {
        val api = mockk<PokeApi>()
        coEvery { api.obtenerPokemon("pikachu") } returns ficha
        val repo = PokemonRepository(api)

        repo.buscar("pikachu")
        repo.buscar("25") shouldBe ficha

        // Si la caché funciona, la API se llamó una sola vez.
        coVerify(exactly = 1) { api.obtenerPokemon(any()) }
    }

    // Situación: la primera búsqueda falla por falta de internet y la segunda funciona.
    // Esperamos: el error llega a quien llamó, no se guarda, y el segundo intento sí trae la ficha.
    test("un error de red se propaga y no queda guardado en la caché") {
        val api = mockk<PokeApi>()
        // La primera llamada falla y la siguiente responde bien.
        coEvery { api.obtenerPokemon("pikachu") } throws IOException("sin red") andThen ficha
        val repo = PokemonRepository(api)

        shouldThrow<IOException> { repo.buscar("pikachu") }
        repo.buscar("pikachu") shouldBe ficha

        coVerify(exactly = 2) { api.obtenerPokemon("pikachu") }
    }
})
