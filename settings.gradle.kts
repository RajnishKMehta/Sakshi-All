pluginManagement {
    repositories {
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Sakshi-Camera"
include(":app")
// include(":sakshi-sdk")
// project(":sakshi-sdk").projectDir = file("../01_sdk/sakshi-sdk")
