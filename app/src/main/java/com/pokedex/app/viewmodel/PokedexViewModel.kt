package com.pokedex.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import com.pokedex.app.repository.PokemonRepository
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Coordina la interacción: valida la consulta, pide datos y publica el estado de la UI.
 * Depende del Repository, por lo que no construye Retrofit ni dibuja componentes Compose.
 */
class PokedexViewModel(private val repository: PokemonRepository) : ViewModel() {
    // La versión mutable queda privada. StateFlow conserva y emite el último estado.
    private val _uiState = MutableStateFlow(PokedexUiState())
    val uiState = _uiState.asStateFlow() // La pantalla solo puede observarlo.

    // Se invoca al editar el campo. Limpia la ficha previa para no asociarla al texto nuevo.
    fun actualizarConsulta(valor: String) {
        if (_uiState.value.cargando) return
        // update recibe el estado actual (it); copy cambia solo las propiedades indicadas.
        _uiState.update { it.copy(consulta = valor, error = null, pokemon = null) }
    }

    // Solo los eventos Buscar y Reintentar llaman a este método, no las recomposiciones.
    fun buscar() {
        if (_uiState.value.cargando) return // Impide solicitudes simultáneas.
        val consulta = _uiState.value.consulta.trim()
        // Validar antes de lanzar la corrutina evita peticiones sin un recurso que consultar.
        if (consulta.isBlank()) {
            _uiState.update { it.copy(error = "Escribe un nombre o número.", pokemon = null) }
            return
        }

        // Publica carga antes de iniciar la petición y retira resultados o errores anteriores.
        _uiState.update { it.copy(cargando = true, error = null, pokemon = null) }
        // Este ámbito cancela su trabajo cuando se elimina el ViewModel.
        // La llamada suspend de Retrofit espera de forma asíncrona, sin bloquear la interfaz.
        viewModelScope.launch {
            try {
                val pokemon = repository.buscar(consulta)
                // Un nuevo estado hace que Compose muestre la ficha y oculte el progreso.
                _uiState.update { it.copy(cargando = false, pokemon = pokemon) }
            } catch (e: CancellationException) {
                // Cancelar la corrutina no es un error de conexión.
                throw e
            // El servidor respondió, pero el código HTTP indica un fallo.
            // 404 significa que no encontró el Pokémon; otros códigos conservan su número.
            } catch (e: HttpException) {
                mostrarError(if (e.code() == 404) "No encontré ese Pokémon. Revisa el nombre o número."
                    else "El servidor respondió con error ${e.code()}. Intenta nuevamente.")
            // Incluye pérdida de conexión, problemas de resolución y tiempo de espera.
            } catch (e: IOException) {
                mostrarError("No pude conectar. Revisa internet e intenta nuevamente.")
            // La respuesta llegó, pero Gson no pudo interpretarla con el formato esperado.
            } catch (e: JsonParseException) {
                mostrarError("La respuesta no tiene el formato esperado.")
            }
        }
    }

    // Centraliza el estado de fallo: termina la carga y habilita otro intento desde la UI.
    private fun mostrarError(mensaje: String) {
        _uiState.update { it.copy(cargando = false, pokemon = null, error = mensaje) }
    }
}
