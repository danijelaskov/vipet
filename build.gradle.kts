plugins {
    java
    application
    alias(libs.plugins.javafx)
    alias(libs.plugins.module)
    alias(libs.plugins.jlink)
    alias(libs.plugins.spotless)
}

repositories {
    mavenCentral()
}

group = property("app.reverseDomain") as String
version = property("app.version") as String

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(property("java.version").toString()))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

application {
    mainModule.set("${property("app.reverseDomain")}")
    mainClass.set("${property("app.reverseDomain")}.Main")
    applicationName = property("app.name") as String
    applicationDefaultJvmArgs = listOf("-Dapp.name=${applicationName}")
}

javafx {
    version = property("javafx.version") as String
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing", "javafx.web")
}

spotless {
    java {
        googleJavaFormat()
    }
}

dependencies {
    implementation(libs.jcommander)

    implementation(libs.gson)
    implementation(libs.opencsv)
    implementation(libs.poi)
    implementation(libs.poi.ooxml)

    implementation(libs.commons.math3)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.log4j.api)
    implementation(libs.log4j.core)
    implementation(libs.log4j.slf4j2.impl)

    // Icons
    implementation(libs.ikonli.javafx)
    implementation(libs.ikonli.core)
    implementation(libs.ikonli.codicons)
    implementation(libs.ikonli.coreui)
    implementation(libs.ikonli.fluentui)
    implementation(libs.ikonli.materialdesign2)
    implementation(libs.ikonli.carbonicons)

    implementation(libs.jlatexmath)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

tasks.test {
    useJUnitPlatform()

    doFirst {
        val separator = File.separator
        val logDir = "$projectDir${separator}logs${separator}tests"

        systemProperty("log.dir", logDir)
        systemProperty("log.level", "DEBUG")
    }
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to "net.askov.vipet.Main"
        )
    }
    from("$rootDir/licenses") {
        into("licences")
    }
}

jlink {
    options = listOf(
        "--strip-debug",
        "--compress", "zip-6",
        "--no-header-files",
        "--no-man-pages",
        "--add-modules", "jdk.localedata",
        "--include-locales=sr,en"
    )

    launcher {
        name = property("app.name") as String
        jvmArgs = listOf(
            "-Dapp.name=${property("app.name")}",
            "-Dapp.version=${property("app.version")}",
            "-Dapp.dev=false",
            "-Dlog.level=INFO"
        )
    }

    jpackage {
        version = property("app.version") as String
        appVersion = property("app.version") as String
        description = property("app.description") as String
        vendor = property("app.author.name") as String

        // Installer
        skipInstaller = false
        installerName = "${property("app.name")} Installer"
        installerOptions = listOf(
            "--about-url", property("app.url") as String
        )

        val osName = System.getProperty("os.name").lowercase()
        if (osName.contains("windows")) {
            installerType = "msi"
            installerOptions = installerOptions + listOf(
                "--win-dir-chooser",
                "--win-shortcut",
                "--win-shortcut-prompt",
                "--license-file", "$rootDir/LICENSE"
            )
            icon = property("app.icon.windows") as String
        } else if (osName.contains("linux")) {
            installerType = "deb"
            installerOptions = installerOptions + listOf(
                "--linux-deb-maintainer", property("app.author.email") as String,
                "--linux-app-category", property("app.category") as String,
                "--linux-shortcut",
                "--license-file", "$rootDir/LICENSE"
            )
            icon = property("app.icon.linux") as String
        } else if (osName.contains("mac")) {
            installerType = "dmg"
        }
    }
}

tasks.named<JavaExec>("run") {
    doFirst {
        systemProperty("app.name", project.findProperty("app.name")?.toString() ?: "")
        systemProperty("app.version", project.findProperty("app.version")?.toString() ?: "")
        systemProperty("app.dev", "true")
        systemProperty("log.level", "DEBUG")
    }
}

tasks.named<ProcessResources>("processResources") {
    from("$rootDir/licenses") {
        into("licenses")
    }
    from("$rootDir/LICENSE")
}

tasks.register("prepareAppDir") {
    dependsOn("jpackage")
    
    doLast {
        val appDir = file("${layout.buildDirectory.get()}/AppDir")
        appDir.mkdirs()

        // Process app.desktop.tpl
        val desktopTemplate = file("AppImage/app.desktop.tpl").readText()
        val processedDesktop = desktopTemplate
            .replace("{{APP_NAME}}", project.property("app.name") as String)
            .replace("{{APP_CATEGORY}}", project.property("app.category") as String)
        file("$appDir/${project.property("app.name")}.desktop").writeText(processedDesktop)

        // Process AppRun.tpl
        val appRunTemplate = file("AppImage/AppRun.tpl").readText()
        val processedAppRun = appRunTemplate
            .replace("{{APP_NAME}}", project.property("app.name") as String)
        file("$appDir/AppRun").writeText(processedAppRun)
        
        // Make AppRun executable
        file("$appDir/AppRun").setExecutable(true)

        // Copy application files from jpackage output
        val jpackageDir = file("${layout.buildDirectory.get()}/jpackage/${project.property("app.name")}")
        copy {
            from(jpackageDir) {
                include("bin/**")
                include("lib/**")
            }
            into("$appDir/usr")
        }

        // Copy icon from jpackage output
        val iconFile = file("$jpackageDir/lib/${project.property("app.name")}.png")
        if (iconFile.exists()) {
            copy {
                from(iconFile)
                into(appDir)
                rename { "${project.property("app.name")}.png" }
            }
        } else {
            println("WARNING: Icon file not found at ${iconFile.absolutePath}")
        }
    }
}