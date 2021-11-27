workspace(
    name = "flink",
    managed_directories = {"@npm": ["node_modules"]},
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

http_archive(
    name = "rules_jvm_external",
    sha256 = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169",
    strip_prefix = "rules_jvm_external-4.0",
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/4.0.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")
load("//:app/src/test/junit5.bzl", "junit_jupiter_java_repositories", "junit_platform_java_repositories")

# Install junit dependencies
JUNIT_JUPITER_VERSION = "5.7.2"

JUNIT_PLATFORM_VERSION = "1.7.2"

junit_jupiter_java_repositories(
    version = JUNIT_JUPITER_VERSION,
)

junit_platform_java_repositories(
    version = JUNIT_PLATFORM_VERSION,
)

maven_install(
    artifacts = [
        "com.auth0:java-jwt:3.18.1",
        "com.fasterxml.jackson.core:jackson-core:2.12.3",
        "com.fasterxml.jackson.core:jackson-databind:2.12.4",
        "com.google.inject:guice:5.0.1",
        "commons-validator:commons-validator:1.7",
        "io.javalin:javalin-bundle:4.1.1",
        "io.javalin:javalin:4.1.1",
        "org.json:json:20210307",
        "org.postgresql:postgresql:42.2.20.jre7",
        "org.slf4j:slf4j-api:1.7.30",
        maven.artifact(
            "org.junit.jupiter",
            "junit-jupiter-api",
            "5.5.0",
            testonly = True,
        ),
        maven.artifact(
            "org.junit.jupiter",
            "junit-jupiter-params",
            "5.7.0",
            testonly = True,
        ),
        maven.artifact(
            "org.mockito",
            "mockito-core",
            "3.12.4",
            testonly = True,
        ),
        maven.artifact(
            "org.mockito",
            "mockito-junit-jupiter",
            "3.12.4",
            testonly = True,
        ),
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2/",
    ],
)
# Needed the POM file generator:

http_archive(
    name = "bazel_common",
    sha256 = "d8c9586b24ce4a5513d972668f94b62eb7d705b92405d4bc102131f294751f1d",
    strip_prefix = "bazel-common-413b433b91f26dbe39cdbc20f742ad6555dd1e27",
    url = "https://github.com/google/bazel-common/archive/413b433b91f26dbe39cdbc20f742ad6555dd1e27.zip",
)

http_archive(
    name = "bazel_skylib",
    sha256 = "1c531376ac7e5a180e0237938a2536de0c54d93f5c278634818e0efc952dd56c",
    urls = [
        "https://github.com/bazelbuild/bazel-skylib/releases/download/1.0.3/bazel-skylib-1.0.3.tar.gz",
        "https://mirror.bazel.build/github.com/bazelbuild/bazel-skylib/releases/download/1.0.3/bazel-skylib-1.0.3.tar.gz",
    ],
)

load("@bazel_skylib//:workspace.bzl", "bazel_skylib_workspace")

bazel_skylib_workspace()

# nodejs

http_archive(
    name = "build_bazel_rules_nodejs",
    sha256 = "b32a4713b45095e9e1921a7fcb1adf584bc05959f3336e7351bcf77f015a2d7c",
    urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/4.1.0/rules_nodejs-4.1.0.tar.gz"],
)

load("@build_bazel_rules_nodejs//:index.bzl", "node_repositories", "yarn_install")

yarn_install(
    name = "npm",
    package_json = "//:package.json",
    symlink_node_modules = False,
    yarn_lock = "//:yarn.lock",
)
