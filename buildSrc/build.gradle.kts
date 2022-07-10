plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

kotlin {
    kotlinDslPluginOptions {
        jvmTarget.set("1.8")
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    implementation("com.badlogicgames.gdx:gdx-jnigen:2.0.1")
    implementation("org.reflections:reflections:0.10.2")
}
