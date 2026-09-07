plugins {
    kotlin("jvm") version "2.0.20"
    id("maven-publish")
    alias(libs.plugins.org.jreleaser)
    alias(libs.plugins.com.intershop.gradle.jaxb)
}

group = "com.vertama"
version = "4.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(libs.jakarta.xml.bind.jakarta.xml.bind.api)
    implementation(libs.org.glassfish.jaxb.jaxb.runtime)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}

jaxb {
    javaGen {
        register("xpersonenstand") {
            schema = file("src/main/resources/schemas/xpsw.domap.de/xpsw2611/xinneres.xpersonenstand.xsd")
            outputDir = file("src/main/java/com/vertama/xpersonenstand/model")
            binding = file("src/main/resources/binding.xjb")
            // -catalog routes external xs:import schemaLocations through src/main/resources/catalog.xml
            // → vendored copies under src/main/resources/schemas/. No network at codegen time.
            args = listOf("-extension", "-catalog", "src/main/resources/catalog.xml")
        }
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

tasks.withType<Jar> {
    // jaxb and javadocjar would clash otherwise
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

// https://jreleaser.org/guide/latest/examples/maven/maven-central.html#_gradle
jreleaser {
    signing {
        active = org.jreleaser.model.Active.ALWAYS
        armored = true
//        passphrase = System.getenv("JRELEASER_GPG_PASSPHRASE")
//        publicKey = System.getenv("JRELEASER_GPG_PUBLIC_KEY")
//        secretKey = System.getenv("JRELEASER_GPG_SECRET_KEY")
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active = org.jreleaser.model.Active.ALWAYS
                    url = "https://central.sonatype.com/api/v1/publisher"
//                    username = System.getenv("JRELEASER_MAVENCENTRAL_USERNAME")
//                    password = System.getenv("JRELEASER_MAVENCENTRAL_PASSWORD")
                    stagingRepository(layout.buildDirectory.dir("staging-deploy").get().toString())
                    applyMavenCentralRules = true
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.vertama"
            artifactId = "xpersonenstand"
            from(components["java"])
            pom {
                name.set("xpersonenstand")
                description.set("XPersonenstand library")
                url.set("https://github.com/Vertama-GmbH/xpersonenstand")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("cbarsch")
                        name.set("Christian Barsch")
                        email.set("dev@vertama.com")
                        organization.set("Vertama GmbH")
                        organizationUrl.set("https://vertama.com/")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/Vertama-GmbH/xpersonenstand.git")
                    developerConnection.set("scm:git:ssh://github.com/Vertama-GmbH/xpersonenstand.git")
                    url.set("https://github.com/Vertama-GmbH/xpersonenstand")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("staging-deploy"))
        }
    }
}
