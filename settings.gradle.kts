pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // HERE Maps SDK repository
        /*maven {
            url = uri("https://repo.here.com/artifactory/api/maven/here-olp-maven")
        }*/
    }
}

rootProject.name = "CRM_Logistico_Movil"
include(":app")
 