import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    jvm("desktop")
    
    sourceSets {
        val desktopMain by getting
        
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.kotlinx.io.core)
            implementation(libs.kotlinx.collection)

            implementation(libs.compose.navigation)

            implementation(libs.filekit.core)
            implementation(libs.filekit.dialogs)
            implementation(libs.filekit.dialogs.compose)

            implementation(libs.serialization.json)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.driver)
            implementation(libs.sqldelight.coroutines)
            // 添加 SLF4J 实现以消除警告
            implementation(libs.slf4j.simple)
        }
    }
}


sqldelight {
    databases {
        create("YtorDatabase") {
            packageName.set("io.kapaseker.ytor.database")
            // 使用同步方法，避免不必要的 runBlocking
            generateAsync.set(false)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.kapaseker.ytor.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Ytor"
            packageVersion = "1.0.0"

            windows {
                iconFile.set(project.file("ytor.icns"))
            }
        }
    }
}
