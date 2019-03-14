import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.BintrayPlugin
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.publish.maven.internal.artifact.FileBasedMavenArtifact
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent


plugins {
	`junit-test-suite`
	`maven-publish` // FIXME temporary
	publishing

	id("com.github.ben-manes.versions") version "0.21.0"
	id("com.jfrog.bintray") version "1.8.4" apply false
}

val subprojectsForPublishing = setOf(
	"chotto-client-connector",
	"chotto-client-model",
	"chotto-service"
)

subprojects {
	group = "team.genki"
	version = "0.9.0"

	apply<JUnitTestSuitePlugin>()

	repositories {
		bintray("fluidsonic/maven")
		bintray("genki/maven")
		bintray("kotlin/kotlin-eap")
		bintray("kotlin/kotlinx")
		jcenter()
		mavenCentral()
	}

	tasks.withType<Test> {
		useJUnitPlatform {
			includeEngines("junit-jupiter")
		}

		reports {
			html.isEnabled = false
			junitXml.isEnabled = false
		}

		testLogging {
			events(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)

			exceptionFormat = TestExceptionFormat.FULL
			showExceptions = true
			testLogging.showStandardStreams = true
		}
	}

	if (subprojectsForPublishing.contains(name)) {
		apply<MavenPublishPlugin>()

		val bintrayUser = findProperty("bintrayUser") as String?
		val bintrayKey = findProperty("bintrayApiKey") as String?
		if (bintrayUser != null && bintrayKey != null) {
			apply<BintrayPlugin>()
			apply<PublishingPlugin>()

			configure<BintrayExtension> {
				user = bintrayUser
				key = bintrayKey

				pkg.apply {
					repo = "maven"
					issueTrackerUrl = "https://github.com/genki/chotto/issues"
					name = project.name
					publicDownloadNumbers = true
					publish = true
					userOrg = "genki"
					vcsUrl = "https://github.com/genki/chotto"
					websiteUrl = "https://github.com/genki/chotto"
					setLicenses("Apache-2.0")

					version.apply {
						name = project.version.toString()
						vcsTag = project.version.toString()
					}
				}

				afterEvaluate {
					setPublications(*publishing.publications.names.toTypedArray())
				}
			}

			tasks.withType<BintrayUploadTask> {
				doFirst {
					publishing.publications
						.filterIsInstance<MavenPublication>()
						.forEach { publication ->
							val moduleFile = buildDir.resolve("publications/${publication.name}/module.json")
							if (moduleFile.exists()) {
								publication.artifact(object : FileBasedMavenArtifact(moduleFile) {
									override fun getDefaultExtension() = "module"
								})
							}
						}
				}
			}
		}

		publishing {
			// FIXME temporary
			repositories {
				maven("${project.rootDir}/releases")
			}
		}
	}
}

tasks.withType<Wrapper> {
	distributionType = Wrapper.DistributionType.ALL
	gradleVersion = "5.3-rc-3"
}