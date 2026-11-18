package com.pokedex.app.viewmodel

import com.pokedex.app.data.model.PokemonDto

/**
 * Fotografía del estado de la pantalla: texto de entrada, carga, ficha y error.
 * data class aporta copy(), que permite publicar una versión nueva sin modificar la anterior.
 * Los valores iniciales muestran el campo con "pikachu", pero no disparan una consulta.
 */
data class PokedexUiState(
    val consulta: String = "pikachu",
    // Mientras es true, la UI muestra progreso y deshabilita nuevas búsquedas.
    val cargando: Boolean = false,
    // null representa la ausencia de un resultado, antes de buscar o después de un error.
    val pokemon: PokemonDto? = null,
    // null indica que no hay mensaje de error que presentar.
    val error: String? = null
)
