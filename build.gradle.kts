// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.google.devtools.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
}

tasks.register("release") {
    group = "build"
    description = "Builds all optimized production release ABI variants."
    dependsOn(
        ":app:assembleArm64Release",
        ":app:assembleArmv7Release",
        ":app:assembleUniversalRelease"
    )

    doLast {
        println("Copying APKs to outputs/release...")
        copy {
            from(layout.projectDirectory.dir("app/build/outputs/apk"))
            into(layout.projectDirectory.dir("outputs/release"))
            include("**/*.apk")
            exclude("**/*-unaligned.apk", "**/*-unsigned.apk", "**/*-debug.apk")
            eachFile {
                relativePath = RelativePath(true, name)
            }
            includeEmptyDirs = false
        }
        println("Release APKs successfully generated in outputs/release!")
    }
}
