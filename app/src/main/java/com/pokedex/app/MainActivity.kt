package com.pokedex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.pokedex.app.data.remote.PokeApiClient
import com.pokedex.app.repository.PokemonRepository
import com.pokedex.app.ui.screen.PantallaPokedex
import com.pokedex.app.viewmodel.PokedexViewModel

/**
 * Punto de entrada de Android. Conecta las dependencias y abre la pantalla Compose.
 * La búsqueda y sus resultados pertenecen al ViewModel, no a esta actividad.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Dibuja detrás de las barras del sistema; Scaffold aplica los márgenes en la pantalla.
        enableEdgeToEdge()
        // Reemplaza una vista XML por una jerarquía de funciones composables.
        setContent {
            // En tu proyecto puedes usar el tema Material 3 generado por Android Studio.
            MaterialTheme {
                // viewModel() recupera la instancia del ámbito de la actividad si ya existe.
                // La fábrica se usa al crearla: API -> Repository -> ViewModel.
                // Así se conserva el estado cuando una rotación recrea la actividad.
                val vm: PokedexViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        PokedexViewModel(PokemonRepository(PokeApiClient.api))
                    }
                })
                // La pantalla recibe el ViewModel para observar el estado y enviar acciones.
                PantallaPokedex(viewModel = vm)
            }
        }
    }
}
