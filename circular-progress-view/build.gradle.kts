plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdkVersion(AndroidConstants.compileSdkVersions)

    defaultConfig {
        minSdkVersion(AndroidConstants.minSdkVersion)
        targetSdkVersion(AndroidConstants.targetSdkVersion)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(Libs.android_annotations)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(android.sourceSets.getByName("main").java.srcDirs)
}
val dokkaDoc by tasks.registering(org.jetbrains.dokka.gradle.DokkaTask::class) {
    outputDirectory.set(buildDir.resolve("javadoc"))
    dokkaSourceSets {
        named("main") {
            noAndroidSdkLink.set(false)
        }
    }
}
val dokkaJar by tasks.registering(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(dokkaDoc)
}
apply(from = rootProject.file("deploy-bintray.gradle.kts"))