load("@rules_java//java:defs.bzl", "java_binary")

package(default_visibility = ["//visibility:public"])

java_library(
    name = "handlers",
    srcs = [
        "AuthHandler.java",
        "LinkHandler.java",
        "PreferenceHandler.java",
        "TagHandler.java",
        "UserHandler.java",
    ],
    deps = [
        "//app/src/main/java/dev/thesummit/flink/auth",
        "//app/src/main/java/dev/thesummit/flink/database:database_service",
        "//app/src/main/java/dev/thesummit/flink/models",
        "@maven//:com_auth0_java_jwt",
        "@maven//:com_fasterxml_jackson_core_jackson_core",
        "@maven//:com_fasterxml_jackson_core_jackson_databind",
        "@maven//:com_google_inject_guice",
        "@maven//:io_javalin_javalin",
        "@maven//:io_javalin_javalin_bundle",
        "@maven//:org_json_json",
        "@maven//:org_slf4j_slf4j_api",
    ],
)
