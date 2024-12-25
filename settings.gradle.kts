import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.2.1"
}

dependencyResolutionManagement {
    // Configure project's dependencies
    repositories {
        mavenCentral()

        // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
        intellijPlatform {
            defaultRepositories()
        }
    }
}

include(
    "modules/core",
    "modules/products/goland",
    "modules/products/gradle",
    "modules/products/idea",
    "modules/products/nodejs",
    "modules/products/rider",
    "modules/products/toml",
)

rootProject.name = "mise"

rootProject.children.forEach {
    it.name = (it.name.replaceFirst("modules/", "mise/").replace("/", "-"))
}
