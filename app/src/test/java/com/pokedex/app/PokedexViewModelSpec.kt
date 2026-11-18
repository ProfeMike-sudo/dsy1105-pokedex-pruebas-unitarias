package com.pokedex.app

import com.pokedex.app.data.model.*
import com.pokedex.app.repository.PokemonRepository
import com.pokedex.app.viewmodel.PokedexViewModel
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PokedexViewModelSpec : FunSpec({
    val ficha = PokemonDto(25, "pikachu", 4, 60, SpritesDto(null), listOf(PokemonTypeDto(1, TypeDto("electric"))))
    // Dispatcher de prueba: las corrutinas no avanzan hasta que la prueba lo ordena.
    val dispatcher = StandardTestDispatcher()

    // viewModelScope necesita Dispatchers.Main, que no existe en una prueba de la JVM:
    // se reemplaza antes de cada prueba y se restaura después.
    beforeTest { Dispatchers.setMain(dispatcher) }
    afterTest { Dispatchers.resetMain() }

    // Situación: la persona deja el campo en blanco y presiona Buscar.
    // Esperamos: aparece un mensaje de error y no se consulta nada.
    test("una consulta vacía muestra un mensaje y no llama al repositorio") {
        // Repository falso creado con MockK: el ViewModel cree que habla con el real.
        val repo = mockk<PokemonRepository>()
        val vm = PokedexViewModel(repo)

        vm.actualizarConsulta("   ")
        vm.buscar()

        vm.uiState.value.error shouldBe "Escribe un nombre o número."
        coVerify(exactly = 0) { repo.buscar(any()) }
    }

    // Situación: se busca pikachu y el Repository responde bien.
    // Esperamos: primero la pantalla queda "cargando" y después muestra la ficha.
    test("publica carga y después la ficha") {
        val repo = mockk<PokemonRepository>()
        coEvery { repo.buscar("pikachu") } returns ficha
        val vm = PokedexViewModel(repo)

        runTest(dispatcher) {
            vm.buscar()
            // La corrutina todavía no corrió: el estado intermedio se puede observar.
            vm.uiState.value.cargando shouldBe true
            // advanceUntilIdle deja correr todo lo pendiente.
            advanceUntilIdle()
        }

        vm.uiState.value.cargando shouldBe false
        vm.uiState.value.pokemon shouldBe ficha
        vm.uiState.value.error.shouldBeNull()
    }

    // Situación: el servidor responde "no existe" (error 404).
    // Esperamos: la pantalla muestra un mensaje que la persona entienda.
    test("un 404 se traduce a un mensaje comprensible") {
        val repo = mockk<PokemonRepository>()
        // Fabrica una respuesta HTTP 404, como la que enviaría un servidor real.
        val respuesta404 = Response.error<PokemonDto>(404, "{}".toResponseBody("application/json".toMediaType()))
        coEvery { repo.buscar(any()) } throws HttpException(respuesta404)
        val vm = PokedexViewModel(repo)

        runTest(dispatcher) { vm.buscar(); advanceUntilIdle() }

        vm.uiState.value.error!! shouldContain "No encontré"
        vm.uiState.value.pokemon.shouldBeNull()
        vm.uiState.value.cargando shouldBe false
    }

    // Situación: la primera búsqueda falla por falta de internet y la segunda funciona.
    // Esperamos: aparece el error y, al buscar otra vez, aparece la ficha y el error desaparece.
    test("tras un error de red, reintentar funciona") {
        val repo = mockk<PokemonRepository>()
        coEvery { repo.buscar("pikachu") } throws IOException("sin red") andThen ficha
        val vm = PokedexViewModel(repo)

        runTest(dispatcher) {
            vm.buscar(); advanceUntilIdle()
            vm.uiState.value.error!! shouldContain "internet"
            vm.buscar(); advanceUntilIdle()
        }

        vm.uiState.value.pokemon shouldBe ficha
        vm.uiState.value.error.shouldBeNull()
    }
})
