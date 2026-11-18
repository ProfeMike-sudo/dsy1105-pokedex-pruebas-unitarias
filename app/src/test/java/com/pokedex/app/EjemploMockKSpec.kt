package com.pokedex.app

import com.pokedex.app.data.model.PokemonDto
import com.pokedex.app.data.model.PokemonTypeDto
import com.pokedex.app.data.model.SpritesDto
import com.pokedex.app.data.model.TypeDto
import com.pokedex.app.data.remote.PokeApi
import com.pokedex.app.repository.PokemonRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException

class EjemploMockKSpec : FunSpec({

    // Datos de ejemplo: una ficha de Charmander inventada por nosotros.
    val ficha = PokemonDto(
        4, "charmander", 6, 85,   // número, nombre, altura y peso
        SpritesDto(null),         // sin imagen
        listOf(PokemonTypeDto(1, TypeDto("fire")))
    )

    // Ejemplo 1: programar una respuesta con coEvery ... returns.
    test("el doble devuelve la ficha que se le programó") {
        // Se crea el doble de PokeApi y se programa: si piden charmander, devuelve la ficha.
        val api = mockk<PokeApi>()
        coEvery { api.obtenerPokemon("charmander") } returns ficha

        PokemonRepository(api).buscar("charmander") shouldBe ficha
    }

    // Ejemplo 2: simular una falla y la recuperación con throws ... andThen.
    test("si la API falla una vez, el error llega y el siguiente intento funciona") {
        val api = mockk<PokeApi>()
        coEvery { api.obtenerPokemon("charmander") } throws IOException("sin red") andThen ficha
        val repo = PokemonRepository(api)

        // 1.ª llamada: falla.
        shouldThrow<IOException> { repo.buscar("charmander") }
        // 2.ª llamada: funciona.
        repo.buscar("charmander") shouldBe ficha
    }

    // Ejemplo 3: comprobar cómo se usó el doble con coVerify.
    test("el Repository llama a la API una sola vez por consulta") {
        val api = mockk<PokeApi>()
        coEvery { api.obtenerPokemon("charmander") } returns ficha

        PokemonRepository(api).buscar("charmander")

        // Comprueba que la API recibió charmander exactamente una vez.
        coVerify(exactly = 1) { api.obtenerPokemon("charmander") }
    }
})
