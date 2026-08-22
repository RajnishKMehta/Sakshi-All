tasks.register("assembleDebug") {
    dependsOn(gradle.includedBuild("02_vault").task(":app:assembleDebug"))
    dependsOn(gradle.includedBuild("03_camera").task(":app:assembleDebug"))
}

tasks.register("test") {
    dependsOn(gradle.includedBuild("02_vault").task(":app:test"))
    dependsOn(gradle.includedBuild("03_camera").task(":app:test"))
}
