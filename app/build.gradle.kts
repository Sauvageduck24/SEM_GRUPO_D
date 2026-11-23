import java.net.URI
import java.nio.file.Files
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.Base64

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp") version "2.2.10-2.0.2"
    kotlin("plugin.serialization") version "2.2.10"
}

abstract class GenerarPoliticaRed : DefaultTask() {
    @get:Input abstract val host: Property<String>
    @get:Input abstract val variante: Property<String>
    @get:Input abstract val backendPublicacionConfigurado: Property<Boolean>
    @get:Input @get:Optional abstract val pinActual: Property<String>
    @get:Input @get:Optional abstract val pinReserva: Property<String>
    @get:Input @get:Optional abstract val caducidad: Property<String>
    @get:InputFile @get:Optional @get:PathSensitive(PathSensitivity.NONE)
    abstract val caLaboratorio: RegularFileProperty

    @get:OutputDirectory abstract val recursos: DirectoryProperty

    @TaskAction
    fun generar() {
        val modo = variante.get()
        val backend = host.get().removeSurrounding("[", "]").lowercase()
        val carpeta = recursos.get().asFile.toPath()
        val certificado = carpeta.resolve("raw/p32_ca_laboratorio.cer")
        val ca = caLaboratorio.orNull
        if (ca != null) {
            val autoridad = ca.asFile.inputStream().use {
                CertificateFactory.getInstance("X.509").generateCertificate(it) as X509Certificate
            }
            require(autoridad.basicConstraints >= 0) { "p32LabCa debe ser un certificado de CA." }
            Files.createDirectories(certificado.parent)
            Files.write(certificado, autoridad.encoded)
        } else {
            Files.deleteIfExists(certificado)
        }
        val pines = when (modo) {
            "verificacion" -> {
                require(ca != null) { "La variante verificacion necesita p32LabCa." }
                bloquePines()
            }
            "release" -> {
                require(backendPublicacionConfigurado.get() || listOf(pinActual, pinReserva, caducidad).none { it.isPresent }) {
                    "Indica p32ReleaseUrl junto con los pines de publicación."
                }
                if (backendPublicacionConfigurado.get()) bloquePines() else ""
            }
            else -> ""
        }
        val config = carpeta.resolve("xml/network_security_config.xml")
        Files.createDirectories(config.parent)
        Files.writeString(config, buildString {
            appendLine("""<?xml version="1.0" encoding="utf-8"?>""")
            appendLine("<network-security-config>")
            appendLine("""    <base-config cleartextTrafficPermitted="false">""")
            appendLine("""        <trust-anchors><certificates src="system"/></trust-anchors>""")
            appendLine("    </base-config>")
            // Android 17 permite tráfico en claro en loopback si queda sin política explícita.
            val sinAnclaje = (listOf("localhost", "ip6-localhost", "127.0.0.1", "::1") + backend)
                .distinct()
                .filterNot { pines.isNotEmpty() && it == backend }
            appendLine("""    <domain-config cleartextTrafficPermitted="false">""")
            for (dominio in sinAnclaje) {
                appendLine("""        <domain includeSubdomains="false">$dominio</domain>""")
            }
            appendLine("    </domain-config>")
            if (pines.isNotEmpty()) {
                appendLine("""    <domain-config cleartextTrafficPermitted="false">""")
                appendLine("""        <domain includeSubdomains="false">$backend</domain>""")
                if (modo == "verificacion") {
                    appendLine("""        <trust-anchors><certificates src="@raw/p32_ca_laboratorio"/></trust-anchors>""")
                }
                append(pines)
                appendLine("    </domain-config>")
            }
            if (modo == "debug") {
                appendLine("    <debug-overrides>")
                appendLine("        <trust-anchors>")
                if (ca != null) appendLine("""            <certificates src="@raw/p32_ca_laboratorio"/>""")
                appendLine("""            <certificates src="user"/>""")
                appendLine("        </trust-anchors>")
                appendLine("    </debug-overrides>")
            }
            appendLine("</network-security-config>")
        })
    }

    private fun bloquePines(): String {
        val origen = if (variante.get() == "release") "p32Release" else "p32Lab"
        val actual = pinActual.orNull
        val reserva = pinReserva.orNull
        val fecha = caducidad.orNull
        require(actual != null && reserva != null && fecha != null) {
            "Indica ${origen}PinActual, ${origen}PinReserva y ${origen}PinCaducidad."
        }
        val huellas = listOf(actual, reserva).map { Base64.getDecoder().decode(it) }
        require(huellas.all { it.size == 32 } && !huellas[0].contentEquals(huellas[1])) {
            "Los pines deben ser dos huellas SHA-256 de clave pública distintas en Base64."
        }
        val vencimiento = LocalDate.parse(fecha)
        require(vencimiento.isAfter(LocalDate.now(ZoneOffset.UTC))) {
            "La caducidad del anclaje debe estar en el futuro."
        }
        return buildString {
            appendLine("""        <pin-set expiration="$vencimiento">""")
            for (huella in huellas) {
                val pin = Base64.getEncoder().encodeToString(huella)
                appendLine("""            <pin digest="SHA-256">$pin</pin>""")
            }
            appendLine("        </pin-set>")
        }
    }
}

fun urlHttps(valor: String): URI = URI.create(valor).also {
    require(it.scheme == "https" && it.host != null && it.userInfo == null &&
        it.rawQuery == null && it.rawFragment == null && it.rawPath.endsWith("/")) {
        "La URL del backend debe ser HTTPS, incluir el host y terminar en /."
    }
}

val urlLaboratorio = urlHttps(providers.gradleProperty("p32LabUrl").getOrElse("https://127.0.0.1:8443/"))
val caParaLaboratorio = providers.gradleProperty("p32LabCa").orNull?.let { file(it) }
val urlPublicacion = providers.gradleProperty("p32ReleaseUrl")
val destinoPublicacion = urlHttps(urlPublicacion.getOrElse("https://appsalud.invalid/"))
val verificarPines = providers.gradleProperty("p32Verificacion").map { it == "true" }.getOrElse(false)

android {
    namespace = "es.uma.seguridad.salud"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "es.uma.seguridad.salud"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "BACKEND_URL", "\"${urlLaboratorio.toASCIIString()}\"")
    }

    buildTypes {
        create("verificacion") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".verificacion"
            isDebuggable = false
            matchingFallbacks += "debug"
        }
        release {
            buildConfigField("String", "BACKEND_URL", "\"${destinoPublicacion.toASCIIString()}\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("verificacion")) { it.enable = verificarPines }
    onVariants { variant ->
        val modo = requireNotNull(variant.buildType)
        val origen = if (modo == "release") "p32Release" else "p32Lab"
        val nombre = variant.name.replaceFirstChar { it.uppercaseChar() }
        val politica = tasks.register<GenerarPoliticaRed>("generarPoliticaRed$nombre") {
            variante.set(modo)
            host.set((if (modo == "release") destinoPublicacion else urlLaboratorio).host)
            backendPublicacionConfigurado.set(urlPublicacion.isPresent)
            if (modo != "debug") {
                pinActual.set(providers.gradleProperty("${origen}PinActual"))
                pinReserva.set(providers.gradleProperty("${origen}PinReserva"))
                caducidad.set(providers.gradleProperty("${origen}PinCaducidad"))
            }
            if (modo != "release" && caParaLaboratorio != null) {
                caLaboratorio.fileValue(caParaLaboratorio)
            }
            recursos.set(layout.buildDirectory.dir("generated/p32/$nombre/res"))
        }
        variant.sources.res?.addGeneratedSourceDirectory(politica, GenerarPoliticaRed::recursos)
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.8.5")
    implementation("androidx.room:room-ktx:2.8.5")
    ksp("androidx.room:room-compiler:2.8.5")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.10.2")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.biometric)
    implementation(libs.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation("androidx.test:runner:1.7.0")
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
