import groovy.lang.Closure
import org.asciidoctor.gradle.AsciidoctorTask
import org.ysb33r.groovy.dsl.vfs.VFS

plugins {
    id("java")
    id("com.github.jruby-gradle.base") version "1.6.0"
    id("org.ysb33r.vfs") version "1.0.1"
    id("org.asciidoctor.convert") version "1.5.8"
    id("org.ajoberstar.github-pages") version "1.7.2"
}

buildscript {
    repositories { jcenter() }
    dependencies { classpath("commons-httpclient:commons-httpclient:3.1") }
}

repositories { jcenter() }

dependencies {
    gems("rubygems:slim:4.0.1")
    gems("rubygems:thread_safe:0.3.6")
}

val revealjsVersion = "3.7.0"
val asciidoctorBackendVersion = "1.0.4"
val downloadDir = file("$buildDir/download")
val revealJsDir = file("$downloadDir/reveal.js")
val templateDir = file("$downloadDir/templates")

asciidoctorj {
    version = "1.5.7"
}

apply(from = "gradle/pdf.gradle")

val download by tasks.registering {
    description = "Download extra deckjs/reveal.js resources"
    inputs.property("asciidoctorBackendVersion", asciidoctorBackendVersion)
    inputs.property("revealjsVersion", revealjsVersion)
    outputs.dir(templateDir)
    outputs.dir(revealJsDir)
    doLast {
        mkdir(downloadDir)
        mkdir(templateDir)
        val vfs: Closure<Closure<VFS>> by project.extra
        vfs(delegateClosureOf<VFS> {
            cp(mapOf("recursive" to true, "overwrite" to true),
                    "zip:https://github.com/asciidoctor/asciidoctor-reveal.js/archive/v$asciidoctorBackendVersion.zip!asciidoctor-reveal.js-$asciidoctorBackendVersion",
                    templateDir)
            cp(mapOf("recursive" to true, "overwrite" to true),
                    "zip:https://github.com/hakimel/reveal.js/archive/$revealjsVersion.zip!reveal.js-$revealjsVersion",
                    revealJsDir)
        })
    }
}

val copyTheme by tasks.registering(Copy::class) {
    dependsOn(download)
    destinationDir = file("$buildDir/asciidoc")

    into("revealjs/reveal.js") {
        from(fileTree("$revealJsDir/reveal.js-${revealjsVersion}"))
    }
    into("revealjs/reveal.js/css/theme") {
        from(fileTree("src/docs/theme"))
    }
}

val copyScreencasts by tasks.registering(Copy::class) {
    from(fileTree("src/docs/asciidoc/screencast"))
    into(file("$buildDir/asciidoc/revealjs"))
}

val asciidoctor by tasks.existing(AsciidoctorTask::class) {
    dependsOn(download, copyTheme, copyScreencasts)

    sources(delegateClosureOf<PatternSet> {
        include("index.adoc")
    })

    resources(closureOf<CopySpec> {
        from(sourceDir) {
            include("images/**")
        }
    })

    backends("revealjs")

    attributes = mapOf(
            "sourcedir" to sourceSets["main"].java.srcDirs.first(),
            "endpoint-url" to "http://example.org",
            "source-highlighter" to "highlightjs",
            "imagesdir" to "./images",
            "toc" to "left",
            "icons" to "font",
            "setanchors" to "true",
            "idprefix" to "",
            "idseparator" to "-",
            "basedir" to projectDir,
            "docinfo1" to "",
            "width" to 1280,
            "height" to 720,
            "project-version" to "1.0",
            "revealjs_transition" to "linear",
            "revealjs_transitionSpeed" to "fast",
            "revealjs_hideAddressBar" to "true",
            "revealjs_touch" to "true",
            "revealjs_history" to "true",
            "revealjs_slideNumber" to "true",
            "revealjs_theme" to "gradle",
            "examples" to file("$projectDir/examples"),
            "user-manual" to "https://docs.gradle.org/5.0-milestone-1/userguide/"
    )
    options(mapOf(
            "template_dirs" to listOf(
                    templateDir.resolve("asciidoctor-reveal.js-${asciidoctorBackendVersion}/templates/slim").absolutePath
            )
    ))

    apply(from = "gradle/asciidoctor-task-extensions.gradle", to = this)

    doLast {
        file("$buildDir/asciidoc/revealjs/index.html").writeText(
                file("$buildDir/asciidoc/revealjs/index.html").readText()
                        .replace(
                                "</head>",
                                "${file("src/docs/asciidoc/docinfo.html").readText()}</head>"))
    }

}


githubPages {
    setRepoUri("git@github.com:eskatos/${rootProject.name}.git")
    pages.from(file("$buildDir/asciidoc/revealjs"))
}

tasks {
    "assemble" {
        dependsOn(asciidoctor)
    }
    "publishGhPages" {
        dependsOn(asciidoctor)
    }
}
