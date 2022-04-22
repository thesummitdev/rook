load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "database_module",
    srcs = [
        "DatabaseModule.java",
    ],
    deps = [
        ":connection_pool",
        ":database_service",
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_google_inject_guice",
    ],
)

java_library(
    name = "connection_pool",
    srcs = [
        "ConnectionPool.java",
        "RookConnectionPool.java",
    ],
    deps = [
        "@maven//:org_postgresql_postgresql",
    ],
)

java_library(
    name = "database_field",
    srcs = [
        "DatabaseArrayField.java",
        "DatabaseField.java",
    ],
)

java_library(
    name = "database_service",
    srcs = [
        "DatabaseService.java",
        "RookDatabaseService.java",
    ],
    deps = [
        ":connection_pool",
        ":database_field",
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
