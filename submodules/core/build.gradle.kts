import org.gradle.jvm.tasks.Jar

plugins {
	kotlin("multiplatform")
	kotlin("kapt")
}

kotlin {
	if (workaroundForKT30667) jvm()
	else targetFromPreset(presets.getByName("jvmWithJava"), "jvm")

	sourceSets {
		commonMain {
			kotlin.setSrcDirs(listOf("sources/common"))
			resources.setSrcDirs(emptyList())

			dependencies {
				api(kotlin("stdlib-common"))
				api(fluid("stdlib", "0.9.9"))
			}
		}

		commonTest {
			kotlin.setSrcDirs(listOf("sources/commonTest"))
			resources.setSrcDirs(emptyList())

			dependencies {
				implementation(submodule("test-model"))
				implementation(kotlin("test-common"))
				implementation(kotlin("test-annotations-common"))
			}
		}

		jvmMain {
			kotlin.setSrcDirs(
				if (workaroundForKT30667) listOf("sources/jvm", "build/generated/source/kaptKotlin/main")
				else listOf("sources/jvm")
			)
			resources.setSrcDirs(emptyList())

			dependencies {
				api(kotlin("stdlib-jdk8"))
				api(fluid("json-coding-jdk8", "0.9.21"))

				compileOnly(fluid("json-annotations", "0.9.21"))
			}
		}

		jvmTest {
			kotlin.setSrcDirs(listOf("sources/jvmTest"))
			resources.setSrcDirs(emptyList())

			dependencies {
				implementation(kotlin("test-junit5"))
				implementation("org.junit.jupiter:junit-jupiter-api:5.4.0")

				runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.4.0")
				runtimeOnly("org.junit.platform:junit-platform-runner:1.4.0")
			}
		}
	}
}

tasks.getByName<Jar>("jvmSourcesJar") {
	from(file("build/generated/source/kaptKotlin/main"))
}

dependencies {
	"kapt"(fluid("json-annotation-processor", "0.9.21"))
}
