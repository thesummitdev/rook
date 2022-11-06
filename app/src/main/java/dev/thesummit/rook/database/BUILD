load("@rules_java//java:defs.bzl", "java_library")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "database_module",
    srcs = [
        "DatabaseModule.java",
        "ScriptRunner.java",
        "RuntimeSqlException.java",
    ],
    deps = [
        ":connection_pool",
        ":database_service",
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
    ],
)

java_library(
    name = "connection_pool",
    srcs = [
        "ConnectionPool.java",
        "RookConnectionPool.java",
    ],
    deps = [
        "@maven//:org_xerial_sqlite_jdbc"
    ],
)

java_library(
    name = "database_field",
    srcs = [
        "DatabaseListField.java",
        "DatabaseField.java",
    ],
)

java_library(
    name = "schema_managers",
    srcs = [
        "DatabaseSchemaManager.java",
        "Sqlite3SchemaManager.java",
        "ScriptRunner.java",
        "RuntimeSqlException.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
        ":database_service",
        ":connection_pool",
        "//app/src/main/java/dev/thesummit/rook/utils:flag_service",
    ],
)

java_library(
    name = "database_service",
    srcs = [
        "DatabaseService.java",
        "RookDatabaseService.java",
        "PagedResults.java",
        "PagedCursor.java",
    ],
    deps = [
        ":connection_pool",
        ":database_field",
        "//app/src/main/java/dev/thesummit/rook/models",
        "@maven//:com_google_inject_guice",
        "@maven//:org_slf4j_slf4j_api",
        "@maven//:org_json_json",
    ],
)
