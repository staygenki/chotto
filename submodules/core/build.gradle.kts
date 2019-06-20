import org.jetbrains.kotlin.gradle.plugin.mpp.*

plugins {
	kotlin("multiplatform")
	id("kotlinx-serialization")
}

kotlin {
	iosArm64()
	iosX64()
	jvm()

	sourceSets {
		commonMain {
			kotlin.setSrcDirs(listOf("sources/common"))
			resources.setSrcDirs(emptyList<Any>())

			dependencies {
				implementation(kotlinx("serialization-runtime-common", "0.11.1"))

				api(kotlin("stdlib-common"))
				api(fluid("stdlib", "0.9.23"))
			}
		}

		commonTest {
			kotlin.setSrcDirs(listOf("sources/commonTest"))
			resources.setSrcDirs(emptyList<Any>())

			dependencies {
				implementation(submodule("test-model"))
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}

		jvmMain {
			kotlin.setSrcDirs(listOf("sources/jvm"))
			resources.setSrcDirs(emptyList<Any>())

			dependencies {
				implementation(kotlinx("serialization-runtime", "0.11.1"))

				api(kotlin("stdlib-jdk7"))
			}
		}

		jvmTest {
			kotlin.setSrcDirs(listOf("sources/jvmTest"))
			resources.setSrcDirs(emptyList<Any>())

			dependencies {
				implementation(kotlin("test-junit5"))
				implementation("org.junit.jupiter:junit-jupiter-api:5.4.0")

				runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
				runtimeOnly("org.junit.platform:junit-platform-runner:1.4.0")
			}
		}

		val iosMain by creating {
			kotlin.setSrcDirs(emptyList<Any>())
			resources.setSrcDirs(emptyList<Any>())
		}

		val iosTest by creating {
			kotlin.setSrcDirs(emptyList<Any>())
			resources.setSrcDirs(emptyList<Any>())
		}

		getByName("iosArm64Main") {
			kotlin.setSrcDirs(listOf("sources/iosArm64"))
			resources.setSrcDirs(emptyList<Any>())

			dependsOn(iosMain)

			dependencies {
				implementation(kotlinx("serialization-runtime-iosarm64", "0.11.1"))
			}
		}

		getByName("iosX64Main") {
			kotlin.setSrcDirs(listOf("sources/iosX64"))
			resources.setSrcDirs(emptyList<Any>())

			dependsOn(iosMain)

			dependencies {
				implementation(kotlinx("serialization-runtime-iosx64", "0.11.1"))
			}
		}

		getByName("iosX64Test") {
			kotlin.setSrcDirs(listOf("sources/iosX64Test"))
			resources.setSrcDirs(emptyList<Any>())

			dependsOn(iosTest)
		}
	}
}


val iosTest by tasks.creating<Task> {
	val device = findProperty("iosDevice")?.toString() ?: "iPhone 8"

	val iosTarget = kotlin.targets.getByName("iosX64") as KotlinNativeTarget
	val binary = iosTarget.binaries.getTest(NativeBuildType.DEBUG)
	val binaryFile = binary.outputFile

	dependsOn(binary.linkTask)

	group = JavaBasePlugin.VERIFICATION_GROUP

	doLast {
		exec {
			println("$ xcrun simctl spawn \"$device\" \"${binaryFile.absolutePath}\"")
			commandLine("xcrun", "simctl", "spawn", device, binaryFile.absolutePath)
		}
	}
}

tasks.named("check") {
	dependsOn("iosTest")
}
