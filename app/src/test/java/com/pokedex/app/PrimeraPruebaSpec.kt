package com.pokedex.app

import com.pokedex.app.data.model.PokemonDto
import com.pokedex.app.data.model.SpritesDto
import com.pokedex.app.data.remote.PokeApi
import com.pokedex.app.repository.PokemonRepository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

// FunSpec: cada test("...") es un caso de prueba y su texto es el nombre que verás en el informe.
class PrimeraPruebaSpec : FunSpec({

    test("el Repository convierte a minúsculas y quita espacios antes de consultar") {
        // Dado: PokeApi es la interfaz que en la app real consulta PokéAPI por internet.
        // Aquí se crea al vuelo una versión falsa (object : PokeApi) que no usa red:
        // devuelve en "name" el texto exacto que recibió.
        val apiFalsa = object : PokeApi {
            override suspend fun obtenerPokemon(nombre: String) =
                PokemonDto(25, nombre, 4, 60, SpritesDto(null), emptyList())
        }
        // El Repository es la pieza que se prueba. Recibe la API falsa por el constructor.
        val repositorio = PokemonRepository(apiFalsa)

        // Cuando: se busca con espacios y mayúsculas
        val ficha = repositorio.buscar("  PIKACHU ")

        // Entonces: como la API falsa devuelve lo que recibió, ficha.name muestra
        // con qué texto la llamó el Repository. Si normalizó bien, fue "pikachu".
        ficha.name shouldBe "pikachu"
    }
})
