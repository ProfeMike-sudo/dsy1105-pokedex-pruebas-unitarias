// Repositorios donde Gradle busca los plugins de compilación.
pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
// Centraliza los repositorios de librerías; evita declararlos de nuevo en cada módulo.
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "PokedexEA3"
// Registra app como el módulo Android que se puede compilar y ejecutar.
include(":app")
