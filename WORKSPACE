load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.0"

RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

load("@rules_jvm_external//:defs.bzl", "maven_install")

maven_install(
    artifacts = [
        #"junit:junit:4.12.1",
        "com.fasterxml.jackson.core:jackson-core:2.12.3",
        "commons-validator:commons-validator:1.7",
        "io.javalin:javalin-bundle:3.13.7",
        "io.javalin:javalin:3.3.0",
        "org.slf4j:slf4j-simple:1.7.30",
        "org.postgresql:postgresql:42.2.20.jre7",
        "org.json:json:20210307",
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2/",
    ],
)
