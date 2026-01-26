import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    iosArm64()
    iosSimulatorArm64()
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.supabase.storage)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "org.example.project.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

val props = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    props.load(localPropertiesFile.inputStream())
}

val sUrl = props.getProperty("supabase.url") ?: "https://default.url"
val sKey = props.getProperty("supabase.key") ?: "default_key"

extensions.configure<com.codingfeline.buildkonfig.gradle.BuildKonfigExtension> {
    packageName = "org.example.project.shared"

    defaultConfigs {
        buildConfigField(Type.STRING, "SUPABASE_URL", sUrl)
        buildConfigField(Type.STRING, "SUPABASE_KEY", sKey)
    }
}