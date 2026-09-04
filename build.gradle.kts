// ########### Debug ###########
tasks.register("debug") {
    // 01_sdk
    dependsOn(
        gradle.includedBuild("01_sdk")
            .task(":sakshi-sdk:assembleDebug")
    )

    // 02_vault
    dependsOn(
        gradle.includedBuild("02_vault")
            .task(":app:assembleDebug")
    )

    // 03_camera
    dependsOn(
        gradle.includedBuild("03_camera")
            .task(":app:assembleDebug")
    )
}

// ########### check ###########
tasks.register("check") {
    // 01_sdk
    dependsOn(
        gradle.includedBuild("01_sdk")
            .task(":sakshi-sdk:check")
    )

    // 02_vault
    dependsOn(
        gradle.includedBuild("02_vault")
            .task(":app:check")
    )

    // 03_camera
    dependsOn(
        gradle.includedBuild("03_camera")
            .task(":app:check")
    )
}

// ########### Release ###########
tasks.register("release") {
    // 01_sdk
    dependsOn(
        gradle.includedBuild("01_sdk")
            .task(":sakshi-sdk:assembleRelease")
    )

    // 02_vault
    dependsOn(
        gradle.includedBuild("02_vault")
            .task(":app:assembleRelease")
    )

    // 03_camera
    dependsOn(
        gradle.includedBuild("03_camera")
            .task(":app:assembleRelease")
    )
}