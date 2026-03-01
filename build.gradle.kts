import java.net.URI

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.plugin.spring)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
}

group = "dev.parcelview"
version = "0.0.1"
description = "Backend for ParcelView in Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(libs.spring.boot.starter)
	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.data.jdbc)
	implementation(libs.kotlin.reflect)
	runtimeOnly(libs.postgresql)
	runtimeOnly("com.h2database:h2")
	implementation(libs.spring.boot.starter.test)
	implementation(libs.kotlin.test.junit5)
	implementation(libs.kotlinx.serialization.json)
	implementation(libs.kotlinx.coroutines.core)
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")

	// Swagger docs
	implementation(libs.springdoc.openapi.starter.webmvc.ui)

	// Test
	testRuntimeOnly(libs.junit.platform.launcher)
	testImplementation(libs.spring.boot.starter.test) {
		exclude(module = "mockito-core")
	}
	testImplementation(libs.google.truth)
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
