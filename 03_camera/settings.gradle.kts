pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
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

includeBuild("../01_sdk") {
    dependencySubstitution {
        substitute(
            module("io.github.rajnishkmehta.sakshi:sakshi-sdk")
        ).using(
            project(":sakshi-sdk")
        )
    }
}
