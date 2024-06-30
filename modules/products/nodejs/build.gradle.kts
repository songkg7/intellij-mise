fun properties(key: String) = project.findProperty(key).toString()

plugins {
    id("org.jetbrains.intellij")
    alias(libs.plugins.kotlin) // Kotlin support
}

// Configure Gradle IntelliJ Plugin - read more: https://github.com/JetBrains/gradle-intellij-plugin
intellij {
    version.set(properties("platformVersion"))
    type.set("IU")

    // Plugin Dependencies
    plugins.set(listOf("JavaScript", "NodeJS"))
}

dependencies {
    implementation(project(":mise-core"))
}