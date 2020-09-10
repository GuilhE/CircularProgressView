import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import java.io.FileInputStream
import java.util.*

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath(Libs.com_jfrog_bintray_gradle_bintray_plugin)
    }
}

apply(plugin = Libs.maven_publish)
plugins.apply(BintrayPlugin::class.java) //https://github.com/bintray/gradle-bintray-plugin/issues/301

val bintrayRepo = properties["bintrayRepo"].toString()
val bintrayName = properties["bintrayName"].toString()
val libraryVersion = properties["libraryVersion"].toString()
val libraryDescription = properties["libraryDescription"].toString()
val siteUrl = properties["siteUrl"].toString()
val gitUrl = properties["gitUrl"].toString()

configure<BintrayExtension> {
    var ossPwd = ""
    if (project.rootProject.file("local.properties").exists()) {
        val fis = FileInputStream(project.rootProject.file("local.properties"))
        val prop = Properties()
        prop.load(fis)
        user = prop.getProperty("bintray.user", "")
        key = prop.getProperty("bintray.apiKey", "")
        ossPwd = prop.getProperty("bintray.ossPwd", "")
    } else {
        user = System.getenv("bintrayUser")
        key = System.getenv("bintrayApiKey")
        ossPwd = System.getenv("mavenCentralPwd")
    }

    setPublications(bintrayRepo)
    override = true

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
            mavenCentralSync.sync = true
            mavenCentralSync.user = user
            mavenCentralSync.password = ossPwd
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

    publications {
        create<MavenPublication>(bintrayRepo) {
            groupId = publishedGroupId
            artifactId = artifact
            version = libraryVersion

            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("dokkaJar"))
            artifact("$buildDir/outputs/aar/${artifactId}-release.aar")

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
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    configurations.getByName("implementation") {
                        dependencies.forEach {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", it.group)
                            dependencyNode.appendNode("artifactId", it.name)
                            dependencyNode.appendNode("version", it.version)
                        }
                    }
                }
            }
        }
    }
}