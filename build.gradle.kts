// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Versão do Plugin do Android (estável)
    id("com.android.application") version "8.4.1" apply false

    // Versão do Plugin do Kotlin (estável e compatível com o compilador do Compose)
    id("org.jetbrains.kotlin.android") version "1.9.23" apply false

    // Versão do Plugin do Google Services
    id("com.google.gms.google-services") version "4.4.1" apply false
}
