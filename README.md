# Pokédex: pruebas unitarias con Kotest, MockK y coroutines-test

Ejemplo de DSY1105, EA3, actividad 3.2. Es la Pokédex de la guía 12, que consulta un Pokémon por nombre o número en PokéAPI, con las dependencias de prueba ya configuradas y archivos de ejemplo dentro de `com.pokedex.app (test)`.

## Abrir y ejecutar en Android Studio

1. En Android Studio elige **File > New > Project from Version Control**, pega la URL de este repositorio y presiona **Clone**. También puedes descargar su ZIP y descomprimirlo.
2. Si usaste el ZIP, abre la carpeta raíz, donde está `settings.gradle.kts`.
3. Usa Android Studio con soporte para Android Gradle Plugin 8.13.1. Configura el JDK de Gradle en 21, que es el utilizado para compilar este ejemplo.
4. Instala Android SDK Platform 36 desde SDK Manager y acepta la sincronización de Gradle.
5. Selecciona un emulador o dispositivo Android API 24 o superior, con conexión a internet.
6. Ejecuta la configuración `app`. Escribe `pikachu` y presiona **Buscar**.

Android Studio configura la ruta del SDK en `local.properties`. Ese archivo es específico de cada equipo y no se versiona. La primera sincronización necesita internet para descargar dependencias.

## Ejecutar las pruebas

1. Instala el plugin **Kotest** en Android Studio: **File > Settings > Plugins > Marketplace**, busca **Kotest** y presiona **Install**.
2. En el panel **Project**, con la vista **Android**, haz clic derecho sobre `com.pokedex.app (test)` y elige **Run**.
3. Los resultados aparecen en el panel **Run**, en la parte inferior. Deben pasar las 17 pruebas.

## Qué hay en las pruebas

Todas están en `app/src/test/java/com/pokedex/app/`:

| Archivo | Herramienta | Qué muestra |
| --- | --- | --- |
| `PokedexTest.kt` | JUnit 4 | Pruebas que ya traía la Pokédex. Corren gracias al motor `junit-vintage-engine`. |
| `PrimeraPruebaSpec.kt` | Kotest | Una prueba con una API falsa escrita a mano. |
| `EjemploMockKSpec.kt` | Kotest y MockK | Programar respuestas, simular una falla y comprobar llamadas. |
| `PokemonRepositorySpec.kt` | Kotest y MockK | Pruebas del Repository: normalización, caché y errores. |
| `PokedexViewModelSpec.kt` | Kotest, MockK y coroutines-test | Pruebas del ViewModel: validación, carga y resultado, error 404 y reintento. |

Las dependencias y la activación de JUnit Platform están en `app/build.gradle.kts`.

## Estructura de la app

```text
app/src/main/java/com/pokedex/app/
    MainActivity.kt
    data/model/PokemonDto.kt
    data/remote/PokeApi.kt
    data/remote/PokeApiClient.kt
    repository/PokemonRepository.kt
    viewmodel/PokedexUiState.kt
    viewmodel/PokedexViewModel.kt
    ui/screen/PantallaPokedex.kt
```

Pokémon y sus personajes pertenecen a sus respectivos titulares. Este proyecto es un ejemplo educativo independiente y utiliza datos e imágenes proporcionados por PokéAPI.
