import com.android.build.gradle.LibraryExtension

plugins {
    id("maven-publish")
}

version = project.version

val android = extensions.findByType(LibraryExtension::class.java)

val androidVariantToRelease = "release"
if (android != null) {
    android.publishing {
        singleVariant(androidVariantToRelease) {
            withJavadocJar()
            withSourcesJar()
        }
    }
} else {
    extensions.configure(JavaPluginExtension::class.java) {
        withJavadocJar()
        withSourcesJar()
    }
}

afterEvaluate {
    publishing {
        repositories {
            maven {
                name = "HingeJFrog"
                url = uri("https://matchgroupcentral.jfrog.io/artifactory/hingeandroid-prod-gradle")
                credentials {
                    username = System.getenv("MGC_JFROG_USERNAME")
                    password = System.getenv("MGC_JFROG_PASSWORD")
                }
            }
        }

        publications {
            create<MavenPublication>("maven") {
                artifactId = computeArtifactId()
                if (android != null) {
                    from(components.findByName(androidVariantToRelease))
                } else {
                    from(components.findByName("java"))
                }

                pom {
                    name.set("OpenTelemetry Android (Hinge Fork)")
                    description.set(project.description)
                    // Removing unnecessary POM metadata for internal artifact
                }
            }
        }
    }
}

fun computeArtifactId(): String {
    val path = project.path
    if (!path.contains("instrumentation")) {
        return project.name
    }
    val match = Regex("[^:]+:[^:]+\$").find(path)
    val artifactId = match!!.value.replace(":", "-")
    return artifactId
}