pluginManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        mavenLocal()
        val gprUser =
            providers.gradleProperty("gpr.user").orNull
                ?: System.getenv("GITHUB_ACTOR")

        val gprKey =
            providers.gradleProperty("gpr.key").orNull
                ?: System.getenv("GITHUB_TOKEN")

        if (!gprUser.isNullOrEmpty() && !gprKey.isNullOrEmpty()) {
            maven {
                url = uri("https://maven.pkg.github.com/RajnishKMehta/sakshi-sdk")

                credentials {
                    username = gprUser
                    password = gprKey
                }

                content {
                    includeModule(
                        "io.github.rajnishkmehta.sakshi",
                        "sakshi-sdk"
                    )
                }
            }
        }
        google()
        mavenCentral()
    }
}

rootProject.name = "Sakshi-Camera"

include(":app")

/*
includeBuild("../01_sdk") {
    dependencySubstitution {
        substitute(
            module("io.github.rajnishkmehta.sakshi:sakshi-sdk")
        ).using(
            project(":sakshi-sdk")
        )
    }
}
*/
