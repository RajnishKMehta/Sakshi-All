pluginManagement {
    repositories {
        google()
        mavenCentral()
        mavenLocal()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        mavenLocal()
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
