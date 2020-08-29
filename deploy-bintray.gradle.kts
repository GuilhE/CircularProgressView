import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import org.jetbrains.dokka.gradle.DokkaTask
import java.io.FileInputStream
import java.util.*

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(Libs.com_jfrog_bintray_gradle_bintray_plugin)
        classpath(Libs.dokka_gradle_plugin)
    }
}

apply(plugin = Libs.maven_publish)
apply(plugin = Libs.org_jetbrains_dokka)
plugins.apply(BintrayPlugin::class.java) //https://github.com/bintray/gradle-bintray-plugin/issues/301

val bintrayRepo = properties["bintrayRepo"].toString()
val bintrayName = properties["bintrayName"].toString()
val libraryVersion = properties["libraryVersion"].toString()
val libraryDescription = properties["libraryDescription"].toString()
val siteUrl = properties["siteUrl"].toString()
val gitUrl = properties["gitUrl"].toString()

configure<BintrayExtension> {
    if (project.rootProject.file("local.properties").exists()) {
        val fis = FileInputStream(project.rootProject.file("local.properties"))
        val prop = Properties()
        prop.load(fis)
        user = prop.getProperty("bintray.user", "")
        key = prop.getProperty("bintray.apiKey", "")
    } else {
        user = System.getenv("bintrayUser")
        key = System.getenv("bintrayApiKey")
    }

    setPublications(bintrayRepo)

    pkg.apply {
        repo = bintrayRepo
        name = bintrayName
        description = libraryDescription
        websiteUrl = siteUrl
        issueTrackerUrl = "$siteUrl/issues"
        vcsUrl = gitUrl
        setLicenses("Apache-2.0")
        publish = true
        publicDownloadNumbers = true
        version.apply {
            name = libraryVersion
            vcsTag = libraryVersion
            desc = libraryDescription
            released = Date().toString()
        }
    }
}

configure<PublishingExtension> {
    val artifact = properties["artifact"].toString()
    val publishedGroupId = properties["publishedGroupId"].toString()
    val libraryName = properties["libraryName"].toString()
    val developerId = properties["developerId"].toString()
    val developerName = properties["developerName"].toString()
    val developerEmail = properties["developerEmail"].toString()

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(project.the<SourceSetContainer>()["main"].allSource)
    }

    val dokkaJar by tasks.registering(Jar::class) {
        group = JavaBasePlugin.DOCUMENTATION_GROUP
        description = "Assembles Kotlin docs with Dokka"
        archiveClassifier.set("javadoc")
        from(tasks.getting(DokkaTask::class) {
            outputFormat = "html"
            outputDirectory = "$buildDir/dokka"
        })
    }

    publications {
        create<MavenPublication>(bintrayRepo) {
            groupId = publishedGroupId
            artifactId = artifact
            version = libraryVersion

            from(components["java"])
            artifact(sourcesJar.get())
            artifact(dokkaJar.get())

            pom {
                packaging = "aar"
                name.set(libraryName)
                description.set(libraryDescription)
                url.set(siteUrl)
                scm {
                    connection.set(gitUrl)
                    developerConnection.set(connection.get())
                    url.set(siteUrl)
                }
                issueManagement { url.set("$siteUrl/issues") }
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set(developerId)
                        name.set(developerName)
                        email.set(developerEmail)
                    }
                }
            }
        }
    }
}