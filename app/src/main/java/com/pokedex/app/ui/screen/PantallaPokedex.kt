package com.pokedex.app.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.pokedex.app.data.model.PokemonDto
import com.pokedex.app.viewmodel.PokedexViewModel

/**
 * View de MVVM: representa el estado y comunica eventos al ViewModel.
 * Compose puede ejecutar esta función muchas veces; por eso aquí no iniciamos peticiones.
 */
@Composable
fun PantallaPokedex(viewModel: PokedexViewModel) {
    // Observa cambios solo cuando el ciclo de vida de la pantalla lo permite.
    // by permite leer directamente el valor observado. Los cambios provocan recomposición.
    val estado by viewModel.uiState.collectAsStateWithLifecycle()

    // Scaffold calcula los márgenes de las barras del sistema para el contenido.
    Scaffold { padding ->
        Column(
            // El orden de los modificadores aplica primero los márgenes del sistema,
            // después habilita desplazamiento y finalmente agrega espacio interior.
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Mi Pokédex", style = MaterialTheme.typography.headlineLarge)
            Text("Busca por nombre o número. Por ejemplo: pikachu o 25.")
            // Campo controlado: value viene del estado; onValueChange envía la edición.
            // No guarda una segunda copia del texto dentro del composable.
            OutlinedTextField(
                value = estado.consulta,
                onValueChange = viewModel::actualizarConsulta,
                label = { Text("Nombre o número") },
                singleLine = true,
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            )
            // ::buscar entrega una referencia a la función; el botón la ejecuta al tocarlo.
            Button(onClick = viewModel::buscar, enabled = !estado.cargando) {
                Text("Buscar")
            }
            // El orden da prioridad a carga, error y resultado; si no hay ninguno, muestra ayuda.
            when {
                estado.cargando -> {
                    CircularProgressIndicator()
                    Text("Consultando PokéAPI…")
                }
                estado.error != null -> {
                    // La rama ya comprobó que error no es null para esta fotografía del estado.
                    Text(estado.error!!, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = viewModel::buscar) { Text("Reintentar") }
                }
                estado.pokemon != null -> FichaPokemon(estado.pokemon!!)
                else -> Text("Escribe un Pokémon y presiona Buscar.")
            }
        }
    }
}

/**
 * Composable de presentación: recibe una ficha y dibuja sus datos, sin consultar la API.
 * Puede reutilizarse con cualquier PokemonDto que entregue el ViewModel.
 */
@Composable
private fun FichaPokemon(pokemon: PokemonDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("#${pokemon.id} · ${pokemon.name}",
                style = MaterialTheme.typography.headlineSmall)
            // Una URL nula se trata por separado de una descarga que falla.
            if (pokemon.sprites.frontDefault == null) {
                Text("Imagen no disponible")
            } else {
                // Coil descarga la imagen; Retrofit obtiene los datos JSON.
                AsyncImage(
                    model = pokemon.sprites.frontDefault,
                    // Descripción accesible para lectores de pantalla.
                    contentDescription = "Imagen de ${pokemon.name}",
                    // Usa un recurso del sistema si la descarga de la imagen falla.
                    error = painterResource(android.R.drawable.ic_menu_report_image),
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(160.dp)
                )
            }
            // Respeta slot, el orden de los tipos definido por la API, y une sus nombres.
            Text("Tipos: " + pokemon.types.sortedBy { it.slot }
                .joinToString { it.type.name })
            Spacer(modifier = Modifier.height(4.dp))
            // / 10.0 hace división decimal: decímetros a metros y hectogramos a kilogramos.
            Text("Altura: ${pokemon.height / 10.0} m")
            Text("Peso: ${pokemon.weight / 10.0} kg")
        }
    }
}
