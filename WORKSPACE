workspace(
    name = "flink",
    managed_directories = {"@npm": ["web/node_modules"]},
)

load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")

RULES_JVM_EXTERNAL_TAG = "4.0"

RULES_JVM_EXTERNAL_SHA = "31701ad93dbfe544d597dbe62c9a1fdd76d81d8a9150c2bf1ecf928ecdf97169"

http_archive(
    name = "build_bazel_rules_nodejs",
    sha256 = "0fa2d443571c9e02fcb7363a74ae591bdcce2dd76af8677a95965edf329d778a",
    urls = ["https://github.com/bazelbuild/rules_nodejs/releases/download/3.6.0/rules_nodejs-3.6.0.tar.gz"],
)

http_archive(
    name = "rules_jvm_external",
    sha256 = RULES_JVM_EXTERNAL_SHA,
    strip_prefix = "rules_jvm_external-%s" % RULES_JVM_EXTERNAL_TAG,
    url = "https://github.com/bazelbuild/rules_jvm_external/archive/%s.zip" % RULES_JVM_EXTERNAL_TAG,
)

http_archive(
    name = "io_bazel_rules_sass",
    sha256 = "f2294af7e7de950c811b3544899576066136f3f7b75db97e509bc5e6bf9457d1",
    strip_prefix = "rules_sass-1.37.0",
    # Make sure to check for the latest version when you install
    url = "https://github.com/bazelbuild/rules_sass/archive/1.37.0.zip",
)

load("@rules_jvm_external//:defs.bzl", "maven_install")
load("@rules_jvm_external//:specs.bzl", "maven")
load("@build_bazel_rules_nodejs//:index.bzl", "node_repositories", "npm_install")
load("@io_bazel_rules_sass//:package.bzl", "rules_sass_dependencies", "rules_sass_dev_dependencies")
load("@io_bazel_rules_sass//:defs.bzl", "sass_repositories")
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
        "com.fasterxml.jackson.core:jackson-core:2.12.3",
        "com.fasterxml.jackson.core:jackson-databind:2.12.4",
        "com.google.inject:guice:5.0.1",
        "commons-validator:commons-validator:1.7",
        "io.javalin:javalin-bundle:3.13.7",
        "io.javalin:javalin:3.3.0",
        "org.json:json:20210307",
        "org.postgresql:postgresql:42.2.20.jre7",
        "org.slf4j:slf4j-simple:1.7.30",
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
            "3.2.4",
            testonly = True,
        ),
    ],
    fetch_sources = True,
    repositories = [
        "https://maven.google.com",
        "https://repo1.maven.org/maven2/",
    ],
)

npm_install(
    name = "npm",
    package_json = "//web:package.json",
    package_lock_json = "//web:package-lock.json",
)

# Fetch required transitive dependencies. This is an optional step because you
# can always fetch the required NodeJS transitive dependency on your own.
#rules_sass_dependencies()

#rules_sass_dev_dependencies()

# Setup repositories which are needed for the Sass rules.
sass_repositories()

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
